package com.chaoscraft.client.modules.gui;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;

/** Clear Background: Unschärfe und Abdunkeln hinter Inventar/Menüs entfernen. */
public class ClearBackgroundModule extends Module {

    private final BooleanSetting blur = add(new BooleanSetting("Unschärfe entfernen", "Keinen Blur hinter Menüs.", true));
    private final BooleanSetting darken = add(new BooleanSetting("Abdunkeln entfernen", "Spielwelt hinter Menüs nicht verdunkeln.", true));

    public ClearBackgroundModule() {
        super("Clear Background", "Menüs ohne Unschärfe und Abdunkeln – die Welt bleibt sichtbar.", Category.GUI, "◌");
        tags("blur", "hintergrund", "unschärfe", "inventar");
    }

    public boolean noBlur() { return blur.isEnabled() && mc.world != null; }
    public boolean noDarken() { return darken.isEnabled() && mc.world != null; }
}
