package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.general.NoToastsModule;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fortschritts-/Rezept-Popups oben rechts unterdrücken. */
@Mixin(ToastManager.class)
public class ToastManagerMixin {
    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void chaosclient$filterToast(Toast toast, CallbackInfo ci) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        NoToastsModule mod = cc.getModuleManager().get(NoToastsModule.class);
        if (mod != null && mod.isEnabled() && mod.shouldHide(toast)) ci.cancel();
    }
}
