package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.Category;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.setting.IntSetting;

/**
 * BlockOverlay — colored outline around the block the player is looking at.
 * Rendered from OutlineMixin (hooked into WorldRenderer.renderTargetBlockOutline).
 * Yarn mappings 1.21.11.
 */
public class BlockOverlayModule extends Module {

    private final IntSetting red   = add(new IntSetting("Red",   "Outline red channel.",   0,   0, 255));
    private final IntSetting green = add(new IntSetting("Green", "Outline green channel.", 230, 0, 255));
    private final IntSetting blue  = add(new IntSetting("Blue",  "Outline blue channel.",  118, 0, 255));
    private final IntSetting alpha = add(new IntSetting("Alpha", "Outline transparency.",  200, 0, 255));

    public BlockOverlayModule() {
        super("BlockOverlay", "Outline the block you're looking at.", Category.RENDER);
    }

    public int getRed()   { return red.getInt(); }
    public int getGreen() { return green.getInt(); }
    public int getBlue()  { return blue.getInt(); }
    public int getAlpha() { return alpha.getInt(); }
}
