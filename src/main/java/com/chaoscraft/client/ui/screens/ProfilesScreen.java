package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.config.ConfigManager;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.Label;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import com.chaoscraft.client.ui.widgets.TextField;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;

import java.nio.file.Path;
import java.util.List;

/**
 * Client-Profile (PvP, SMP, Chaoscraft, Recording …): wechseln, anlegen,
 * duplizieren, löschen, exportieren/importieren (config/chaosclient/exports).
 */
public class ProfilesScreen extends ChaosScreen {

    private ScrollPanel list;
    private ScrollPanel exports;
    private String newName = "";
    private int px, py, pw, ph;

    public ProfilesScreen(Screen parent) { super("Profile", parent); }

    private ConfigManager cfg() { return ChaosClient.get().getConfigManager(); }

    @Override
    protected void build() {
        pw = panelWidth(620);
        ph = panelHeight(440);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        add(new Button(px + pw - 62, py + 13, 50, 20, "Zurück", this::close));

        int leftW = pw / 2 - 12;
        // Neues Profil
        add(new TextField(px + 8, py + 50, leftW - 130, 18, newName, 24, v -> newName = v).placeholder("Neues Profil (z.B. PvP)"));
        add(new Button(px + 8 + leftW - 126, py + 49, 60, 20, "Leer", () -> { if (!newName.isBlank()) { cfg().createProfile(newName, false); newName = ""; notify("Profil erstellt"); init(); } }).tooltip("Neues Profil mit Standardwerten"));
        add(new Button(px + 8 + leftW - 62, py + 49, 62, 20, "Kopie", () -> { if (!newName.isBlank()) { cfg().createProfile(newName, true); newName = ""; notify("Profil kopiert"); init(); } }).style(Button.Style.PRIMARY).tooltip("Neues Profil aus den aktuellen Einstellungen"));

        list = add(new ScrollPanel(px + 8, py + 76, leftW, ph - 90));
        int y = list.y + 2;
        for (String p : cfg().listProfiles()) {
            final String name = p;
            boolean active = p.equals(cfg().getActiveProfile());
            list.add(new Label(list.x + 6, y + 5, leftW - 130, (active ? "● " : "○ ") + p, active ? theme().accentLight() : theme().text()));
            if (!active) list.add(new Button(list.x + leftW - 122, y, 54, 16, "Laden", () -> { cfg().switchProfile(name); notify("Profil '" + name + "' geladen"); init(); }).style(Button.Style.PRIMARY));
            list.add(new Button(list.x + leftW - 64, y, 26, 16, "⬆", () -> { Path f = cfg().exportProfile(name.equals(cfg().getActiveProfile()) ? name : name); notify("Exportiert: " + f.getFileName()); init(); }).tooltip("Exportieren nach exports/" + p + ".json"));
            list.add(new Button(list.x + leftW - 34, y, 26, 16, "✕", () -> { cfg().deleteProfile(name); notify("Profil gelöscht"); init(); }).style(Button.Style.DANGER).tooltip("Profil löschen"));
            y += 20;
        }
        list.setContentHeight(y + 6 - list.y);

        // Exporte / Import
        int rx = px + pw / 2 + 4;
        exports = add(new ScrollPanel(rx, py + 76, pw / 2 - 12, ph - 90));
        add(new Button(rx, py + 49, 110, 20, "Ordner öffnen", () -> Util.getOperatingSystem().open(cfg().exportsDir())).tooltip("Hier .json-Dateien ablegen, um sie zu importieren"));
        add(new Button(rx + 114, py + 49, 90, 20, "Aktuelles ⬆", () -> { Path f = cfg().exportProfile(cfg().getActiveProfile()); notify("Exportiert: " + f.getFileName()); init(); }));
        int ey = exports.y + 2;
        List<Path> files = cfg().listExports();
        if (files.isEmpty()) exports.add(new Label(exports.x + 6, ey + 4, exports.w - 12, "Keine Exporte. Beispiel: ChaosPvP.json", theme().textFaint()));
        for (Path f : files) {
            final Path file = f;
            exports.add(new Label(exports.x + 6, ey + 5, exports.w - 80, f.getFileName().toString(), theme().text()));
            exports.add(new Button(exports.x + exports.w - 70, ey, 62, 16, "IMPORT", () -> { if (cfg().importProfile(file)) { notify("Importiert in '" + cfg().getActiveProfile() + "'"); init(); } }).style(Button.Style.PRIMARY).tooltip("In das aktive Profil importieren"));
            ey += 20;
        }
        exports.setContentHeight(ey + 6 - exports.y);
    }

    private void notify(String s) { ChaosClient.get().getNotifications().success(s); }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        panel(ctx, px, py, pw, ph);
        header(ctx, px + 16, py + 12, "PROFILE", null);
        ctx.fill(px + 8, py + 44, px + pw - 8, py + 45, theme().border());
        ctx.drawTextWithShadow(textRenderer, "Aktiv: §f" + cfg().getActiveProfile() + "  §8· HUD, Module, Keybinds, Cosmetics, Crosshair, Performance, Chat, GUI", px + 12, py + ph - 14, theme().textDim());
        ctx.drawTextWithShadow(textRenderer, "EXPORT / IMPORT", px + pw / 2 + 4, py + 38, theme().accentLight());
        ctx.fill(px + pw / 2 - 2, py + 50, px + pw / 2 - 1, py + ph - 20, theme().border());
        String t = list.tooltipAt(mx, my);
        if (t == null) t = exports.tooltipAt(mx, my);
        if (t != null) setTooltip(Draw.trim(t, 400), mx, my);
    }
}
