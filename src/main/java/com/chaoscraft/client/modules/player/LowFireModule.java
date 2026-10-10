package com.chaoscraft.client.modules.player;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.DoubleSetting;

/** Low Fire: das Feuer-Overlay nach unten schieben, damit es nicht die Sicht nimmt. */
public class LowFireModule extends Module {

    private final DoubleSetting offset = add(new DoubleSetting("Verschiebung", "Wie weit das Feuer nach unten rückt.", 0.3, 0.0, 0.6, 0.05));

    public LowFireModule() {
        super("Low Fire", "Feuer-Overlay tiefer anzeigen – freie Sicht beim Brennen.", Category.PLAYER, "♨");
        tags("feuer", "fire", "overlay", "brennen");
    }

    public float offset() { return offset.getFloat(); }
}
