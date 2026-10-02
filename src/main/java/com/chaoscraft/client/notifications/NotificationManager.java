package com.chaoscraft.client.notifications;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.config.ChaosTheme;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.modules.misc.NotificationsModule;
import com.chaoscraft.client.ui.Anim;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Chaos-Notifications: kleine animierte Popups oben rechts.
 *   [CHAOS] Dein Cape wurde aktiviert.
 */
public class NotificationManager {

    public enum Kind { INFO, SUCCESS, WARNING, ERROR }

    private static final class Entry {
        final String title;
        final String message;
        final Kind kind;
        final long created = System.currentTimeMillis();
        final long duration;
        final Anim slide = new Anim(0f, 10f);
        Entry(String title, String message, Kind kind, long duration) { this.title = title; this.message = message; this.kind = kind; this.duration = duration; }
    }

    private final List<Entry> entries = new ArrayList<>();

    public void push(String title, String message, Kind kind) {
        NotificationsModule settings = ChaosClient.get().getModuleManager().get(NotificationsModule.class);
        if (settings != null && !settings.isEnabled()) return;
        long dur = settings != null ? settings.durationMs() : 4000;
        Entry e = new Entry(title, message, kind, dur);
        e.slide.setTarget(1f);
        synchronized (entries) {
            entries.add(e);
            while (entries.size() > 5) entries.remove(0);
        }
        if (settings == null || settings.sound()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            float vol = ChaosClient.get().getAudio().notificationVolume();
            if (vol > 0 && mc.getSoundManager() != null) {
                mc.execute(() -> mc.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK.value(), kind == Kind.ERROR ? 0.7f : 1.2f, vol)));
            }
        }
    }

    public void info(String message) { push("CHAOS", message, Kind.INFO); }
    public void success(String message) { push("CHAOS", message, Kind.SUCCESS); }
    public void warn(String message) { push("CHAOS", message, Kind.WARNING); }
    public void error(String message) { push("CHAOS", message, Kind.ERROR); }

    public void moduleToggled(Module m) {
        NotificationsModule settings = ChaosClient.get().getModuleManager().get(NotificationsModule.class);
        if (settings != null && !settings.showToggles()) return;
        push("CHAOS", m.getName() + (m.isEnabled() ? " aktiviert" : " deaktiviert"), m.isEnabled() ? Kind.SUCCESS : Kind.INFO);
    }

    /** Wird vom HudRenderer jeden Frame aufgerufen. */
    public void render(DrawContext ctx) {
        ChaosTheme t = ChaosTheme.get();
        MinecraftClient mc = MinecraftClient.getInstance();
        int sw = ctx.getScaledWindowWidth();
        int y = 8;
        long now = System.currentTimeMillis();
        synchronized (entries) {
            Iterator<Entry> it = entries.iterator();
            while (it.hasNext()) {
                Entry e = it.next();
                long age = now - e.created;
                if (age > e.duration) e.slide.setTarget(0f);
                float p = e.slide.get();
                if (age > e.duration && p <= 0.01f) { it.remove(); continue; }
                int w = Math.max(150, Math.max(mc.textRenderer.getWidth(e.title), mc.textRenderer.getWidth(e.message)) + 34);
                int h = 30;
                int x = sw - 8 - (int) (w * Anim.easeOutCubic(p));
                int accent = switch (e.kind) { case SUCCESS -> t.success(); case WARNING -> t.warning(); case ERROR -> t.danger(); default -> t.accent(); };
                if (t.shadows()) Draw.shadow(ctx, x, y, w, h, t.radius(), 90);
                Draw.roundedRect(ctx, x, y, w, h, t.radius(), t.bg1());
                Draw.roundedBorder(ctx, x, y, w, h, t.radius(), t.border());
                ctx.fill(x, y + 6, x + 3, y + h - 6, accent);
                ctx.drawTextWithShadow(mc.textRenderer, "[" + e.title + "]", x + 10, y + 6, accent);
                ctx.drawTextWithShadow(mc.textRenderer, Draw.trim(e.message, w - 18), x + 10, y + 17, t.text());
                // Fortschritt (Restzeit)
                float rest = 1f - Math.min(1f, (float) age / e.duration);
                ctx.fill(x + 10, y + h - 2, x + 10 + (int) ((w - 20) * rest), y + h - 1, Draw.alpha(accent, 120));
                y += h + 6;
            }
        }
    }
}
