package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.hud.PotionHud;
import com.chaoscraft.client.modules.hud.ScoreboardModule;
import com.chaoscraft.client.modules.pvp.CrosshairModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Ersetzt Vanilla-Crosshair, Effekt-Overlay und Scoreboard-Sidebar durch Chaos-Module, wenn aktiv. */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void chaosclient$crosshair(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        CrosshairModule m = cc.getModuleManager().get(CrosshairModule.class);
        if (m != null && m.hideVanilla()) ci.cancel();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void chaosclient$effects(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        PotionHud m = cc.getModuleManager().get(PotionHud.class);
        if (m != null && m.hideVanilla()) ci.cancel();
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
    private void chaosclient$scoreboard(DrawContext ctx, ScoreboardObjective objective, CallbackInfo ci) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        ScoreboardModule m = cc.getModuleManager().get(ScoreboardModule.class);
        try {
            if (m != null && m.render(ctx, objective)) ci.cancel();
        } catch (Exception e) {
            ChaosClient.LOGGER.warn("[ChaosClient] Scoreboard: {}", e.toString());
        }
    }
}
