package com.chaoscraft.client.modules.player;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;

/** ToggleSneak: schleichen bis zum Umschalten. */
public class ToggleSneakModule extends Module {

    public ToggleSneakModule() {
        super("ToggleSneak", "Schleichen per Umschalten statt Halten.", Category.PLAYER, "▼");
        tags("sneak", "shift", "schleichen");
    }

    @Override public void onTick() { if (mc.options != null && mc.currentScreen == null) mc.options.sneakKey.setPressed(true); }
    @Override public void onDisable() { if (mc.options != null) mc.options.sneakKey.setPressed(false); }
}
