package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.cosmetics.CapeManager;
import com.chaoscraft.client.cosmetics.CosmeticsConfig;
import com.chaoscraft.client.emotes.EmoteManager;
import com.chaoscraft.client.modules.cosmetics.CosmeticsModule;
import com.chaoscraft.client.modules.cosmetics.EmotesModule;
import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.KeybindWidget;
import com.chaoscraft.client.ui.widgets.Label;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import com.chaoscraft.client.ui.widgets.ToggleWidget;
import com.chaoscraft.client.ui.widgets.Widget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Cosmetics ingame: Capes (Meine Capes: aktivieren, deaktivieren, wechseln,
 * Vorschau, aktualisieren), Hüte/Wings/Back Items/Partikel (vorbereitet),
 * Emotes (Name, Vorschau, Keybind, abspielen).
 */
public class CosmeticsScreen extends ChaosScreen {

    public enum Tab { CAPES, HATS, WINGS, BACK, PARTICLES, EMOTES }

    private Tab tab;
    private ScrollPanel content;
    private int px, py, pw, ph;

    public CosmeticsScreen(Screen parent) { this(parent, Tab.CAPES); }
    public CosmeticsScreen(Screen parent, Tab tab) { super("Cosmetics", parent); this.tab = tab; }

    @Override
    protected void build() {
        pw = panelWidth(760);
        ph = panelHeight(500);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        add(new Button(px + pw - 62, py + 13, 50, 20, "Zurück", this::close));
        // Tabs
        int tx = px + 8, ty = py + 50;
        String[] names = {"CAPES", "HÜTE", "WINGS", "BACK ITEMS", "PARTIKEL", "EMOTES"};
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            final Tab t = tabs[i];
            Button b = new Button(tx, ty, 112, 20, names[i], () -> { tab = t; init(); }).style(Button.Style.GHOST).active(tab == t);
            if (t != Tab.CAPES && t != Tab.EMOTES) b.tooltip("Folgt mit einem Chaos-Client-Update über die Cosmetics-API.");
            add(b);
            ty += 24;
        }
        content = add(new ScrollPanel(px + 128, py + 50, pw - 136, ph - 64));
        switch (tab) {
            case CAPES -> buildCapes();
            case EMOTES -> buildEmotes();
            default -> buildComing();
        }
    }

    private void buildCapes() {
        CapeManager cm = CapeManager.get();
        CosmeticsModule mod = ChaosClient.get().getModuleManager().get(CosmeticsModule.class);
        int x = content.x + 8, w = content.w - 24, y = content.y + 4;
        if (!cm.isEnabled()) {
            content.add(new Label(x, y, w, "Cosmetics sind deaktiviert oder der Launcher hat keine Daten exportiert.", theme().warning()));
            content.add(new Label(x, y + 12, w, "Starte das Profil über den Chaos Launcher mit aktivierten Cosmetics.", theme().textDim()));
            content.setContentHeight(40);
            return;
        }
        // Schalter
        if (mod != null) {
            content.add(new Label(x, y + 4, w - 40, "Eigenes Cape anzeigen", theme().text()));
            content.add(new ToggleWidget(x + w - 30, y, () -> mod.showOwn().isEnabled(), v -> { mod.showOwn().set(v); cm.setShowOwn(v); }));
            y += 20;
            content.add(new Label(x, y + 4, w - 40, "Capes anderer Chaos-Spieler anzeigen", theme().text()));
            content.add(new ToggleWidget(x + w - 30, y, () -> mod.showOthers().isEnabled(), v -> { mod.showOthers().set(v); cm.setShowOthers(v); }));
            y += 24;
        }
        content.add(new Label(x, y, w, "MEINE CAPES  §8· " + (cm.config().ownerName.isEmpty() ? "" : cm.config().ownerName), theme().accentLight()));
        y += 14;
        content.add(new Button(x, y, 110, 16, "Aktualisieren", () -> { cm.reload(client); ChaosClient.get().getNotifications().info("Capes neu geladen."); init(); }).tooltip("Cape-Daten vom Launcher neu einlesen"));
        content.add(new Button(x + 114, y, 100, 16, cm.activeCapeId().isEmpty() ? "Kein Cape ✓" : "Kein Cape", () -> { cm.setActiveCape(""); init(); }).style(cm.activeCapeId().isEmpty() ? Button.Style.PRIMARY : Button.Style.DEFAULT));
        y += 24;
        List<CosmeticsConfig.LibraryCape> lib = cm.library();
        if (lib.isEmpty()) {
            content.add(new Label(x, y, w, "Keine Capes in der Bibliothek. Lade im Launcher unter Cosmetics → Meine Capes ein Cape hoch.", theme().textDim()));
            y += 14;
        }
        int cols = Math.max(1, w / 120);
        int cw = (w - (cols - 1) * 8) / cols;
        for (int i = 0; i < lib.size(); i++) {
            CosmeticsConfig.LibraryCape c = lib.get(i);
            int cx = x + (i % cols) * (cw + 8);
            int cy = y + (i / cols) * 120;
            content.add(new CapeCard(cx, cy, cw, 112, c));
        }
        y += ((lib.size() + cols - 1) / cols) * 120 + 8;
        String api = cm.config().apiUrl.isEmpty() ? "Cosmetics-API: nicht konfiguriert – Capes lokal (Mitspieler mit Chaos Client sehen sie über die API, sobald sie eingerichtet ist)." : "Cosmetics-API: " + cm.config().apiUrl;
        content.add(new Label(x, y, w, Draw.trim(api, w), theme().textFaint()));
        content.setContentHeight(y + 16 - content.y);
    }

    private void buildEmotes() {
        EmoteManager em = ChaosClient.get().getEmotes();
        EmotesModule mod = ChaosClient.get().getModuleManager().get(EmotesModule.class);
        int x = content.x + 8, w = content.w - 24, y = content.y + 4;
        content.add(new Label(x, y, w, "EMOTES  §8· Taste frei konfigurierbar · Emote-Rad: " + (mod != null ? KeybindSetting.keyName(mod.getKeyCode()) : "—"), theme().accentLight()));
        y += 16;
        for (EmoteManager.Emote e : em.all()) {
            final EmoteManager.Emote emote = e;
            content.add(new Label(x, y + 2, w - 190, e.name(), theme().text()));
            content.add(new Label(x, y + 12, w - 190, e.description(), theme().textDim()));
            KeybindSetting ks = mod != null ? mod.keyFor(e.id()) : null;
            if (ks != null) content.add(new KeybindWidget(x + w - 180, y + 3, 90, ks));
            content.add(new Button(x + w - 84, y + 3, 76, 16, em.active() == e ? "Läuft …" : "Abspielen", () -> { em.play(emote); init(); }).style(Button.Style.PRIMARY));
            y += 28;
        }
        content.add(new Label(x, y + 4, w, "Emotes laufen clientseitig (Handschwung, Drehung). Synchronisierte Animationen folgen über die Cosmetics-API.", theme().textFaint()));
        content.setContentHeight(y + 24 - content.y);
    }

    private void buildComing() {
        int x = content.x + 8, w = content.w - 24, y = content.y + 8;
        String what = switch (tab) { case HATS -> "Hüte"; case WINGS -> "Wings"; case BACK -> "Back Items"; default -> "Partikel-Effekte"; };
        content.add(new Label(x, y, w, what + " – vorbereitet", theme().accentLight()));
        content.add(new Label(x, y + 14, w, "Diese Kategorie ist im Cosmetics-System registriert und wird über dieselbe", theme().textDim()));
        content.add(new Label(x, y + 24, w, "Chaos-Cosmetics-API ausgeliefert wie Capes. Du kannst sie hier ein-/ausschalten,", theme().textDim()));
        content.add(new Label(x, y + 34, w, "sobald Inhalte verfügbar sind.", theme().textDim()));
        CosmeticsModule mod = ChaosClient.get().getModuleManager().get(CosmeticsModule.class);
        if (mod != null) {
            var s = mod.categoryToggle(tab.name());
            if (s != null) {
                content.add(new Label(x, y + 56, w - 40, what + " anzeigen", theme().text()));
                content.add(new ToggleWidget(x + w - 30, y + 52, s::isEnabled, s::set));
            }
        }
        content.setContentHeight(90);
    }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        panel(ctx, px, py, pw, ph);
        header(ctx, px + 16, py + 12, "COSMETICS", null);
        ctx.fill(px + 8, py + 44, px + pw - 8, py + 45, theme().border());
        ctx.fill(px + 124, py + 50, px + 125, py + ph - 14, theme().border());
        String t = content.tooltipAt(mx, my);
        if (t != null) setTooltip(t, mx, my);
    }

    /* ---------- Cape-Karte mit Vorschau ---------- */
    private final class CapeCard extends Widget {
        private final CosmeticsConfig.LibraryCape cape;
        CapeCard(int x, int y, int w, int h, CosmeticsConfig.LibraryCape c) { super(x, y, w, h); this.cape = c; tooltip(c.name() + " · " + c.source()); }

        @Override
        public void render(DrawContext ctx, int mx, int my, float delta) {
            boolean active = CapeManager.get().activeCapeId().equals(cape.id());
            boolean hov = hovered(mx, my);
            int r = theme().radius();
            Draw.roundedRect(ctx, x, y, w, h, r, hov ? theme().bg3() : theme().bg2());
            Draw.roundedBorder(ctx, x, y, w, h, r, active ? theme().accent() : theme().border());
            // Vorschau: Vorderseite des Capes (u 1/64..11/64, v 1/32..17/32)
            Identifier tex = CapeManager.get().previewTexture(cape);
            int pwid = 30, phei = 48;
            int pxx = x + (w - pwid) / 2, pyy = y + 8;
            Draw.roundedRect(ctx, pxx - 3, pyy - 3, pwid + 6, phei + 6, 4, theme().bg0());
            if (tex != null) {
                ctx.drawTexture(RenderPipelines.GUI_TEXTURED, tex, pxx, pyy, 64f / 64f * 1f, 1f, pwid, phei, 10, 16, 64, 32);
            } else {
                ctx.drawTextWithShadow(font(), "?", pxx + pwid / 2 - 2, pyy + phei / 2 - 4, theme().textFaint());
            }
            ctx.drawTextWithShadow(font(), Draw.trim(cape.name(), w - 10), x + (w - font().getWidth(Draw.trim(cape.name(), w - 10))) / 2, y + 62, theme().text());
            String badge = active ? "AKTIV" : hov ? "ANWENDEN" : cape.source().toUpperCase();
            int bw = font().getWidth(badge) + 12;
            Draw.roundedRect(ctx, x + (w - bw) / 2, y + h - 22, bw, 14, 7, active ? theme().accent() : hov ? theme().accentDark() : theme().bg3());
            ctx.drawTextWithShadow(font(), badge, x + (w - font().getWidth(badge)) / 2, y + h - 19, active || hov ? 0xFFFFFFFF : theme().textDim());
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (!hovered(mx, my) || button != 0) return false;
            playClick();
            CapeManager.get().setActiveCape(CapeManager.get().activeCapeId().equals(cape.id()) ? "" : cape.id());
            init();
            return true;
        }
    }
}
