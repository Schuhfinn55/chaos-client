package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.cosmetics.ChaosPlayerState;
import com.chaoscraft.client.emotes.EmotePose;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Merkt sich die UUID des gerenderten Spielers im Render-State (für Hut-/Wings-Renderer), friert die
 * Emote-Pose pro Frame ein und wendet Ganzkörper-Bewegungen (Sprung, Salto, Drehung, Sitzen) auf die
 * Render-Matrix an – so folgen Hüte, Flügel, Capes, Rüstung und 3D-Skin-Layer automatisch mit.
 */
@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("TAIL"))
    private void chaosclient$storeUuid(PlayerLikeEntity entity, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (state instanceof ChaosPlayerState cps) {
            cps.chaos$setUuid(entity.getUuid());
            try { cps.chaos$setMotion(!entity.isOnGround(), (float) entity.getVelocity().y); } catch (Exception ignored) {}
            // Emote-Pose einmal pro Frame berechnen: Modell, Overlay-Teile und Cosmetics nutzen exakt dieselbe Pose
            EmotePose pose = null;
            try {
                ChaosClient cc = ChaosClient.get();
                if (cc != null && cc.getEmotes() != null) pose = cc.getEmotes().poseFor(entity.getUuid());
            } catch (Exception ignored) {}
            cps.chaos$setEmotePose(pose);
        }
        // Chaos-Badge vor dem Namen über dem Kopf
        try { state.displayName = com.chaoscraft.client.cosmetics.BadgeManager.decorate(entity.getUuid(), state.displayName); } catch (Exception ignored) {}
    }

    /**
     * Ganzkörper-Transformation des Emotes. Hier ist die Matrix noch im Entity-Raum (y nach oben, Ursprung an den
     * Füßen, bereits um den Körper-Yaw gedreht); das Modell und alle Feature-Renderer bauen darauf auf.
     */
    @Inject(method = "setupTransforms(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V", at = @At("RETURN"))
    private void chaosclient$emoteTransform(PlayerEntityRenderState state, MatrixStack matrices, float bodyYaw, float scale, CallbackInfo ci) {
        if (!(state instanceof ChaosPlayerState cps)) return;
        EmotePose pose = cps.chaos$emotePose();
        if (pose == null) return;
        final float u = 1f / 16f; // Modell-Einheit → Block
        // Modellraum: y positiv = nach unten, z positiv = nach hinten; Entity-Raum: y nach oben
        if (pose.offsetY != 0f || pose.offsetZ != 0f) matrices.translate(0f, -pose.offsetY * u, pose.offsetZ * u);
        if (pose.groupPitch != 0f || pose.groupYaw != 0f) {
            float cy = 12f * u; // Körpermitte (Modell y = 12)
            matrices.translate(0f, cy, 0f);
            // Vorzeichen: Modellraum ist gegenüber dem Entity-Raum in x und y gespiegelt
            if (pose.groupYaw != 0f) matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-pose.groupYaw));
            if (pose.groupPitch != 0f) matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-pose.groupPitch));
            matrices.translate(0f, -cy, 0f);
        }
    }
}
