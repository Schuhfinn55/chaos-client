package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.player.FreeLookModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/** Free Look: Kamerarotation von der Spielerrotation entkoppeln. */
@Mixin(Camera.class)
public class CameraMixin {
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void chaosclient$freeLook(Args args) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        FreeLookModule fl = cc.getModuleManager().get(FreeLookModule.class);
        if (fl == null || !fl.isActive()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean front = mc.options != null && mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT;
        args.set(0, front ? fl.yaw() + 180f : fl.yaw());
        args.set(1, front ? -fl.pitch() : fl.pitch());
    }
}
