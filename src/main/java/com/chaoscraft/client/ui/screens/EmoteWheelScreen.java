package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.emotes.EmoteAnimations;
import com.chaoscraft.client.emotes.EmoteManager;
import com.chaoscraft.client.modules.cosmetics.EmotesModule;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.Label;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;

import java.util.List;

/**
 * Emote-Rad: 8 Slots kreisförmig um die Bildmitte. Linksklick spielt ab, Rechtsklick (oder
 * „Bearbeiten“) öffnet die Auswahl, um den Slot mit einem anderen Emote zu belegen.
 * Ziffern 1–8 wählen direkt. Legendäre Emotes sind gesperrt, bis ein Code eingelöst wurde.
 */
public class EmoteWheelScreen extends ChaosScreen {

    private final EmotesModule mod;
    private final EmoteManager em;
    private List<String> wheel;
    private int hover = -1;
    private boolean editMode;
    private int pickSlot = -1;
    private ScrollPanel picker;

    public EmoteWheelScreen(Screen parent) {
        super("Emotes", parent);
        this.mod = ChaosClient.get().getModuleManager().get(EmotesModule.class);
        this.em = ChaosClient.get().getEmotes();
        this.wheel = mod != null ? mod.wheel() : EmotesModule.DEFAULT_WHEEL;
    }

    @Override
    protected void build() {
        picker = null;
        if (pickSlot >= 0) buildPicker();
        else add(new Button(width - 94, 8, 86, 20, editMode ? "Fertig" : "Bearbeiten", () -> { editMode = !editMode; rebuild(); }).style(editMode ? Button.Style.PRIMARY : Button.Style.GHOST).tooltip("Slots umbelegen – Rechtsklick auf einen Slot"));
    }

    private void rebuild() { widgets.clear(); build(); }

    private void buildPicker() {
        int pw = Math.min(380, width - 20), ph = Math.min(320, height - 20);
        int px = (width - pw) / 2, py = (height - ph) / 2;
        add(new Button(px + pw - 62, py + 10, 54, 18, "Zurück", () -> { pickSlot = -1; rebuild(); }));
        add(new Button(px + pw - 128, py + 10, 60, 18, "Leeren", () -> { mod.setSlot(pickSlot, ""); wheel = mod.wheel(); pickSlot = -1; rebuild(); }).style(Button.Style.DANGER));
        picker = add(new ScrollPanel(px + 8, py + 36, pw - 16, ph - 44));
        int y = picker.y + 2, x = picker.x + 4, w = picker.w - 16;
        for (EmoteManager.Emote e : em.all()) {
            if (!(e instanceof EmoteAnimations.Animated a)) continue;
            boolean unlocked = em.isUnlocked(a);
            int inSlot = wheel.indexOf(a.id());
            String title = a.icon() + " " + a.name() + (a.exclusive() ? " §6★" : "") + (inSlot >= 0 ? " §8· Slot " + (inSlot + 1) : "");
            picker.add(new Label(x, y + 2, w - 70, title, unlocked ? theme().text() : theme().textFaint()));
            picker.add(new Label(x, y + 12, w - 70, Draw.trim(unlocked ? a.description() : "Legendär – Code im Chaos Launcher einlösen", w - 74), theme().textFaint()));
            final String id = a.id();
            Button b = picker.add(new Button(x + w - 62, y + 3, 58, 16, unlocked ? "Wählen" : "Gesperrt", () -> { mod.setSlot(pickSlot, id); wheel = mod.wheel(); pickSlot = -1; rebuild(); }).style(unlocked ? Button.Style.PRIMARY : Button.Style.GHOST));
            b.enabled = unlocked;
            y += 26;
        }
        picker.setContentHeight(y + 4 - picker.y);
    }

    @Override
    public boolean shouldPause() { return false; }

    private int indexAt(double mx, double my) {
        double cx = width / 2.0, cy = height / 2.0;
        double dx = mx - cx, dy = my - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        int r = radius();
        if (dist < r * 0.32 || dist > r * 1.45) return -1;
        double ang = Math.atan2(dy, dx) + Math.PI / 2; // 0 = oben
        if (ang < 0) ang += Math.PI * 2;
        return (int) Math.floor(ang / (Math.PI * 2) * EmotesModule.SLOTS + 0.5) % EmotesModule.SLOTS;
    }

    private int radius() { return Math.min(width, height) / 2 - 48; }

