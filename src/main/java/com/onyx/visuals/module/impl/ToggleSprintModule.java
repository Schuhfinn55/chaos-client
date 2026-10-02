package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.Category;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.setting.BooleanSetting;

/**
 * ToggleSprint — always sprint when moving forward. No packets.
 * Yarn mappings 1.21.11: Input.hasForwardMovement().
 */
public class ToggleSprintModule extends Module {

    private final BooleanSetting hungerGate = add(new BooleanSetting("Hunger Gate", "Only sprint when hunger > 6.", true));

    public ToggleSprintModule() {
        super("ToggleSprint", "Always sprint when moving forward.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.player.input == null) return;

        if (!mc.player.input.hasForwardMovement()) return;
        if (mc.player.isSneaking()) return;
        if (mc.player.horizontalCollision) return;
        if (mc.player.isUsingItem()) return;
        if (hungerGate.isEnabled() && mc.player.getHungerManager().getFoodLevel() <= 6) return;

        mc.player.setSprinting(true);
    }
}
