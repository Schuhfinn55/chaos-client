package com.chaoscraft.client.ui.widgets;

import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/** Eigenes Textfeld (Chaos-Stil) mit Cursor, Backspace/Delete, Pfeilen, Einfügen. */
public class TextField extends Widget {

    private String value;
    private String placeholder = "";
    private int cursor;
    private boolean focused;
    private final int maxLength;
    private final Consumer<String> onChange;
    private Runnable onEnter;
    private String icon;

    public TextField(int x, int y, int w, int h, String value, int maxLength, Consumer<String> onChange) {
        super(x, y, w, h);
        this.value = value == null ? "" : value;
        this.cursor = this.value.length();
        this.maxLength = maxLength;
        this.onChange = onChange;
    }

    public TextField placeholder(String p) { this.placeholder = p; return this; }
    public TextField icon(String i) { this.icon = i; return this; }
    public TextField onEnter(Runnable r) { this.onEnter = r; return this; }
    public String getValue() { return value; }
    public void setValue(String v) { value = v == null ? "" : v; cursor = Math.min(cursor, value.length()); }
    @Override public boolean isFocused() { return focused; }
    @Override public void setFocused(boolean f) { focused = f; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (!visible) return;
        int r = Math.min(theme().radius(), h / 2);
        Draw.roundedRect(ctx, x, y, w, h, r, theme().bg0());
        Draw.roundedBorder(ctx, x, y, w, h, r, focused ? theme().accent() : hovered(mx, my) ? theme().borderLight() : theme().border());
        if (focused && theme().shadows()) Draw.roundedBorder(ctx, x - 1, y - 1, w + 2, h + 2, r + 1, theme().accentGlow(50));
        int tx = x + 8;
        if (icon != null) { ctx.drawTextWithShadow(font(), icon, tx, y + (h - 8) / 2, theme().accentLight()); tx += font().getWidth(icon) + 6; }
        int ty = y + (h - 8) / 2;
        ctx.enableScissor(tx, y, x + w - 6, y + h);
        if (value.isEmpty() && !focused) {
            ctx.drawTextWithShadow(font(), placeholder, tx, ty, theme().textFaint());
        } else {
            // Text so verschieben, dass der Cursor sichtbar bleibt
            String before = value.substring(0, Math.min(cursor, value.length()));
            int avail = x + w - 6 - tx;
            int offset = Math.max(0, font().getWidth(before) - avail + 4);
            ctx.drawTextWithShadow(font(), value, tx - offset, ty, theme().text());
            if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
                int cx = tx - offset + font().getWidth(before);
                ctx.fill(cx, ty - 1, cx + 1, ty + 9, theme().accentLight());
            }
        }
        ctx.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        boolean inside = hovered(mx, my);
        focused = inside && enabled;
        if (inside) cursor = value.length();
        return inside;
    }

    @Override
    public boolean charTyped(CharInput chr) {
        if (!focused || !chr.isValidChar()) return false;
        String s = chr.asString();
        if (value.length() + s.length() > maxLength) return true;
        value = value.substring(0, cursor) + s + value.substring(cursor);
        cursor += s.length();
        if (onChange != null) onChange.accept(value);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput key) {
        if (!focused) return false;
        int code = key.key();
        boolean ctrl = (key.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
        switch (code) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (cursor > 0) { value = value.substring(0, cursor - 1) + value.substring(cursor); cursor--; if (onChange != null) onChange.accept(value); }
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (cursor < value.length()) { value = value.substring(0, cursor) + value.substring(cursor + 1); if (onChange != null) onChange.accept(value); }
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> { cursor = Math.max(0, cursor - 1); return true; }
            case GLFW.GLFW_KEY_RIGHT -> { cursor = Math.min(value.length(), cursor + 1); return true; }
            case GLFW.GLFW_KEY_HOME -> { cursor = 0; return true; }
            case GLFW.GLFW_KEY_END -> { cursor = value.length(); return true; }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> { if (onEnter != null) onEnter.run(); return true; }
            case GLFW.GLFW_KEY_ESCAPE -> { focused = false; return true; }
            default -> {
                if (ctrl && code == GLFW.GLFW_KEY_V) {
                    String clip = mc.keyboard.getClipboard().replace("\n", " ").replace("\r", "");
                    int room = maxLength - value.length();
                    if (room > 0) {
                        if (clip.length() > room) clip = clip.substring(0, room);
                        value = value.substring(0, cursor) + clip + value.substring(cursor);
                        cursor += clip.length();
                        if (onChange != null) onChange.accept(value);
                    }
                    return true;
                }
                if (ctrl && code == GLFW.GLFW_KEY_A) { cursor = value.length(); return true; }
                if (ctrl && code == GLFW.GLFW_KEY_C) { mc.keyboard.setClipboard(value); return true; }
            }
        }
        return focused; // Tasten nicht an das Spiel durchreichen
    }
}
