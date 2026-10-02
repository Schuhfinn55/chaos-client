package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.HudModule;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/**
 * Keystrokes — shows W/A/S/D + LMB/RMB on screen, lit up while pressed.
 * Draggable HUD element. Yarn mappings 1.21.11.
 */
public class KeystrokesModule extends HudModule {

    private static final int BOX = 22;
    private static final int GAP = 3;

    public KeystrokesModule() {
        super("Keystrokes", "Show pressed movement keys on screen.", 8, 140);
    }

    @Override
    public int getHudWidth() { return 3 * BOX + 2 * GAP; }

    @Override
    public int getHudHeight() { return 3 * BOX + 2 * GAP; }

    @Override
    public void renderHud(DrawContext ctx) {
        long handle = mc.getWindow().getHandle();

        drawKey(ctx, "W", posX + BOX + GAP, posY,
                GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS);
        drawKey(ctx, "A", posX, posY + BOX + GAP,
                GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS);
        drawKey(ctx, "S", posX + BOX + GAP, posY + BOX + GAP,
                GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS);
        drawKey(ctx, "D", posX + 2 * (BOX + GAP), posY + BOX + GAP,
                GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS);
        drawKey(ctx, "LMB", posX, posY + 2 * (BOX + GAP),
                GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS, 32);
        drawKey(ctx, "RMB", posX + 32 + GAP, posY + 2 * (BOX + GAP),
                GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS, 32);
    }

    private void drawKey(DrawContext ctx, String label, int x, int y, boolean pressed) {
        drawKey(ctx, label, x, y, pressed, BOX);
    }

    private void drawKey(DrawContext ctx, String label, int x, int y, boolean pressed, int w) {
        int bg = pressed ? 0xCC00E676 : 0x99000000;
        int fg = pressed ? 0xFF000000 : 0xFFFFFFFF;
        ctx.fill(x, y, x + w, y + BOX, bg);
        int tw = mc.textRenderer.getWidth(label);
        ctx.drawTextWithShadow(mc.textRenderer, label, x + (w - tw) / 2, y + (BOX - 8) / 2, fg);
    }
}
