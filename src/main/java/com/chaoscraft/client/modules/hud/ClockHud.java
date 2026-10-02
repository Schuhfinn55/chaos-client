package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Uhr: Echtzeit, optional Sekunden/Datum und Minecraft-Tageszeit. */
public class ClockHud extends HudModule {

    private final BooleanSetting seconds = add(new BooleanSetting("Sekunden", "Sekunden anzeigen.", false));
    private final BooleanSetting date = add(new BooleanSetting("Datum", "Datum anzeigen.", false));
    private final BooleanSetting gameTime = add(new BooleanSetting("Spielzeit", "Minecraft-Tageszeit anzeigen.", false));

    public ClockHud() {
        super("Clock", "Echte Uhrzeit und optional die Minecraft-Tageszeit.", Category.MISC, "◷", 8, 360);
        tags("clock", "uhr", "zeit", "time");
    }

    private String now() { return LocalDateTime.now().format(DateTimeFormatter.ofPattern(seconds.isEnabled() ? "HH:mm:ss" : "HH:mm")); }
    private String dateStr() { return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")); }
    private String mcTime() {
        if (mc.world == null) return "";
        long t = (mc.world.getTimeOfDay() + 6000) % 24000;
        return String.format("☀ %02d:%02d", t / 1000, (t % 1000) * 60 / 1000);
    }

    @Override public int getContentWidth() { int w = textWidth(now()); if (date.isEnabled()) w = Math.max(w, textWidth(dateStr())); if (gameTime.isEnabled()) w = Math.max(w, textWidth(mcTime())); return w; }
    @Override public int getContentHeight() { return 10 + (date.isEnabled() ? 10 : 0) + (gameTime.isEnabled() ? 10 : 0); }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        int w = getContentWidth(), cy = y;
        text(ctx, now(), x, cy, w); cy += 10;
        if (date.isEnabled()) { text(ctx, dateStr(), x, cy, w, textColor.withAlpha(0.7)); cy += 10; }
        if (gameTime.isEnabled()) text(ctx, mcTime(), x, cy, w, textColor.withAlpha(0.7));
    }
}
