package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.cosmetics.ChaosPlayerState;
import com.chaoscraft.client.emotes.EmotePose;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/** Wendet Emote-Posen (Ganzkörper-Animationen) auf das Spielermodell an. */
@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin {
    @Shadow @org.spongepowered.asm.mixin.Final public ModelPart leftSleeve;
    @Shadow @org.spongepowered.asm.mixin.Final public ModelPart rightSleeve;
    @Shadow @org.spongepowered.asm.mixin.Final public ModelPart leftPants;
    @Shadow @org.spongepowered.asm.mixin.Final public ModelPart rightPants;
    @Shadow @org.spongepowered.asm.mixin.Final public ModelPart jacket;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
    private void chaosclient$emotePose(PlayerEntityRenderState state, CallbackInfo ci) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null || cc.getEmotes() == null || !(state instanceof ChaosPlayerState cps)) return;
        UUID uuid = cps.chaos$uuid();
        if (uuid == null) return;
        EmotePose pose = cc.getEmotes().poseFor(uuid);
        if (pose == null) return;
        net.minecraft.client.render.entity.model.BipedEntityModel<?> m = (net.minecraft.client.render.entity.model.BipedEntityModel<?>) (Object) this;
        ModelPart[] parts = {m.head, m.body, m.rightArm, m.leftArm, m.rightLeg, m.leftLeg};
        for (int i = 0; i < 6; i++) {
            if (pose.set[i]) {
                parts[i].pitch = (float) Math.toRadians(pose.pitch[i]);
                parts[i].yaw = (float) Math.toRadians(pose.yaw[i]);
                parts[i].roll = (float) Math.toRadians(pose.roll[i]);
            }
        }
        // Gesamtverschiebung und Gruppendrehung um die Körpermitte (y = 12)
        float cy = 12f;
        for (ModelPart p : parts) {
            p.originY += pose.offsetY;
            p.originZ += pose.offsetZ;
        }
        if (pose.groupPitch != 0f) {
            double a = Math.toRadians(pose.groupPitch);
            double ca = Math.cos(a), sa = Math.sin(a);
            float cyo = cy + pose.offsetY, czo = pose.offsetZ;
            for (ModelPart p : parts) {
                double y = p.originY - cyo, z = p.originZ - czo;
                p.originY = (float) (cyo + y * ca - z * sa);
                p.originZ = (float) (czo + y * sa + z * ca);
                p.pitch += (float) a;
            }
        }
        if (pose.groupYaw != 0f) {
            double a = Math.toRadians(pose.groupYaw);
            double ca = Math.cos(a), sa = Math.sin(a);
            for (ModelPart p : parts) {
                double x = p.originX, z = p.originZ - pose.offsetZ;
                p.originX = (float) (x * ca + z * sa);
                p.originZ = (float) (-x * sa + z * ca) + pose.offsetZ;
                p.yaw += (float) a;
            }
        }
        // Zweite Hautschicht übernimmt die Transformationen
        copy(m.hat, m.head);
        copy(jacket, m.body);
        copy(leftSleeve, m.leftArm);
        copy(rightSleeve, m.rightArm);
        copy(leftPants, m.leftLeg);
        copy(rightPants, m.rightLeg);
    }

    private static void copy(ModelPart dst, ModelPart src) {
        if (dst == null || src == null) return;
        dst.originX = src.originX; dst.originY = src.originY; dst.originZ = src.originZ;
        dst.pitch = src.pitch; dst.yaw = src.yaw; dst.roll = src.roll;
    }
}
