package com.onyx.visuals.mixin;

import com.onyx.visuals.OnyxVisuals;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.module.impl.CPSModule;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Counts every attack click for the CPS module.
 * Target verified: MinecraftClient.doAttack() returns boolean (method_1536).
 * Yarn mappings 1.21.11.
 */
@Mixin(MinecraftClient.class)
public class CpsMixin {

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void onyxvisuals$countClick(CallbackInfoReturnable<Boolean> cir) {
        OnyxVisuals ov = OnyxVisuals.getInstance();
        if (ov == null || ov.getModuleManager() == null) return;
        Module mod = ov.getModuleManager().getByName("CPS");
        if (mod != null && mod.isEnabled()) {
            ((CPSModule) mod).recordClick();
        }
    }
}
