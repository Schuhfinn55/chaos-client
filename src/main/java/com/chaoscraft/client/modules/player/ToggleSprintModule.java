package com.chaoscraft.client.modules.player;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;

/** ToggleSprint: dauerhaft sprinten beim Vorwärtslaufen (keine Pakete, nur Eingabe). */
public class ToggleSprintModule extends Module {

    private final BooleanSetting hungerGate = add(new BooleanSetting("Hunger beachten", "Nur sprinten, wenn Hunger > 6.", true));

    public ToggleSprintModule() {
        super("ToggleSprint", "Sprint per Umschalten statt Halten.", Category.PLAYER, "➤");
        tags("sprint", "sprinting", "laufen");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.player.input == null) return;
        if (!mc.player.input.hasForwardMovement() || mc.player.isSneaking() || mc.player.horizontalCollision || mc.player.isUsingItem()) return;
        if (hungerGate.isEnabled() && mc.player.getHungerManager().getFoodLevel() <= 6) return;
        mc.player.setSprinting(true);
    }
}
