package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.player.FreeLookModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Free Look: Mausbewegung des eigenen Spielers auf die freie Kamera umleiten. */
@Mixin(Entity.class)
public class EntityLookMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void chaosclient$freeLook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || (Object) this != mc.player) return;
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        FreeLookModule fl = cc.getModuleManager().get(FreeLookModule.class);
        if (fl == null || !fl.isActive()) return;
        fl.applyMouse(cursorDeltaX, cursorDeltaY);
        ci.cancel();
    }
}
