package com.chaoscraft.client.modules.chaos;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.ui.screens.ProfilesScreen;

/** Client-Profile: PvP, SMP, Chaoscraft, Recording … – Taste öffnet die Profilverwaltung. */
public class ProfilesModule extends Module {

    public ProfilesModule() {
        super("Profiles", "Chaos-Client-Profile verwalten, wechseln, importieren/exportieren.", Category.CHAOS, "▣");
        alwaysOn();
        tags("profil", "profile", "config", "export", "import");
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled && isEnabled() && mc.currentScreen == null && mc.player != null) { mc.setScreen(new ProfilesScreen(null)); return; }
        super.setEnabled(true);
    }

    @Override public void onTick() { ChaosClient.get(); }
}
