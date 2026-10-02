package com.chaoscraft.client.modules.screen;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.EnumSetting;
import com.chaoscraft.client.settings.IntSetting;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

/**
 * Fenstermodus: Fullscreen, Borderless (randlos auf Monitorgröße), Fenster,
 * skalierbare Fenstergröße. Umschalten per Taste.
 */
public class FullscreenModule extends Module {

    public enum WindowMode { WINDOWED, BORDERLESS, FULLSCREEN }

    private final EnumSetting<WindowMode> mode = add(new EnumSetting<>("Modus", "Fenster, Borderless oder Vollbild.", WindowMode.WINDOWED));
    private final IntSetting width = add(new IntSetting("Fensterbreite", "Breite im Fenstermodus (0 = unverändert).", 0, 0, 7680));
    private final IntSetting height = add(new IntSetting("Fensterhöhe", "Höhe im Fenstermodus (0 = unverändert).", 0, 0, 4320));

    private WindowMode applied = null;
    private boolean borderlessActive;

    public FullscreenModule() {
        super("Fenstermodus", "Fullscreen, Borderless oder Fenster mit eigener Auflösung.", Category.SCREEN, "▭");
        alwaysOn();
        tags("fullscreen", "vollbild", "borderless", "fenster", "auflösung", "resolution");
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Taste: Modus durchschalten
        if (!enabled && isEnabled()) { mode.cycle(true); return; }
        super.setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mode.get() == applied) return;
        Window w = mc.getWindow();
        if (w == null) return;
        long h = w.getHandle();
        switch (mode.get()) {
            case FULLSCREEN -> { if (borderlessActive) undoBorderless(h); if (!w.isFullscreen()) w.toggleFullscreen(); }
            case BORDERLESS -> {
                if (w.isFullscreen()) w.toggleFullscreen();
                long monitor = GLFW.glfwGetPrimaryMonitor();
                GLFWVidMode vm = GLFW.glfwGetVideoMode(monitor);
                int[] mx = new int[1], my = new int[1];
                GLFW.glfwGetMonitorPos(monitor, mx, my);
                GLFW.glfwSetWindowAttrib(h, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
                if (vm != null) { GLFW.glfwSetWindowPos(h, mx[0], my[0]); GLFW.glfwSetWindowSize(h, vm.width(), vm.height()); }
                borderlessActive = true;
            }
            default -> {
                if (w.isFullscreen()) w.toggleFullscreen();
                if (borderlessActive) undoBorderless(h);
                if (width.getInt() > 320 && height.getInt() > 240) w.setWindowedSize(width.getInt(), height.getInt());
            }
        }
        applied = mode.get();
    }

    private void undoBorderless(long h) {
        GLFW.glfwSetWindowAttrib(h, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
        GLFW.glfwSetWindowSize(h, Math.max(854, width.getInt()), Math.max(480, height.getInt()));
        GLFW.glfwSetWindowPos(h, 100, 100);
        borderlessActive = false;
    }
}
