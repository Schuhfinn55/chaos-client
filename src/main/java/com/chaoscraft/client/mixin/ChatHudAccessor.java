package com.chaoscraft.client.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/** Zugriff auf die Nachrichtenliste (Duplikate zusammenfassen). */
@Mixin(ChatHud.class)
public interface ChatHudAccessor {
    @Accessor("messages") List<ChatHudLine> chaos$messages();
    @Invoker("refresh") void chaos$refresh();
}
