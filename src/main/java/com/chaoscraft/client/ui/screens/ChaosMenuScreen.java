package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.core.ModuleManager;
import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.ui.Anim;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import com.chaoscraft.client.ui.widgets.SettingWidgets;
import com.chaoscraft.client.ui.widgets.TextField;
import com.chaoscraft.client.ui.widgets.ToggleWidget;
import com.chaoscraft.client.ui.widgets.Widget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

/**
 * Das Chaos-Client-Hauptmenü: Kopf mit „CHAOS CLIENT“, Suchleiste und
 * Schnellzugriffen, links Kategorien, in der Mitte Modul-Kacheln, rechts
 * die Konfiguration des gewählten Moduls.
 */
public class ChaosMenuScreen extends ChaosScreen {

    private static Category lastCategory = Category.HUD;
    private static String lastQuery = "";

    private Category category = lastCategory;
    private String query = lastQuery;
    private Module selected;
    private ScrollPanel catPanel;
    private ScrollPanel gridPanel;
    private ScrollPanel configPanel;
    private TextField search;
    private int px, py, pw, ph;
    private final Anim configAnim = new Anim(0f, 12f);

    public ChaosMenuScreen() { this((Screen) null); }
    public ChaosMenuScreen(Screen parent) { super("Chaos Client", parent); }
    public ChaosMenuScreen(Category cat) { this((Screen) null); category = cat; }

    private ModuleManager modules() { return ChaosClient.get().getModuleManager(); }

    @Override
    protected void build() {
        pw = panelWidth(860);
        ph = panelHeight(540);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        int headerH = 44;
        int sideW = 118;
        int configW = selected != null ? Math.min(260, pw / 3) : 0;

        // Suche
        search = add(new TextField(px + 150, py + 14, Math.min(260, pw - 420), 18, query, 40, q -> { query = q; lastQuery = q; rebuildGrid(); }).placeholder("Search modules...").icon("⌕"));
        // Schnellzugriffe
        int bx = px + pw - 12;
        bx -= 72; add(new Button(bx, py + 13, 70, 20, "HUD Editor", () -> client.setScreen(new HudEditorScreen(this))).style(Button.Style.PRIMARY).tooltip("HUD-Elemente verschieben, Größe, Farbe, Raster (/chaos hud)"));
        bx -= 64; add(new Button(bx, py + 13, 60, 20, "Keybinds", () -> client.setScreen(new KeybindManagerScreen(this))).tooltip("Zentrale Tastenverwaltung mit Konflikterkennung"));
        bx -= 60; add(new Button(bx, py + 13, 56, 20, "Profile", () -> client.setScreen(new ProfilesScreen(this))).tooltip("Client-Profile: PvP, SMP, Chaoscraft, Recording …"));

        // Kategorien
        catPanel = add(new ScrollPanel(px + 8, py + headerH + 6, sideW, ph - headerH - 30));
        int cy = catPanel.y + 2;
        for (Category c : Category.values()) {
            final Category cat = c;
            int count = (int) modules().getByCategory(c).stream().filter(Module::isEnabled).count();
            Button b = new Button(catPanel.x, cy, sideW - 8, 20, c.getIcon() + "  " + c.getLabel().toUpperCase() + (count > 0 ? "  §7" + count : ""), () -> {
                category = cat; lastCategory = cat; query = ""; lastQuery = ""; search.setValue(""); selected = null; init();
            }).style(Button.Style.GHOST).active(category == cat && query.isEmpty());
            b.tooltip(c.getDescription());
            catPanel.add(b);
            cy += 22;
        }

        // Modul-Raster
        gridPanel = add(new ScrollPanel(px + sideW + 16, py + headerH + 6, pw - sideW - 24 - (configW > 0 ? configW + 8 : 0), ph - headerH - 30));
        rebuildGrid();

        // Konfiguration
        if (selected != null) {
            configPanel = add(new ScrollPanel(px + pw - configW - 8, py + headerH + 6, configW, ph - headerH - 30));
            rebuildConfig();
            configAnim.setTarget(1f);
        } else {
            configPanel = null;
            configAnim.snap(0f);
        }
    }

