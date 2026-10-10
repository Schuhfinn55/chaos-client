package com.chaoscraft.client.modules.player;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.cosmetics.ChaosPlayerState;
import com.chaoscraft.client.settings.BooleanSetting;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;

/** Hide Armor: Rüstung ausblenden, damit Skins und Cosmetics sichtbar bleiben. */
public class HideArmorModule extends Module {

    private final BooleanSetting own = add(new BooleanSetting("Eigene Rüstung", "Deine Rüstung nicht rendern.", true));
    private final BooleanSetting others = add(new BooleanSetting("Andere Spieler", "Rüstung anderer Spieler nicht rendern.", false));
    private final BooleanSetting mobsToo = add(new BooleanSetting("Mobs", "Auch Rüstung von Zombies/Skeletten ausblenden.", false));

    public HideArmorModule() {
        super("Hide Armor", "Rüstung ausblenden – Skin und Cosmetics sichtbar.", Category.PLAYER, "⛨");
        tags("rüstung", "armor", "ausblenden", "skin");
    }

    public boolean shouldHide(BipedEntityRenderState state) {
        if (state instanceof ChaosPlayerState cps) {
            var id = cps.chaos$uuid();
            boolean self = id != null && mc.player != null && id.equals(mc.player.getUuid());
            return self ? own.isEnabled() : others.isEnabled();
        }
        return mobsToo.isEnabled();
    }
}
