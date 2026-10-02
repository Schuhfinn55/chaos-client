package com.chaoscraft.client.modules.chaos;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.ui.screens.KeybindManagerScreen;

/** Keybind Manager: Taste öffnet die zentrale Tastenverwaltung mit Konflikterkennung. */
public class KeybindManagerModule extends Module {

    public KeybindManagerModule() {
        super("Keybind Manager", "Alle Tastenbelegungen an einem Ort, Konflikte werden erkannt.", Category.CHAOS, "⌨");
        alwaysOn();
        tags("keybind", "tasten", "hotkey", "konflikt");
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled && isEnabled() && mc.currentScreen == null && mc.player != null) { mc.setScreen(new KeybindManagerScreen(null)); return; }
        super.setEnabled(true);
    }
}
