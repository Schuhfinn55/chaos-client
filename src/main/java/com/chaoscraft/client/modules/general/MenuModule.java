package com.chaoscraft.client.modules.general;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;

/**
 * Chaos Menü: hält die Menü-Taste (Standard RIGHT SHIFT, frei belegbar)
 * und Grundoptionen. Immer aktiv.
 */
public class MenuModule extends Module {

    private final BooleanSetting showHint = add(new BooleanSetting("Hinweis beim Beitreten", "Zeigt beim Serverbeitritt kurz, welche Taste das Menü öffnet.", true));

    public MenuModule() {
        super("Chaos Menü", "Öffnet das Chaos-Client-Menü (Standard: RIGHT SHIFT).", Category.GENERAL, "✸");
        alwaysOn();
        tags("menu", "menü", "taste", "right shift", "gui");
    }

    public boolean showHint() { return showHint.isEnabled(); }
}
