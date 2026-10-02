package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.hud.CpsHud;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Zählt Linksklicks (Angriff) für den CPS-Counter. */
@Mixin(MinecraftClient.class)
public class CpsMixin {
    @Inject(method = "doAttack", at = @At("HEAD"))
    private void chaosclient$countClick(CallbackInfoReturnable<Boolean> cir) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        CpsHud cps = cc.getModuleManager().get(CpsHud.class);
        if (cps != null && cps.isEnabled()) cps.recordClick();
    }
}
