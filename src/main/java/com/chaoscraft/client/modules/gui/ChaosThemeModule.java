package com.chaoscraft.client.modules.gui;

import com.chaoscraft.client.config.ChaosTheme;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.ColorSetting;
import com.chaoscraft.client.settings.DoubleSetting;
import com.chaoscraft.client.settings.EnumSetting;
import com.chaoscraft.client.settings.IntSetting;

/** Chaos Theme: Dark/Light, Akzentfarbe, Scale, Transparenz, Radius, Schatten, Animation, Blur. Immer aktiv. */
public class ChaosThemeModule extends Module {

    private final EnumSetting<ChaosTheme.Mode> mode = add(new EnumSetting<>("Modus", "Dark oder Light Mode für alle Chaos-GUIs.", ChaosTheme.Mode.DARK));
    private final ColorSetting accent = add(new ColorSetting("Akzentfarbe", "Standard: Chaos Red.", 0xFFE11D2E, false));
    private final DoubleSetting scale = add(new DoubleSetting("Scale", "Größe der Chaos-Menüs.", 1.0, 0.8, 1.3, 0.05));
    private final IntSetting opacity = add(new IntSetting("Transparenz", "Deckkraft der Panels in Prozent.", 96, 50, 100).suffix("%"));
    private final IntSetting radius = add(new IntSetting("Radius", "Eckenradius der Panels.", 8, 0, 14));
    private final BooleanSetting shadows = add(new BooleanSetting("Schatten", "Weiche Schatten unter Panels.", true));
    private final BooleanSetting animations = add(new BooleanSetting("Animationen", "Übergänge, Hover- und Klickanimationen.", true));
    private final IntSetting blur = add(new IntSetting("Blur", "Abdunklung/Weichzeichnung des Hintergrunds (0–4).", 2, 0, 4));

    public ChaosThemeModule() {
        super("Chaos Theme", "Aussehen aller Chaos-GUIs: Dark/Light, Akzent, Scale, Transparenz, Radius, Schatten.", Category.GUI, "◈");
        alwaysOn();
        tags("theme", "design", "farbe", "dark", "light", "akzent");
        apply();
    }

    @Override public void onTick() { apply(); }

    private void apply() {
        ChaosTheme.get().apply(mode.get(), accent.argb(), scale.get(), opacity.getInt(), radius.getInt(), shadows.isEnabled(), animations.isEnabled(), blur.getInt());
    }
}