    private void rebuildGrid() {
        gridPanel.clear();
        gridPanel.resetScroll();
        List<Module> list = query.trim().isEmpty() ? modules().getByCategory(category) : modules().search(query);
        int cols = gridPanel.w >= 420 ? 2 : 1;
        int gap = 8;
        int cw = (gridPanel.w - 8 - gap * (cols - 1)) / cols;
        int chh = 64;
        int i = 0;
        for (Module m : list) {
            int cx = gridPanel.x + (i % cols) * (cw + gap);
            int cy = gridPanel.y + 2 + (i / cols) * (chh + gap);
            gridPanel.add(new ModuleCard(cx, cy, cw, chh, m));
            i++;
        }
        if (list.isEmpty()) gridPanel.add(new com.chaoscraft.client.ui.widgets.Label(gridPanel.x + 8, gridPanel.y + 12, gridPanel.w - 16, "Keine Module gefunden.", theme().textFaint()));
        gridPanel.setContentHeight(((list.size() + cols - 1) / cols) * (chh + gap) + 8);
    }

    private void rebuildConfig() {
        if (configPanel == null || selected == null) return;
        configPanel.clear();
        int x = configPanel.x + 8, w = configPanel.w - 20;
        int y = configPanel.y + 26;
        if (!selected.isSupported()) {
            configPanel.add(new com.chaoscraft.client.ui.widgets.Label(x, y, w, "⚠ " + selected.getUnsupportedReason(), theme().warning()));
            y += 14;
        }
        if (selected instanceof HudModule) {
            configPanel.add(new Button(x, y, w, 18, "Im HUD-Editor positionieren", () -> client.setScreen(new HudEditorScreen(this, (HudModule) selected))).style(Button.Style.DEFAULT));
            y += 24;
        }
        Runnable open = screenFor(selected);
        if (open != null) {
            configPanel.add(new Button(x, y, w, 18, "Öffnen ▸", open).style(Button.Style.PRIMARY));
            y += 24;
        }
        y = SettingWidgets.build(configPanel, selected, x, y, w, true);
        configPanel.add(new Button(x, y + 4, w, 16, "Auf Standard zurücksetzen", () -> { selected.getSettings().forEach(s -> { if (s != selected.getKeybindSetting()) s.reset(); }); rebuildConfig(); }).style(Button.Style.GHOST));
        configPanel.setContentHeight(y + 30 - configPanel.y);
    }

