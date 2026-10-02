package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.ColorSetting;
import com.chaoscraft.client.settings.EnumSetting;
import com.chaoscraft.client.settings.IntSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * Keystrokes: W A S D + LMB/RMB, optional Space/Shift/Ctrl, animiertes
 * Aufleuchten, Größe, Abstand, Rundung, Transparenz, Farben, Textur-Stil.
 */
public class KeystrokesHud extends HudModule {

    public enum Texture { FLAT, GLASS, OUTLINE }

    private final IntSetting box = add(new IntSetting("Tastengröße", "Kantenlänge einer Taste.", 20, 12, 40));
    private final IntSetting gap = add(new IntSetting("Abstand", "Abstand zwischen Tasten.", 2, 0, 8));
    private final IntSetting round = add(new IntSetting("Rundung", "Eckenradius der Tasten.", 4, 0, 10));
    private final BooleanSetting mouse = add(new BooleanSetting("Maustasten", "LMB/RMB anzeigen.", true));
    private final BooleanSetting space = add(new BooleanSetting("Space", "Leertaste anzeigen.", false));
    private final BooleanSetting shiftCtrl = add(new BooleanSetting("Shift & Ctrl", "Shift und Ctrl anzeigen.", false));
    private final BooleanSetting animate = add(new BooleanSetting("Animation", "Weiches Aufleuchten beim Drücken.", true));
    private final BooleanSetting showCps = add(new BooleanSetting("CPS auf Maustasten", "Klickrate in LMB/RMB einblenden.", false));
    private final ColorSetting keyColor = add(new ColorSetting("Tastenfarbe", "Farbe der Tasten im Ruhezustand.", 0x99000000));
    private final ColorSetting pressedColor = add(new ColorSetting("Gedrückt", "Farbe gedrückter Tasten.", 0xDDE11D2E));
    private final EnumSetting<Texture> texture = add(new EnumSetting<>("Textur", "Stil der Tasten.", Texture.FLAT));

    private final Map<String, Float> glow = new HashMap<>();
    private long lastFrame = System.nanoTime();

    public KeystrokesHud() {
        super("Keystrokes", "Zeigt WASD, Maustasten und optional Space/Shift/Ctrl animiert an.", Category.HUD, "⌨", 8, 60);
        background.set(false);
        tags("keys", "tasten", "wasd", "input");
    }

    private int rows() { int r = 2; if (mouse.isEnabled()) r++; if (space.isEnabled()) r++; if (shiftCtrl.isEnabled()) r++; return r; }
    @Override public int getContentWidth() { return 3 * box.getInt() + 2 * gap.getInt(); }
    @Override public int getContentHeight() { int b = box.getInt(), g = gap.getInt(); int h = 2 * b + g; if (mouse.isEnabled()) h += g + b; if (space.isEnabled()) h += g + b / 2; if (shiftCtrl.isEnabled()) h += g + b; return h; }

    private boolean key(int k) { return GLFW.glfwGetKey(mc.getWindow().getHandle(), k) == GLFW.GLFW_PRESS && mc.currentScreen == null; }
    private boolean mouseBtn(int b) { return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), b) == GLFW.GLFW_PRESS && mc.currentScreen == null; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastFrame) / 1e9f);
        lastFrame = now;
        int b = box.getInt(), g = gap.getInt();
        draw(ctx, "W", x + b + g, y, b, b, key(GLFW.GLFW_KEY_W), dt);
        draw(ctx, "A", x, y + b + g, b, b, key(GLFW.GLFW_KEY_A), dt);
        draw(ctx, "S", x + b + g, y + b + g, b, b, key(GLFW.GLFW_KEY_S), dt);
        draw(ctx, "D", x + 2 * (b + g), y + b + g, b, b, key(GLFW.GLFW_KEY_D), dt);
        int cy = y + 2 * (b + g);
        if (mouse.isEnabled()) {
            int mw = (3 * b + 2 * g - g) / 2;
            String l = "LMB", r = "RMB";
            if (showCps.isEnabled()) {
                var cps = com.chaoscraft.client.ChaosClient.get().getModuleManager().get(CpsHud.class);
                if (cps != null) { l = "LMB"; r = "RMB"; }
            }
            draw(ctx, l, x, cy, mw, b, mouseBtn(0), dt);
            draw(ctx, r, x + mw + g, cy, mw, b, mouseBtn(1), dt);
            cy += b + g;
        }
        if (space.isEnabled()) { draw(ctx, "———", x, cy, 3 * b + 2 * g, b / 2, key(GLFW.GLFW_KEY_SPACE), dt); cy += b / 2 + g; }
        if (shiftCtrl.isEnabled()) {
            int mw = (3 * b + 2 * g - g) / 2;
            draw(ctx, "SHIFT", x, cy, mw, b, key(GLFW.GLFW_KEY_LEFT_SHIFT), dt);
            draw(ctx, "CTRL", x + mw + g, cy, mw, b, key(GLFW.GLFW_KEY_LEFT_CONTROL), dt);
        }
    }

    private void draw(DrawContext ctx, String label, int x, int y, int w, int h, boolean pressed, float dt) {
        float t = glow.getOrDefault(label, 0f);
        float target = pressed ? 1f : 0f;
        if (animate.isEnabled()) { float k = 1f - (float) Math.exp(-18f * dt); t += (target - t) * k; } else t = target;
        glow.put(label, t);
        int bg = Draw.mix(keyColor.argb(), pressedColor.argb(), t);
        int r = Math.min(round.getInt(), h / 2);
        switch (texture.get()) {
            case GLASS -> { Draw.roundedRect(ctx, x, y, w, h, r, bg); Draw.roundedRect(ctx, x + 1, y + 1, w - 2, h / 2, r, Draw.alpha(0xFFFFFF, (int) (30 + 40 * t))); }
            case OUTLINE -> { Draw.roundedRect(ctx, x, y, w, h, r, Draw.alpha(bg, (int) (40 + 160 * t))); Draw.roundedBorder(ctx, x, y, w, h, r, Draw.mix(textColor.argb(), pressedColor.argb() | 0xFF000000, t)); }
            default -> Draw.roundedRect(ctx, x, y, w, h, r, bg);
        }
        int fg = Draw.mix(textColor.argb(), 0xFFFFFFFF, t);
        int tw = textWidth(label);
        if (tw > w - 4) label = Draw.trim(label, w - 4);
        ctx.drawText(mc.textRenderer, label, x + (w - textWidth(label)) / 2, y + (h - 8) / 2, fg, textShadow.isEnabled());
    }
}
