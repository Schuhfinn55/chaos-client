package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.Category;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.setting.DoubleSetting;
import org.lwjgl.glfw.GLFW;

/**
 * Zoom — Optifine-style zoom while holding the bound key.
 * Uses ZoomMixin on GameRenderer.getFov to divide the FOV while the key is held.
 * Yarn mappings 1.21.11.
 */
public class ZoomModule extends Module {

    private final DoubleSetting factor = add(new DoubleSetting("Factor", "Zoom strength.", 4.0, 2.0, 10.0, 0.5));

    public ZoomModule() {
        super("Zoom", "Hold key to zoom in.", Category.RENDER);
    }

    public float getFactor() {
        return factor.getFloat();
    }

    /** True while the bound key is physically held down. */
    public boolean isZoomKeyHeld() {
        if (mc.getWindow() == null) return false;
        int key = getKeyCode();
        if (key == -1) return false;
        return GLFW.glfwGetKey(mc.getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
    }
}
