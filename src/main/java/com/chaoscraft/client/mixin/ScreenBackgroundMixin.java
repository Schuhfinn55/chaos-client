package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.gui.ClearBackgroundModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Clear Background: Abdunkeln und Unschärfe hinter Menüs im Spiel entfernen. */
@Mixin(Screen.class)
public class ScreenBackgroundMixin {
    @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
    private void chaosclient$noDarken(DrawContext ctx, CallbackInfo ci) {
        ClearBackgroundModule mod = mod();
        if (mod != null && mod.noDarken()) ci.cancel();
    }

    @Inject(method = "applyBlur", at = @At("HEAD"), cancellable = true)
    private void chaosclient$noBlur(DrawContext ctx, CallbackInfo ci) {
        ClearBackgroundModule mod = mod();
        if (mod != null && mod.noBlur()) ci.cancel();
    }

    private static ClearBackgroundModule mod() {
        ChaosClient cc = ChaosClient.get();
        if (cc == null || cc.getModuleManager() == null) return null;
        ClearBackgroundModule m = cc.getModuleManager().get(ClearBackgroundModule.class);
        return m != null && m.isEnabled() ? m : null;
    }
}