    private EmoteAnimations.Animated at(int slot) {
        if (slot < 0 || slot >= wheel.size()) return null;
        return EmoteAnimations.byId(wheel.get(slot));
    }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        float a = intro.get();
        ctx.fill(0, 0, width, height, Draw.alpha(0x000000, (int) (120 * a)));
        if (pickSlot >= 0) {
            int pw = Math.min(380, width - 20), ph = Math.min(320, height - 20);
            int px = (width - pw) / 2, py = (height - ph) / 2;
            panel(ctx, px, py, pw, ph);
            ctx.drawTextWithShadow(textRenderer, "§c✸ §fSlot " + (pickSlot + 1) + " belegen", px + 12, py + 15, theme().text());
            return;
        }
        hover = indexAt(mx, my);
        int cx = width / 2, cy = height / 2;
        int r = radius();
        EmoteAnimations.Animated hov = at(hover);
        String title = hover >= 0 ? (hov != null ? hov.name() : "Leer") : "EMOTES";
        String sub = hover >= 0 ? (hov != null ? (em.isUnlocked(hov) ? hov.description() : "Legendär – Code im Launcher einlösen") : "Rechtsklick zum Belegen") : (editMode ? "Klick auf einen Slot zum Belegen" : "Klick = abspielen · Rechtsklick = tauschen · 1–8");
        Draw.roundedRect(ctx, cx - 86, cy - 24, 172, 48, 12, Draw.alpha(theme().bg2(), (int) (230 * a)));
        Draw.roundedBorder(ctx, cx - 86, cy - 24, 172, 48, 12, editMode ? theme().accentLight() : theme().accent());
        ctx.drawCenteredTextWithShadow(textRenderer, title, cx, cy - 14, theme().text());
        ctx.drawCenteredTextWithShadow(textRenderer, Draw.trim(sub, 160), cx, cy + 2, theme().textDim());
        int n = EmotesModule.SLOTS;
        for (int i = 0; i < n; i++) {
            double ang = (i / (double) n) * Math.PI * 2 - Math.PI / 2;
            double rr = r * (0.55 + 0.45 * a);
            int x = (int) (cx + Math.cos(ang) * rr), y = (int) (cy + Math.sin(ang) * rr);
            boolean h = i == hover;
            int w = 96, hh = 42;
            EmoteAnimations.Animated e = at(i);
            boolean locked = e != null && !em.isUnlocked(e);
            int lx = (int) (cx + Math.cos(ang) * r * 0.36), ly = (int) (cy + Math.sin(ang) * r * 0.36);
            Draw.roundedRect(ctx, Math.min(lx, x), Math.min(ly, y), Math.max(2, Math.abs(lx - x)), Math.max(2, Math.abs(ly - y)), 1, Draw.alpha(h ? theme().accent() : theme().border(), (int) (90 * a)));
            if (h && theme().shadows()) Draw.roundedRect(ctx, x - w / 2 - 4, y - hh / 2 - 4, w + 8, hh + 8, 14, theme().accentGlow(60));
            Draw.roundedRect(ctx, x - w / 2, y - hh / 2, w, hh, 10, Draw.alpha(h ? theme().bg3() : theme().bg2(), (int) (235 * a)));
            Draw.roundedBorder(ctx, x - w / 2, y - hh / 2, w, hh, 10, h ? theme().accent() : (e != null && e.exclusive() ? 0xFFF5C342 : theme().border()));
            if (e == null) {
                ctx.drawCenteredTextWithShadow(textRenderer, "§8+", x, y - 10, theme().textFaint());
                ctx.drawCenteredTextWithShadow(textRenderer, "§8Leer", x, y + 1, theme().textFaint());
            } else {
                ctx.drawCenteredTextWithShadow(textRenderer, (locked ? "🔒 " : "") + e.icon(), x, y - 15, 0xFFFFFFFF);
                ctx.drawCenteredTextWithShadow(textRenderer, Draw.trim(e.name(), w - 8), x, y - 2, locked ? theme().textFaint() : h ? theme().accentLight() : (e.exclusive() ? 0xFFF5C342 : theme().text()));
            }
            ctx.drawCenteredTextWithShadow(textRenderer, "§8" + (i + 1), x, y + 10, theme().textFaint());
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean dc) {
        if (pickSlot >= 0) return super.mouseClicked(click, dc);
        if (super.mouseClicked(click, dc)) return true;
        int i = indexAt(click.x(), click.y());
        if (i < 0) return false;
        EmoteAnimations.Animated e = at(i);
        if (click.button() == 1 || editMode || e == null) { pickSlot = i; rebuild(); return true; }
        if (!em.isUnlocked(e)) { ChaosClient.get().getNotifications().warn("Legendäres Emote – Code im Chaos Launcher unter Cosmetics › Emotes einlösen."); return true; }
        em.play(e);
        close();
        return true;
    }

    @Override
    protected boolean onKey(KeyInput key) {
        if (pickSlot >= 0) return false;
        int k = key.key();
        int idx = k >= 49 && k <= 56 ? k - 49 : -1;
        if (idx >= 0) {
            EmoteAnimations.Animated e = at(idx);
            if (e != null && em.isUnlocked(e)) { em.play(e); close(); }
            return true;
        }
        return false;
    }
}
