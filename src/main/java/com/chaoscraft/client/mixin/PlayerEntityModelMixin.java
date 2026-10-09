package com.chaoscraft.client.mixin;

import com.chaoscraft.client.cosmetics.ChaosPlayerState;
import com.chaoscraft.client.emotes.EmotePose;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wendet die Gliedmaßen-Winkel einer Emote-Pose auf das Spielermodell an. Die Pose wird einmal pro Frame im
 * {@link PlayerEntityRendererMixin} eingefroren; Ganzkörper-Verschiebung/-Drehung passiert dort auf der Matrix.
 * Hut-, Jacken-, Ärmel- und Hosen-Overlay sind in 1.21.11 Kinder der Basisteile und folgen automatisch –
 * sie dürfen NICHT zusätzlich kopiert werden (sonst doppelte Drehung, sichtbar v. a. mit 3D Skin Layers).
 */
@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin {

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
    private void chaosclient$emotePose(PlayerEntityRenderState state, CallbackInfo ci) {
        if (!(state instanceof ChaosPlayerState cps)) return;
        EmotePose pose = cps.chaos$emotePose();
        if (pose == null) return;
        BipedEntityModel<?> m = (BipedEntityModel<?>) (Object) this;
        ModelPart[] parts = {m.head, m.body, m.rightArm, m.leftArm, m.rightLeg, m.leftLeg};
        for (int i = 0; i < 6; i++) {
            if (!pose.set[i]) continue;
            parts[i].pitch = (float) Math.toRadians(pose.pitch[i]);
            parts[i].yaw = (float) Math.toRadians(pose.yaw[i]);
            parts[i].roll = (float) Math.toRadians(pose.roll[i]);
        }
    }
}
