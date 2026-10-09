package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.emotes.EmoteAnimations;
import com.chaoscraft.client.emotes.EmoteManager;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;

import java.util.List;

/**
 * Emote-Rad: Emotes kreisförmig um die Bildmitte, Maus zeigt → Highlight, Klick spielt ab.
 * Taste des Emotes-Moduls (Standard B) öffnet das Rad, Esc/B schließt es.
 */
public class EmoteWheelScreen extends ChaosScreen {

    private final List<EmoteManager.Emote> emotes;
    private int hover = -1;

    public EmoteWheelScreen(Screen parent) {
        super("Emotes", parent);
        this.emotes = ChaosClient.get().getEmotes().all();
    }

    @Override
    protected void build() {}

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
        int n = emotes.size();
        return (int) Math.floor(ang / (Math.PI * 2) * n + 0.5) % n;
    }

    private int radius() { return Math.min(width, height) / 2 - 48; }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        hover = indexAt(mx, my);
        int cx = width / 2, cy = height / 2;
        int r = radius();
        int n = emotes.size();
        float a = intro.get();
        // Verlauf hinter dem Rad
        ctx.fill(0, 0, width, height, Draw.alpha(0x000000, (int) (120 * a)));
        // Mittelpunkt
        String title = hover >= 0 ? emotes.get(hover).name() : "EMOTES";
        String sub = hover >= 0 ? emotes.get(hover).description() : "Zeigen & klicken · Bewegung bricht ab";
        Draw.roundedRect(ctx, cx - 80, cy - 24, 160, 48, 12, Draw.alpha(theme().bg2(), (int) (230 * a)));
        Draw.roundedBorder(ctx, cx - 80, cy - 24, 160, 48, 12, theme().accent());
        ctx.drawCenteredTextWithShadow(textRenderer, title, cx, cy - 14, theme().text());
        ctx.drawCenteredTextWithShadow(textRenderer, Draw.trim(sub, 150), cx, cy + 2, theme().textDim());
        for (int i = 0; i < n; i++) {
            double ang = (i / (double) n) * Math.PI * 2 - Math.PI / 2;
            double rr = r * (0.55 + 0.45 * a);
            int x = (int) (cx + Math.cos(ang) * rr), y = (int) (cy + Math.sin(ang) * rr);
            boolean h = i == hover;
            int w = 92, hh = 40;
            // Verbindungslinie
            int lx = (int) (cx + Math.cos(ang) * r * 0.36), ly = (int) (cy + Math.sin(ang) * r * 0.36);
            Draw.roundedRect(ctx, Math.min(lx, x), Math.min(ly, y), Math.max(2, Math.abs(lx - x)), Math.max(2, Math.abs(ly - y)), 1, Draw.alpha(h ? theme().accent() : theme().border(), (int) (90 * a)));
            if (h && theme().shadows()) Draw.roundedRect(ctx, x - w / 2 - 4, y - hh / 2 - 4, w + 8, hh + 8, 14, theme().accentGlow(60));
            Draw.roundedRect(ctx, x - w / 2, y - hh / 2, w, hh, 10, Draw.alpha(h ? theme().bg3() : theme().bg2(), (int) (235 * a)));
            Draw.roundedBorder(ctx, x - w / 2, y - hh / 2, w, hh, 10, h ? theme().accent() : theme().border());
            EmoteManager.Emote e = emotes.get(i);
            String icon = e instanceof EmoteAnimations.Animated an ? an.icon() : "•";
            ctx.drawCenteredTextWithShadow(textRenderer, icon, x, y - 15, 0xFFFFFFFF);
            ctx.drawCenteredTextWithShadow(textRenderer, Draw.trim(e.name(), w - 8), x, y - 2, h ? theme().accentLight() : theme().text());
            ctx.drawCenteredTextWithShadow(textRenderer, "§8" + (i + 1), x, y + 9, theme().textFaint());
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean dc) {
        int i = indexAt(click.x(), click.y());
        if (i >= 0) {
            ChaosClient.get().getEmotes().play(emotes.get(i));
            close();
            return true;
        }
        return super.mouseClicked(click, dc);
    }

    @Override
    protected boolean onKey(KeyInput key) {
        // Zifferntasten 1-9/0 wählen direkt
        int k = key.key();
        int idx = k >= 49 && k <= 57 ? k - 49 : k == 48 ? 9 : -1;
        if (idx >= 0 && idx < emotes.size()) {
            ChaosClient.get().getEmotes().play(emotes.get(idx));
            close();
            return true;
        }
        return false;
    }
}
