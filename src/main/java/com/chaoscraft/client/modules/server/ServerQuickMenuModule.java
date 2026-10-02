package com.chaoscraft.client.modules.server;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.ui.screens.ServerQuickScreen;
import org.lwjgl.glfw.GLFW;

/** Server Quick Menu: Taste (Standard N) öffnet die Serverliste zum Schnellbeitritt. */
public class ServerQuickMenuModule extends Module {

    public ServerQuickMenuModule() {
        super("Server Quick Menu", "Mit einer Taste Chaoscraft, Lobby, Citybuild, PvP und gespeicherte Server öffnen.", Category.SERVER, "▤");
        getKeybindSetting().set(GLFW.GLFW_KEY_N);
        alwaysOn();
        tags("server", "join", "beitreten", "schnellmenü", "chaoscraft");
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled && isEnabled() && mc.currentScreen == null && mc.player != null) { mc.setScreen(new ServerQuickScreen(null)); return; }
        super.setEnabled(true);
    }
}
