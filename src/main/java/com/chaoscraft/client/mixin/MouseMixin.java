package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.general.ZoomModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Scrollrad während des Zooms → Feinzoom statt Hotbar-Wechsel. */
@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void chaosclient$zoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null || MinecraftClient.getInstance().currentScreen != null) return;
        ZoomModule zoom = cc.getModuleManager().get(ZoomModule.class);
        if (zoom != null && zoom.handleScroll(vertical)) ci.cancel();
    }
}
