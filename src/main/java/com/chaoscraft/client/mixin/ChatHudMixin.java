package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.chat.ChatModule;
import com.chaoscraft.client.modules.pvp.AutoGGModule;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Chat-Verarbeitung: Filter (Nachricht verwerfen), Zeitstempel/Highlights
 * (Nachricht ersetzen) und AutoGG-Trigger. Rekursionsschutz, damit die
 * ersetzte Nachricht nicht erneut verarbeitet wird.
 */
@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Unique private boolean chaosclient$reentrant;

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), cancellable = true)
    private void chaosclient$onChatMessage(Text message, MessageSignatureData signature, MessageIndicator indicator, CallbackInfo ci) {
        if (chaosclient$reentrant || message == null) return;
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        try {
            AutoGGModule gg = cc.getModuleManager().get(AutoGGModule.class);
            if (gg != null && gg.isEnabled()) gg.onChatMessage(message.getString());
            ChatModule chat = cc.getModuleManager().get(ChatModule.class);
            if (chat == null || !chat.isEnabled()) return;
            Text processed = chat.process(message);
            if (processed == null) { ci.cancel(); return; }
            if (processed != message) {
                chaosclient$reentrant = true;
                try {
                    ((ChatHud) (Object) this).addMessage(processed, signature, indicator);
                } finally {
                    chaosclient$reentrant = false;
                }
                ci.cancel();
            }
        } catch (Exception e) {
            ChaosClient.LOGGER.warn("[ChaosClient] Chat-Verarbeitung: {}", e.toString());
        }
    }
}
