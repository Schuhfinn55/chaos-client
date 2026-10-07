package com.chaoscraft.client.mixin;

import com.chaoscraft.client.session.SessionHealer;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Erkennt „Ungültige Sitzung“ und stößt die automatische Sitzungs-Erneuerung an. */
@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin {
    @Inject(method = "<init>(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/text/Text;Lnet/minecraft/network/DisconnectionInfo;Lnet/minecraft/text/Text;)V", at = @At("TAIL"))
    private void chaosclient$onDisconnect(Screen parent, Text title, DisconnectionInfo info, Text buttonLabel, CallbackInfo ci) {
        try { if (info != null) SessionHealer.onDisconnect(info.reason()); } catch (Exception ignored) {}
    }
}
