package com.chaoscraft.client.cosmetics;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.mixin.ChaosPlayerState;
import com.chaoscraft.client.modules.cosmetics.CosmeticsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Rendert Chaos-Hüte am Kopf des Spielers. Jeder Hut wird einmal als
 * ModelPart aus farbigen Quadern gebaut; die Farben kommen aus einer
 * dynamisch erzeugten Paletten-Textur (256×256, 32 Farbfelder à 64×32),
 * damit ein Hut mit einem einzigen Draw-Call gezeichnet werden kann.
 */
public final class HatFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {

    private static final Identifier PALETTE = Identifier.of(ChaosClient.MOD_ID, "hat_palette");
    private static final int TEX = 256, BAND_W = 64, BAND_H = 32, COLS = TEX / BAND_W;

    private static final Map<Integer, int[]> PALETTE_SLOTS = new LinkedHashMap<>();
    private static boolean paletteReady;
    private static final Map<String, ModelPart> PARTS = new HashMap<>();

    public HatFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> ctx) {
        super(ctx);
    }

    /** Palette aus allen Hut-Farben erzeugen und als Textur registrieren. */
    private static void ensurePalette() {
        if (paletteReady) return;
        List<Integer> colors = new ArrayList<>();
        for (HatCatalog.Hat h : HatCatalog.all()) for (HatCatalog.Box b : h.boxes()) if (!colors.contains(b.color())) colors.add(b.color());
        NativeImage img = new NativeImage(TEX, TEX, true);
        int slot = 0;
        for (int color : colors) {
            if (slot >= COLS * (TEX / BAND_H)) break;
            int u = (slot % COLS) * BAND_W, v = (slot / COLS) * BAND_H;
            // NativeImage erwartet ABGR
            int abgr = (color & 0xFF000000) | ((color & 0xFF) << 16) | (color & 0xFF00) | ((color >> 16) & 0xFF);
            img.fillRect(u, v, BAND_W, BAND_H, abgr);
            PALETTE_SLOTS.put(color, new int[]{u, v});
            slot++;
        }
        MinecraftClient.getInstance().getTextureManager().registerTexture(PALETTE, new NativeImageBackedTexture(() -> "chaos_hat_palette", img));
        paletteReady = true;
    }

    private static ModelPart partFor(HatCatalog.Hat hat) {
        return PARTS.computeIfAbsent(hat.id(), id -> {
            ensurePalette();
            ModelData data = new ModelData();
            ModelPartBuilder builder = ModelPartBuilder.create();
            for (HatCatalog.Box b : hat.boxes()) {
                int[] uv = PALETTE_SLOTS.getOrDefault(b.color(), new int[]{0, 0});
                // +2 Rand, damit Filterung nicht in den Nachbarslot blutet
                builder.uv(uv[0] + 2, uv[1] + 2).cuboid(b.x(), b.y(), b.z(), b.w(), b.h(), b.d());
            }
            data.getRoot().addChild("hat", builder, ModelTransform.NONE);
            return TexturedModelData.of(data, TEX, TEX).createModel().getChild("hat");
        });
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        if (state.invisible || !(state instanceof ChaosPlayerState cps)) return;
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        CosmeticsModule mod = cc.getModuleManager().get(CosmeticsModule.class);
        if (mod != null && !mod.categoryToggle("HATS").isEnabled()) return;
        UUID uuid = cps.chaos$uuid();
        String hatId = CosmeticsManager.get().hatFor(uuid);
        if (hatId == null) return;
        HatCatalog.Hat hat = HatCatalog.byId(hatId);
        if (hat == null) return;
        try {
            ModelPart part = partFor(hat);
            matrices.push();
            getContextModel().head.applyTransform(matrices);
            queue.submitModelPart(part, matrices, RenderLayers.entityCutoutNoCull(PALETTE), light, OverlayTexture.DEFAULT_UV, null);
            matrices.pop();
        } catch (Exception e) {
            ChaosClient.LOGGER.warn("[ChaosCosmetics] Hut {}: {}", hatId, e.toString());
        }
    }
}
