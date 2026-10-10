package com.chaoscraft.client.modules.player;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;

/** Name Tags: eigenen Namen in der dritten Person sehen. */
public class NameTagsModule extends Module {

    private final BooleanSetting ownName = add(new BooleanSetting("Eigener Name", "Deinen Namen über dem eigenen Kopf anzeigen (3. Person).", true));

    public NameTagsModule() {
        super("Name Tags", "Eigenen Nametag in der dritten Person anzeigen.", Category.PLAYER, "⌂");
        tags("nametag", "name", "label", "third person");
    }

    public boolean showOwn() { return isEnabled() && ownName.isEnabled(); }
}