    /** Module mit eigenem Screen. */
    private Runnable screenFor(Module m) {
        return switch (m.getName()) {
            case "Cosmetics" -> () -> client.setScreen(new CosmeticsScreen(this));
            case "Emotes" -> () -> client.setScreen(new CosmeticsScreen(this, CosmeticsScreen.Tab.EMOTES));
            case "Social" -> () -> client.setScreen(new SocialScreen(this));
            case "Server Quick Menu" -> () -> client.setScreen(new ServerQuickScreen(this));
            case "Performance" -> () -> client.setScreen(new PerformanceScreen(this));
            case "Waypoints" -> () -> client.setScreen(new WaypointsScreen(this));
            case "Screenshots" -> () -> client.setScreen(new ScreenshotsScreen(this));
            case "Music Player" -> () -> client.setScreen(new MusicScreen(this));
            case "Profiles" -> () -> client.setScreen(new ProfilesScreen(this));
            case "Keybind Manager" -> () -> client.setScreen(new KeybindManagerScreen(this));
            default -> null;
        };
    }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        panel(ctx, px, py, pw, ph);
        header(ctx, px + 16, py + 12, "CLIENT", null);
        // Trennlinien
        ctx.fill(px + 8, py + 44, px + pw - 8, py + 45, theme().border());
        ctx.fill(catPanel.x + catPanel.w + 4, py + 50, catPanel.x + catPanel.w + 5, py + ph - 26, theme().border());
        // Kategorie-Titel
        String title = query.trim().isEmpty() ? category.getIcon() + " " + category.getLabel().toUpperCase() : "SUCHE: " + query;
        ctx.drawTextWithShadow(textRenderer, title, gridPanel.x, py + 50, theme().accentLight());
        if (configPanel != null) {
            ctx.fill(configPanel.x - 4, py + 50, configPanel.x - 3, py + ph - 26, theme().border());
            ctx.drawTextWithShadow(textRenderer, Draw.trim((selected.getIcon().isEmpty() ? "" : selected.getIcon() + " ") + selected.getName(), configPanel.w - 16), configPanel.x + 8, configPanel.y + 2, theme().text());
            ctx.drawTextWithShadow(textRenderer, Draw.trim(selected.getDescription(), configPanel.w - 16), configPanel.x + 8, configPanel.y + 12, theme().textDim());
        }
        // Footer
        String profile = "Profil: §f" + ChaosClient.get().getConfigManager().getActiveProfile();
        ctx.drawTextWithShadow(textRenderer, profile, px + 12, py + ph - 18, theme().textDim());
        String hint = "§7" + KeybindSetting.keyName(ChaosClient.get().getMenuKey()) + " schließt · Klick: Konfiguration · Rechtsklick: an/aus · /chaos hud";
        ctx.drawTextWithShadow(textRenderer, hint, px + pw - 12 - textRenderer.getWidth(hint), py + ph - 18, theme().textFaint());
        // Tooltips der Scroll-Panels
        for (ScrollPanel p : new ScrollPanel[]{catPanel, gridPanel, configPanel}) {
            if (p == null) continue;
            String t = p.tooltipAt(mx, my);
            if (t != null) { setTooltip(t, mx, my); break; }
        }
    }

    @Override
    protected void renderOverlays(DrawContext ctx, int mx, int my) {
        if (configPanel != null) configPanel.renderOverlays(ctx, mx, my);
    }

    @Override
    protected boolean onKey(net.minecraft.client.input.KeyInput key) {
        if (key.key() == ChaosClient.get().getMenuKey() && !search.isFocused()) { close(); return true; }
        return false;
    }

    /* ---------------- Modul-Kachel ---------------- */
    private final class ModuleCard extends Widget {
        private final Module module;
        private final ToggleWidget toggle;
        private final Anim hover = new Anim(0f, 14f);

        ModuleCard(int x, int y, int w, int h, Module m) {
            super(x, y, w, h);
            this.module = m;
            this.toggle = new ToggleWidget(x + w - 38, y + 8, m::isEnabled, v -> { m.setEnabled(v); ChaosClient.get().getNotifications().moduleToggled(m); });
            toggle.enabled = !m.isAlwaysOn() && m.isSupported();
            tooltip(m.isSupported() ? m.getDescription() : m.getUnsupportedReason());
        }

        @Override
        public void render(DrawContext ctx, int mx, int my, float delta) {
            boolean hov = hovered(mx, my);
            hover.setTarget(hov ? 1f : 0f);
            float hv = hover.get();
            boolean sel = module == selected;
            int r = theme().radius();
            int bg = Draw.mix(theme().bg2(), theme().bg3(), hv * 0.6f);
            if (theme().shadows() && (hv > 0.1f || sel)) Draw.shadow(ctx, x, y, w, h, r, (int) (50 * Math.max(hv, sel ? 1 : 0)));
            Draw.roundedRect(ctx, x, y - (int) (hv * 1.5f), w, h, r, bg);
            Draw.roundedBorder(ctx, x, y - (int) (hv * 1.5f), w, h, r, sel ? theme().accent() : module.isEnabled() ? theme().accentGlow(120) : theme().border());
            if (module.isEnabled()) ctx.fill(x, y + 10, x + 3, y + h - 10, theme().accent());
            int tx = x + 12;
            if (!module.getIcon().isEmpty()) { ctx.drawTextWithShadow(font(), module.getIcon(), tx, y + 9, theme().accentLight()); tx += 14; }
            ctx.drawTextWithShadow(font(), Draw.trim(module.getName(), w - 60 - (tx - x)), tx, y + 9, module.isSupported() ? theme().text() : theme().textFaint());
            // Beschreibung (max. 2 Zeilen)
            String desc = module.isSupported() ? module.getDescription() : "⚠ Nicht unterstützt in dieser Version";
            String l1 = font().trimToWidth(desc, w - 24);
            String rest = desc.substring(l1.length()).trim();
            ctx.drawTextWithShadow(font(), l1, x + 12, y + 24, module.isSupported() ? theme().textDim() : theme().warning());
            if (!rest.isEmpty()) ctx.drawTextWithShadow(font(), Draw.trim(rest, w - 24), x + 12, y + 34, theme().textDim());
            // Keybind + Settings
            String key = "Keybind: §f" + KeybindSetting.keyName(module.getKeyCode());
            ctx.drawTextWithShadow(font(), key, x + 12, y + h - 14, theme().textFaint());
            String more = module.hasSettings() ? "Settings ›" : "";
            ctx.drawTextWithShadow(font(), more, x + w - 12 - font().getWidth(more), y + h - 14, sel ? theme().accentLight() : theme().textFaint());
            toggle.render(ctx, mx, my, delta);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (!hovered(mx, my)) return false;
            if (toggle.mouseClicked(mx, my, button)) { init(); return true; }
            if (button == 1) { if (toggle.enabled) { module.toggle(); ChaosClient.get().getNotifications().moduleToggled(module); init(); } return true; }
            playClick();
            selected = (selected == module) ? null : module;
            init();
            return true;
        }
    }
}
