package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.hud.TablistModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tablist: Ping als farbige Zahl statt Balken. */
@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    @Inject(method = "renderLatencyIcon", at = @At("HEAD"), cancellable = true)
    private void chaosclient$latency(DrawContext ctx, int width, int x, int y, PlayerListEntry entry, CallbackInfo ci) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        TablistModule m = cc.getModuleManager().get(TablistModule.class);
        if (m != null && m.renderLatency(ctx, width, x, y, entry)) ci.cancel();
    }
}
