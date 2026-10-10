package com.chaoscraft.client.mixin;

import com.chaoscraft.client.modules.pvp.ComboCounterHud;
import com.chaoscraft.client.modules.pvp.ReachDisplayHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Meldet eigene Angriffe auf Entities an Combo Counter und Reach Display. */
@Mixin(MinecraftClient.class)
public class AttackMixin {
    @Inject(method = "doAttack", at = @At("HEAD"))
    private void chaosclient$onAttack(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        if (mc.player == null || !(mc.crosshairTarget instanceof EntityHitResult hit)) return;
        try {
            ComboCounterHud.onAttack(hit.getEntity());
            ReachDisplayHud.onAttack(hit);
        } catch (Exception ignored) {}
    }
}
