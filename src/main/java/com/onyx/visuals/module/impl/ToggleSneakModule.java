package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.Category;
import com.onyx.visuals.module.Module;

/**
 * ToggleSneak — keeps the player sneaking until toggled off.
 * Yarn mappings 1.21.11.
 */
public class ToggleSneakModule extends Module {

    public ToggleSneakModule() {
        super("ToggleSneak", "Sneak until toggled off.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.options == null) return;
        mc.options.sneakKey.setPressed(true);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.sneakKey.setPressed(false);
    }
}
