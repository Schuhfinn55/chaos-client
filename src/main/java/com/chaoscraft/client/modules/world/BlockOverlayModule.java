package com.chaoscraft.client.modules.world;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.ColorSetting;

/** Block Overlay: farbiger Rahmen um den anvisierten Block (OutlineMixin). */
public class BlockOverlayModule extends Module {

    private final ColorSetting color = add(new ColorSetting("Farbe", "Farbe des Rahmens (mit Transparenz).", 0xC8E11D2E));

    public BlockOverlayModule() {
        super("Block Overlay", "Farbiger Rahmen um den Block, den du anschaust.", Category.WORLD, "▢");
        tags("outline", "block", "rahmen", "selection");
    }

    public int getRed() { return color.red(); }
    public int getGreen() { return color.green(); }
    public int getBlue() { return color.blue(); }
    public int getAlpha() { return color.alpha(); }
}
