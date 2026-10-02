package com.chaoscraft.client.ui.widgets;

import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;

/** Keybind-Button: Klick → Taste drücken (ESC = abbrechen, Rechtsklick = löschen). */
public class KeybindWidget extends Widget {

    private final KeybindSetting setting;
    private BooleanSupplier conflict = () -> false;

    public KeybindWidget(int x, int y, int w, KeybindSetting setting) {
        super(x, y, w, 16);
        this.setting = setting;
    }

    public KeybindWidget conflict(BooleanSupplier c) { this.conflict = c; return this; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (!visible) return;
        boolean hov = hovered(mx, my);
        boolean listening = setting.isListening();
        int r = Math.min(theme().radius(), 6);
        int border = listening ? theme().warning() : conflict.getAsBoolean() ? theme().danger() : hov ? theme().accent() : theme().borderLight();
        Draw.roundedRect(ctx, x, y, w, h, r, listening ? Draw.alpha(theme().warning(), 40) : theme().bg3());
        Draw.roundedBorder(ctx, x, y, w, h, r, border);
        String label = listening ? "Taste drücken …" : setting.getDisplayName();
        int col = listening ? theme().warning() : setting.isBound() ? theme().accentLight() : theme().textFaint();
        ctx.drawTextWithShadow(font(), Draw.trim(label, w - 8), x + (w - font().getWidth(Draw.trim(label, w - 8))) / 2, y + 4, col);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (setting.isListening()) {
            // Maustaste als Bind (außer Linksklick auf das Feld selbst)
            if (button != 0 || !hovered(mx, my)) { setting.setKey(KeybindSetting.MOUSE_OFFSET + button); return true; }
            setting.stopListening();
            return true;
        }
        if (!enabled || !hovered(mx, my)) return false;
        if (button == 0) setting.startListening();
        else if (button == 1) setting.setKey(KeybindSetting.NONE);
        playClick();
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput key) {
        if (!setting.isListening()) return false;
        int code = key.key();
        if (code == GLFW.GLFW_KEY_ESCAPE) setting.stopListening();
        else if (code == GLFW.GLFW_KEY_BACKSPACE || code == GLFW.GLFW_KEY_DELETE) setting.setKey(KeybindSetting.NONE);
        else setting.setKey(code);
        return true;
    }

    @Override public boolean isFocused() { return setting.isListening(); }
    public KeybindSetting setting() { return setting; }
}
