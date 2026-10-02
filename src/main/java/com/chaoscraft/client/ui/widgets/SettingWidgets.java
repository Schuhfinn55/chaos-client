package com.chaoscraft.client.ui.widgets;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.ColorSetting;
import com.chaoscraft.client.settings.DoubleSetting;
import com.chaoscraft.client.settings.EnumSetting;
import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.settings.Setting;
import com.chaoscraft.client.settings.StringSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Erzeugt für jede Einstellung eine Zeile mit Label + passendem Widget
 * (BOOLEAN → Toggle, SLIDER → Slider, COLOR → Farbwähler, KEYBIND → Button,
 * DROPDOWN → Dropdown, TEXT → Textfeld). Beschreibung als Tooltip.
 */
public final class SettingWidgets {

    private SettingWidgets() {}

    /** Zeile: Label links, Control rechts (oder darunter bei großen Controls). */
    public static final class Row extends Widget {
        private final Setting<?> setting;
        private final Widget control;
        private final boolean below;

        Row(int x, int y, int w, Setting<?> setting, Widget control, boolean below) {
            super(x, y, w, below ? control.h + 14 : Math.max(18, control.h + 4));
            this.setting = setting;
            this.control = control;
            this.below = below;
            tooltip(setting.getDescription());
        }

        @Override
        public void render(DrawContext ctx, int mx, int my, float delta) {
            visible = setting.isVisible();
            if (!visible) return;
            boolean hov = hovered(mx, my);
            if (hov) Draw.roundedRect(ctx, x - 4, y - 2, w + 8, h + 4, 6, Draw.alpha(theme().bg3(), 120));
            String label = setting.getName();
            if (setting instanceof DoubleSetting ds) label += ": §f" + ds.format();
            ctx.drawTextWithShadow(font(), Draw.trim(label, below ? w : w - control.w - 10), x, y + (below ? 0 : (h - 8) / 2 - 1), theme().textDim());
            control.render(ctx, mx, my, delta);
        }

        @Override public boolean hasOverlay() { return control.hasOverlay(); }
        @Override public void renderOverlay(DrawContext ctx, int mx, int my) { control.renderOverlay(ctx, mx, my); }
        @Override public boolean mouseClicked(double mx, double my, int b) { return visible && control.mouseClicked(mx, my, b); }
        @Override public boolean mouseReleased(double mx, double my, int b) { return visible && control.mouseReleased(mx, my, b); }
        @Override public boolean mouseDragged(double mx, double my, int b, double dx, double dy) { return visible && control.mouseDragged(mx, my, b, dx, dy); }
        @Override public boolean mouseScrolled(double mx, double my, double a) { return visible && control.mouseScrolled(mx, my, a); }
        @Override public boolean keyPressed(net.minecraft.client.input.KeyInput k) { return visible && control.keyPressed(k); }
        @Override public boolean charTyped(net.minecraft.client.input.CharInput c) { return visible && control.charTyped(c); }
        @Override public boolean isFocused() { return control.isFocused(); }
        @Override public void setFocused(boolean f) { control.setFocused(f); }
        public Widget control() { return control; }
    }

    /** Baut alle Zeilen eines Moduls ab (x, y) mit Breite w in das Panel. */
    public static int build(ScrollPanel panel, Module module, int x, int y, int w, boolean includeKeybind) {
        int cy = y;
        for (Setting<?> s : module.getSettings()) {
            if (!includeKeybind && s == module.getKeybindSetting()) continue;
            Row row = create(s, x, cy, w, module);
            panel.add(row);
            cy += row.h + 4;
        }
        return cy;
    }

    public static Row create(Setting<?> s, int x, int y, int w, Module module) {
        if (s instanceof BooleanSetting bs) {
            ToggleWidget t = new ToggleWidget(x + w - 30, y + 1, bs::isEnabled, bs::set);
            return new Row(x, y, w, s, t, false);
        }
        if (s instanceof DoubleSetting ds) {
            SliderWidget sl = new SliderWidget(x, y + 12, w, ds);
            return new Row(x, y, w, s, sl, true);
        }
        if (s instanceof ColorSetting cs) {
            ColorWidget cw = new ColorWidget(x, y + 12, w, cs);
            return new Row(x, y, w, s, cw, true);
        }
        if (s instanceof KeybindSetting ks) {
            KeybindWidget kw = new KeybindWidget(x + w - 90, y, 90, ks);
            if (module != null) kw.conflict(() -> ChaosClient.get().getModuleManager().hasConflict(module));
            return new Row(x, y, w, s, kw, false);
        }
        if (s instanceof EnumSetting<?> es) {
            DropdownWidget dd = new DropdownWidget(x + w - 110, y, 110, es::label, () -> {
                List<String> l = new ArrayList<>();
                for (Enum<?> e : es.getOptions()) l.add(EnumSetting.label(e));
                return l;
            }, idx -> setEnum(es, idx));
            return new Row(x, y, w, s, dd, false);
        }
        if (s instanceof StringSetting ss) {
            TextField tf = new TextField(x, y + 12, w, 16, ss.get(), ss.getMaxLength(), ss::set).placeholder(ss.getPlaceholder());
            return new Row(x, y, w, s, tf, true);
        }
        Label l = new Label(x + w - 60, y + 3, 60, () -> String.valueOf(s.get()), 0xFFAAAAAA);
        return new Row(x, y, w, s, l, false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setEnum(EnumSetting es, int idx) {
        List opts = es.getOptions();
        if (idx >= 0 && idx < opts.size()) es.set((Enum) opts.get(idx));
    }
}
