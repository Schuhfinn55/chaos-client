package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.cosmetics.CapeManager;
import com.chaoscraft.client.cosmetics.CosmeticsManager;
import com.chaoscraft.client.cosmetics.EffectCatalog;
import com.chaoscraft.client.cosmetics.HatCatalog;
import com.chaoscraft.client.cosmetics.WingsCatalog;
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
            if (t == Tab.BACK) b.tooltip("Folgt mit einem Chaos-Client-Update über die Cosmetics-API.");
            add(b);
            ty += 24;
        }
        content = add(new ScrollPanel(px + 128, py + 50, pw - 136, ph - 64));
        switch (tab) {
            case CAPES -> buildCapes();
            case EMOTES -> buildEmotes();
            case HATS -> buildHats();
            case WINGS -> buildWings();
            case PARTICLES -> buildEffects();
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

    private void buildHats() {
        CosmeticsManager cm = CosmeticsManager.get();
        CosmeticsModule mod = ChaosClient.get().getModuleManager().get(CosmeticsModule.class);
        int x = content.x + 8, w = content.w - 24, y = content.y + 4;
        if (mod != null) {
            var s = mod.categoryToggle("HATS");
            content.add(new Label(x, y + 4, w - 40, "Hüte anzeigen", theme().text()));
            content.add(new ToggleWidget(x + w - 30, y, s::isEnabled, s::set));
            y += 24;
        }
        content.add(new Label(x, y, w, "HÜTE  §8· werden am Kopf gerendert, andere Chaos-Spieler sehen sie über die API", theme().accentLight()));
        y += 14;
        content.add(new Button(x, y, 100, 16, cm.hatId().isEmpty() ? "Kein Hut ✓" : "Kein Hut", () -> { cm.setHat(""); init(); }).style(cm.hatId().isEmpty() ? Button.Style.PRIMARY : Button.Style.DEFAULT));
        y += 24;
        List<HatCatalog.Hat> hats = HatCatalog.all();
        int cols = Math.max(1, w / 120);
        int cw = (w - (cols - 1) * 8) / cols;
        for (int i = 0; i < hats.size(); i++) {
            HatCatalog.Hat h = hats.get(i);
            int cx = x + (i % cols) * (cw + 8);
            int cy = y + (i / cols) * 104;
            content.add(new CosmeticCard(cx, cy, cw, 96, h.name(), h.description(), colorsOf(h), () -> cm.hatId().equals(h.id()), () -> { cm.setHat(h.id()); init(); }));
        }
        y += ((hats.size() + cols - 1) / cols) * 104 + 8;
        content.setContentHeight(y + 8 - content.y);
    }

    private void buildWings() {
        CosmeticsManager cm = CosmeticsManager.get();
        CosmeticsModule mod = ChaosClient.get().getModuleManager().get(CosmeticsModule.class);
        int x = content.x + 8, w = content.w - 24, y = content.y + 4;
        if (mod != null) {
            var s = mod.categoryToggle("WINGS");
            content.add(new Label(x, y + 4, w - 40, "Wings anzeigen", theme().text()));
            content.add(new ToggleWidget(x + w - 30, y, s::isEnabled, s::set));
            y += 24;
        }
        content.add(new Label(x, y, w, "WINGS  §8· animierte Flügel am Rücken, für alle Chaos-Spieler sichtbar", theme().accentLight()));
        y += 14;
        content.add(new Button(x, y, 110, 16, cm.wingsId().isEmpty() ? "Keine Wings ✓" : "Keine Wings", () -> { cm.setWings(""); init(); }).style(cm.wingsId().isEmpty() ? Button.Style.PRIMARY : Button.Style.DEFAULT));
        y += 24;
        List<WingsCatalog.Wings> list = WingsCatalog.all();
        int cols = Math.max(1, w / 120);
        int cw = (w - (cols - 1) * 8) / cols;
        for (int i = 0; i < list.size(); i++) {
            WingsCatalog.Wings wg = list.get(i);
            int cx = x + (i % cols) * (cw + 8);
            int cy = y + (i / cols) * 104;
            java.util.LinkedHashSet<Integer> set = new java.util.LinkedHashSet<>();
            for (int c : wg.colors()) set.add(c | 0xFF000000);
            int[] colors = new int[Math.min(6, set.size())];
            int k = 0;
            for (int c : set) { if (k >= colors.length) break; colors[k++] = c; }
            boolean locked = !cm.isUnlocked(wg.id());
            content.add(new CosmeticCard(cx, cy, cw, 96, (locked ? "🔒 " : "") + wg.icon() + " " + wg.name(), locked ? "LEGENDÄR – Code im Launcher einlösen" : wg.description(), colors, () -> cm.wingsId().equals(wg.id()), () -> { cm.setWings(wg.id()); init(); }));
        }
        y += ((list.size() + cols - 1) / cols) * 104 + 8;
        content.setContentHeight(y + 8 - content.y);
    }

    private void buildEffects() {
        CosmeticsManager cm = CosmeticsManager.get();
        CosmeticsModule mod = ChaosClient.get().getModuleManager().get(CosmeticsModule.class);
        int x = content.x + 8, w = content.w - 24, y = content.y + 4;
        if (mod != null) {
            var s = mod.categoryToggle("PARTICLES");
            content.add(new Label(x, y + 4, w - 40, "Partikel-Effekte anzeigen", theme().text()));
            content.add(new ToggleWidget(x + w - 30, y, s::isEnabled, s::set));
            y += 24;
        }
        content.add(new Label(x, y, w, "EFFEKTE  §8· rein kosmetisch, clientseitig", theme().accentLight()));
        y += 14;
        content.add(new Button(x, y, 110, 16, cm.effectId().isEmpty() ? "Kein Effekt ✓" : "Kein Effekt", () -> { cm.setEffect(""); init(); }).style(cm.effectId().isEmpty() ? Button.Style.PRIMARY : Button.Style.DEFAULT));
        y += 24;
        List<EffectCatalog.Effect> effects = EffectCatalog.all();
        int cols = Math.max(1, w / 120);
        int cw = (w - (cols - 1) * 8) / cols;
        for (int i = 0; i < effects.size(); i++) {
            EffectCatalog.Effect e = effects.get(i);
            int cx = x + (i % cols) * (cw + 8);
            int cy = y + (i / cols) * 104;
            content.add(new CosmeticCard(cx, cy, cw, 96, e.name(), e.description(), new int[]{e.color()}, () -> cm.effectId().equals(e.id()), () -> { cm.setEffect(e.id()); init(); }));
        }
        y += ((effects.size() + cols - 1) / cols) * 104 + 8;
        content.setContentHeight(y + 8 - content.y);
    }

    private static int[] colorsOf(HatCatalog.Hat h) {
        java.util.LinkedHashSet<Integer> set = new java.util.LinkedHashSet<>();
        for (HatCatalog.Box b : h.boxes()) set.add(b.color());
        int[] out = new int[set.size()];
        int i = 0;
        for (int c : set) out[i++] = c;
        return out;
    }

    /* ---------- Karte für Hüte/Effekte ---------- */
    private final class CosmeticCard extends Widget {
        private final String name, desc;
        private final int[] colors;
        private final java.util.function.BooleanSupplier isActive;
        private final Runnable apply;
        CosmeticCard(int x, int y, int w, int h, String name, String desc, int[] colors, java.util.function.BooleanSupplier isActive, Runnable apply) {
            super(x, y, w, h); this.name = name; this.desc = desc; this.colors = colors; this.isActive = isActive; this.apply = apply; tooltip(desc);
        }

        @Override
        public void render(DrawContext ctx, int mx, int my, float delta) {
            boolean active = isActive.getAsBoolean();
            boolean hov = hovered(mx, my);
            int r = theme().radius();
            Draw.roundedRect(ctx, x, y, w, h, r, hov ? theme().bg3() : theme().bg2());
            Draw.roundedBorder(ctx, x, y, w, h, r, active ? theme().accent() : theme().border());
            // Farbfelder als Vorschau
            int sw = Math.min(16, (w - 16) / Math.max(1, colors.length));
            int total = sw * colors.length + (colors.length - 1) * 2;
            int sx = x + (w - total) / 2;
            for (int c : colors) {
                Draw.shadow(ctx, sx, y + 12, sw, sw, 3, 80);
                Draw.roundedRect(ctx, sx, y + 12, sw, sw, 3, c);
                sx += sw + 2;
            }
            ctx.drawTextWithShadow(font(), Draw.trim(name, w - 10), x + (w - font().getWidth(Draw.trim(name, w - 10))) / 2, y + 40, theme().text());
            String d = Draw.trim(desc, w - 10);
            ctx.drawText(font(), d, x + (w - font().getWidth(d)) / 2, y + 52, theme().textDim(), false);
            String badge = active ? "AKTIV" : hov ? "ANWENDEN" : "CHAOS";
            int bw = font().getWidth(badge) + 12;
            Draw.roundedRect(ctx, x + (w - bw) / 2, y + h - 22, bw, 14, 7, active ? theme().accent() : hov ? theme().accentDark() : theme().bg3());
            ctx.drawTextWithShadow(font(), badge, x + (w - font().getWidth(badge)) / 2, y + h - 19, active || hov ? 0xFFFFFFFF : theme().textDim());
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (!hovered(mx, my) || button != 0) return false;
            playClick();
            apply.run();
            return true;
        }
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
