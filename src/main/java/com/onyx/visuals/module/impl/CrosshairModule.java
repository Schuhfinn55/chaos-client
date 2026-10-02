package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.Category;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.setting.BooleanSetting;
import com.onyx.visuals.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

/**
 * Crosshair — custom 2D crosshair in screen center. Color, size, thickness,
 * gap all slider-configurable. Rendered via RenderManager's HUD element.
 * Yarn mappings 1.21.11.
 */
public class CrosshairModule extends Module {

    private final IntSetting red       = add(new IntSetting("Red",       "Crosshair red channel.",   0,   0, 255));
    private final IntSetting green     = add(new IntSetting("Green",     "Crosshair green channel.", 230, 0, 255));
    private final IntSetting blue      = add(new IntSetting("Blue",      "Crosshair blue channel.",  118, 0, 255));
    private final IntSetting size      = add(new IntSetting("Size",      "Length of each arm.",      6,   1, 20));
    private final IntSetting thickness = add(new IntSetting("Thickness", "Line thickness.",          1,   1, 5));
    private final IntSetting gap       = add(new IntSetting("Gap",       "Gap from center.",         2,   0, 10));
    private final BooleanSetting dot   = add(new BooleanSetting("Center Dot", "Draw a dot in the middle.", true));

    public CrosshairModule() {
        super("Crosshair", "Custom crosshair in screen center.", Category.RENDER);
    }

    /** Called by RenderManager every HUD frame. */
    public void render(DrawContext ctx) {
        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        int color = 0xFF000000 | (red.getInt() << 16) | (green.getInt() << 8) | blue.getInt();

        int s = size.getInt(), t = thickness.getInt(), g = gap.getInt();

        ctx.fill(cx - g - s, cy - t / 2, cx - g, cy + (t + 1) / 2, color);
        ctx.fill(cx + g,     cy - t / 2, cx + g + s, cy + (t + 1) / 2, color);
        ctx.fill(cx - t / 2, cy - g - s, cx + (t + 1) / 2, cy - g, color);
        ctx.fill(cx - t / 2, cy + g,     cx + (t + 1) / 2, cy + g + s, color);

        if (dot.isEnabled()) ctx.fill(cx - 1, cy - 1, cx + 2, cy + 2, color);
    }
}
