package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.KeybindWidget;
import com.chaoscraft.client.ui.widgets.Label;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import com.chaoscraft.client.ui.widgets.TextField;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;
import java.util.Map;

/** Zentrale Keybind-Verwaltung: Modul — Taste, Konflikte werden erkannt. */
public class KeybindManagerScreen extends ChaosScreen {

    private ScrollPanel list;
    private String query = "";
    private int px, py, pw, ph;

    public KeybindManagerScreen(Screen parent) { super("Keybinds", parent); }

    @Override
    protected void build() {
        pw = panelWidth(560);
        ph = panelHeight(460);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        add(new TextField(px + pw - 220, py + 14, 150, 18, query, 30, q -> { query = q; rebuild(); }).placeholder("Suchen …").icon("⌕"));
        add(new Button(px + pw - 62, py + 13, 50, 20, "Zurück", this::close));
        list = add(new ScrollPanel(px + 8, py + 48, pw - 16, ph - 60));
        rebuild();
    }

    private void rebuild() {
        list.clear();
        Map<Integer, List<Module>> conflicts = ChaosClient.get().getModuleManager().keyConflicts();
        int y = list.y + 4;
        int x = list.x + 8, w = list.w - 24;
        // Menü-Taste
        list.add(new Label(x, y + 4, w - 100, "Chaos Menü (Standard RIGHT SHIFT)", theme().accentLight()));
        Module menu = ChaosClient.get().getModuleManager().getByName("Chaos Menü");
        if (menu != null) list.add(new KeybindWidget(x + w - 96, y, 96, menu.getKeybindSetting()));
        y += 22;
        for (Module m : ChaosClient.get().getModuleManager().getModules()) {
            if (m == menu) continue;
            if (!query.isEmpty() && !m.matches(query)) continue;
            final Module mod = m;
            boolean conflict = conflicts.containsKey(m.getKeyCode());
            list.add(new Label(x, y + 4, w - 100, (m.getIcon().isEmpty() ? "" : m.getIcon() + " ") + m.getName() + " §8· " + m.getCategory().getLabel(), conflict ? theme().danger() : theme().text()));
            list.add(new KeybindWidget(x + w - 96, y, 96, m.getKeybindSetting()).conflict(() -> ChaosClient.get().getModuleManager().hasConflict(mod)));
            y += 22;
        }
        list.setContentHeight(y + 8 - list.y);
    }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        panel(ctx, px, py, pw, ph);
        header(ctx, px + 16, py + 12, "KEYBINDS", null);
        ctx.fill(px + 8, py + 44, px + pw - 8, py + 45, theme().border());
        Map<Integer, List<Module>> conflicts = ChaosClient.get().getModuleManager().keyConflicts();
        String foot = conflicts.isEmpty() ? "§aKeine Konflikte" : "§c" + conflicts.size() + " Konflikt(e): " + String.join(", ", conflicts.keySet().stream().map(KeybindSetting::keyName).toList());
        ctx.drawTextWithShadow(textRenderer, Draw.trim(foot + "  §7· Klick: neu belegen · Rechtsklick: löschen · Maustasten erlaubt", pw - 24), px + 12, py + ph - 14, theme().textDim());
    }
}
