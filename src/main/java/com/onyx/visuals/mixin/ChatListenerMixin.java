package com.onyx.visuals.mixin;

import com.onyx.visuals.OnyxVisuals;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.module.impl.AutoGGModule;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Listens to incoming chat messages for the AutoGG module.
 * Target verified: ChatHud.addMessage(Text) = method_1812, descriptor (Lnet/minecraft/text/Text;)V.
 * Yarn mappings 1.21.11.
 */
@Mixin(ChatHud.class)
public class ChatListenerMixin {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void onyxvisuals$onChatMessage(Text message, CallbackInfo ci) {
        OnyxVisuals ov = OnyxVisuals.getInstance();
        if (ov == null || ov.getModuleManager() == null) return;
        Module mod = ov.getModuleManager().getByName("AutoGG");
        if (mod != null && mod.isEnabled()) {
            ((AutoGGModule) mod).onChatMessage(message.getString());
        }
    }
}
