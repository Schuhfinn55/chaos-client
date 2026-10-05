package com.chaoscraft.client.cosmetics;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.cosmetics.CosmeticsModule;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.Set;

import java.util.UUID;

/**
 * Animierte Pixel-Art-Wings am Rücken des Spielers.
 *
 * Jeder Flügel ist eine flache Textur-Ebene (Quader mit Tiefe 0) mit Pivot an
 * der Flügelwurzel am Rücken. Animation: Auf-/Zuklappen um die Hochachse
 * (Sinus), leichtes Heben/Senken der Spitzen, schneller beim Laufen, weit
 * geöffnet beim Gleiten/Fliegen, eingeklappt beim Schleichen. Leuchtende
 * Designs werden über die emissive Render-Schicht gezeichnet.
 */
public final class WingsFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {

    private static ModelPart left, right;

    public WingsFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> ctx) {
        super(ctx);
    }

    private static ModelPart build(boolean isLeft) {
        float w = WingsCatalog.planeW(), h = WingsCatalog.planeH(), top = WingsCatalog.planeTop();
        ModelData data = new ModelData();
        data.getRoot().addChild("wing",
            // Nur eine Fläche (Nordseite): beidseitig sichtbar, kein Z-Fighting zwischen Vorder- und Rückseite
            ModelPartBuilder.create().uv(0, 0).mirrored(!isLeft).cuboid(isLeft ? 0f : -w, -top, 0f, w, h, 0f, Set.of(Direction.NORTH)),
            ModelTransform.NONE);
        return TexturedModelData.of(data, WingsCatalog.texW(), WingsCatalog.texH()).createModel().getChild("wing");
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        if (state.invisible || !(state instanceof ChaosPlayerState cps)) return;
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        CosmeticsModule mod = cc.getModuleManager().get(CosmeticsModule.class);
        CosmeticsManager cm = CosmeticsManager.get();
        UUID uuid = cps.chaos$uuid();
        if (mod != null) {
            if (!mod.categoryToggle("WINGS").isEnabled()) return;
            if (!cm.isOwner(uuid) && !mod.showOthers().isEnabled()) return;
            if (cm.isOwner(uuid) && !mod.showOwn().isEnabled()) return;
        }
        String id = cm.wingsFor(uuid);
        if (id == null) return;
        WingsCatalog.Wings w = WingsCatalog.byId(id);
        if (w == null) return;
        try {
            if (left == null) { left = build(true); right = build(false); }
            float t = state.age;
            boolean moving = state.limbSwingAmplitude > 0.15f;
            boolean gliding = state.glidingTicks > 0f || state.applyFlyingRotation;
            boolean sneaking = state.isInPose(EntityPose.CROUCHING);
            float speed = w.flapSpeed() * (gliding ? 1.8f : moving ? 1.5f : 0.8f);
            float amp = w.flapAmp() * (gliding ? 1.3f : moving ? 1.1f : 1f);
            float phase = t * speed;
            float flap = MathHelper.sin(phase);
            // Hauptschlag = Heben/Senken der Spitzen (Roll) – bleibt von hinten immer gut sichtbar;
            // Auf-/Zuklappen (Yaw) nur dezent, damit der Flügel nie zum Strich wird.
            float open = w.openAngle() * 0.75f + flap * amp * 0.3f + (gliding ? 18f : 0f) + (sneaking ? -12f : 0f) + (moving ? 4f : 0f);
            open = MathHelper.clamp(open, 10f, 60f);
            float tilt = w.tilt() + flap * amp * 0.6f + (gliding ? 12f : 0f) - (sneaking ? 6f : 0f);
            float pitch = (sneaking ? 12f : (gliding ? -8f : 0f)) + MathHelper.sin(phase - 0.4f) * 3f;
            float breathe = MathHelper.sin(t * 0.045f) * 1.5f;

            RenderLayer layer = w.glow() ? RenderLayers.entityTranslucentEmissive(w.texture()) : RenderLayers.entityTranslucent(w.texture());
            int lit = w.glow() ? 0x00F000F0 : light;

            matrices.push();
            getContextModel().body.applyTransform(matrices);
            for (int side = 0; side < 2; side++) {
                boolean isLeft = side == 0;
                float sx = isLeft ? 1f : -1f;
                ModelPart p = isLeft ? left : right;
                p.originX = sx * WingsCatalog.rootX();
                p.originY = WingsCatalog.rootY();
                p.originZ = WingsCatalog.rootZ();
                p.xScale = p.yScale = p.zScale = w.scale();
                // Modellraum: y nach unten, Spieler blickt nach -z → negative Yaw klappt die Spitze nach hinten,
                // negative Roll hebt die Spitze an.
                p.setAngles((float) Math.toRadians(pitch), (float) Math.toRadians(-sx * open), (float) Math.toRadians(-sx * (tilt + breathe)));
                queue.submitModelPart(p, matrices, layer, lit, OverlayTexture.DEFAULT_UV, null);
            }
            matrices.pop();
        } catch (Exception e) {
            ChaosClient.LOGGER.warn("[ChaosCosmetics] Wings {}: {}", id, e.toString());
        }
    }
}
