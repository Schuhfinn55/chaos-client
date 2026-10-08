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
 * Jeder Flügel besteht aus zwei Ebenen: der großen Außenschwinge und einer
 * kleineren, dunkleren Innenschwinge dahinter (Tiefe). Animation: ruhiger
 * Flügelschlag im Stand, schneller beim Laufen, weit gespreizt in der Luft
 * (Sprung/Fall – Flügel „bremsen“), weit geöffnet beim Gleiten, eingeklappt
 * beim Schleichen. Leuchtende Designs pulsieren leicht und werden über die
 * emissive Render-Schicht gezeichnet.
 */
public final class WingsFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {

    /** Flügelteile pro Spieler: [links, rechts]. */
    private static final java.util.Map<UUID, ModelPart[]> PARTS = new java.util.HashMap<>();

    public WingsFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> ctx) {
        super(ctx);
    }

    private static ModelPart build(boolean isLeft) {
        float w = WingsCatalog.planeW(), h = WingsCatalog.planeH(), top = WingsCatalog.planeTop();
        ModelData data = new ModelData();
        data.getRoot().addChild("wing",
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
            ModelPart[] parts = PARTS.computeIfAbsent(uuid, k -> new ModelPart[]{build(true), build(false)});
            if (PARTS.size() > 64) { ModelPart[] keep = parts; PARTS.clear(); PARTS.put(uuid, keep); }
            float t = state.age;
            boolean moving = state.limbSwingAmplitude > 0.15f;
            boolean gliding = state.glidingTicks > 0f || state.applyFlyingRotation;
            boolean sneaking = state.isInPose(EntityPose.CROUCHING);
            boolean airborne = cps.chaos$airborne() && !gliding;
            float velY = cps.chaos$velY();
            boolean falling = airborne && velY < -0.25f;

            float speed = w.flapSpeed() * (gliding ? 1.8f : airborne ? 2.4f : moving ? 1.5f : 0.8f);
            float amp = w.flapAmp() * (gliding ? 1.3f : airborne ? 1.7f : moving ? 1.1f : 1f);
            float phase = t * speed;
            float flap = MathHelper.sin(phase);
            // Ruhig und flach aufgespannt hinter dem Rücken; Hauptschlag = Heben/Senken der Spitzen,
            // Auf-/Zuklappen nur dezent. In der Luft weit gespreizt, im Fall hochgerissen (bremsen).
            float open = w.openAngle() * 0.6f + flap * amp * 0.25f + (gliding ? 15f : 0f) + (airborne ? 14f : 0f) + (sneaking ? -8f : 0f) + (moving ? 3f : 0f);
            open = MathHelper.clamp(open, 10f, 62f);
            float tilt = w.tilt() * 0.6f + flap * amp * 0.35f + (gliding ? 8f : 0f) + (falling ? 16f : airborne ? 6f : 0f) - (sneaking ? 4f : 0f);
            tilt = MathHelper.clamp(tilt, -8f, 34f);
            float pitch = sneaking ? 10f : (gliding ? -6f : falling ? -4f : 0f);
            float breathe = MathHelper.sin(t * 0.045f) * 1.0f;
            float pulse = w.glow() ? 1f + 0.035f * MathHelper.sin(t * 0.16f) : 1f;

            net.minecraft.util.Identifier tex = w.frameTexture(System.currentTimeMillis());
            RenderLayer layer = w.glow() ? RenderLayers.entityTranslucentEmissive(tex) : RenderLayers.entityTranslucent(tex);
            int lit = w.glow() ? 0x00F000F0 : light;

            matrices.push();
            getContextModel().body.applyTransform(matrices);
            // Eine Flügellage pro Seite – eine zweite Lage dahinter erzeugt von hinten doppelte Konturen.
            for (int side = 0; side < 2; side++) {
                boolean isLeft = side == 0;
                float sx = isLeft ? 1f : -1f;
                ModelPart p = parts[isLeft ? 0 : 1];
                p.originX = sx * WingsCatalog.rootX();
                p.originY = WingsCatalog.rootY();
                p.originZ = WingsCatalog.rootZ();
                p.xScale = p.yScale = p.zScale = w.scale() * pulse;
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
