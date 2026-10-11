package com.chaoscraft.client.modules.pvp;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.DoubleSetting;

/**
 * Low Shield: das Schild in der Ego-Perspektive kleiner und weiter unten/außen zeichnen,
 * damit es beim Blocken nicht die halbe Sicht nimmt (HeldItemRendererMixin).
 */
public class LowShieldModule extends Module {

    private final DoubleSetting down = add(new DoubleSetting("Nach unten", "Wie weit das Schild nach unten rückt.", 0.35, 0.0, 1.0, 0.05));
    private final DoubleSetting out = add(new DoubleSetting("Nach außen", "Wie weit das Schild zur Seite rückt.", 0.15, 0.0, 0.8, 0.05));
    private final DoubleSetting scale = add(new DoubleSetting("Größe", "Skalierung des Schilds (1 = normal).", 0.8, 0.4, 1.0, 0.05));
    private final BooleanSetting onlyBlocking = add(new BooleanSetting("Nur beim Blocken", "Nur verschieben, während du aktiv blockst.", false));

    public LowShieldModule() {
        super("Low Shield", "Schild in der Ego-Perspektive tiefer und kleiner – freie Sicht beim Blocken.", Category.PVP, "⛨");
        tags("schild", "shield", "block", "sicht", "pvp");
    }

    public float down() { return down.getFloat(); }
    public float out() { return out.getFloat(); }
    public float scale() { return scale.getFloat(); }
    public boolean onlyBlocking() { return onlyBlocking.isEnabled(); }
}
