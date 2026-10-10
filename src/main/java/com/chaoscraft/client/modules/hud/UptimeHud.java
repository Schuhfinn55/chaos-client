package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.EnumSetting;
import net.minecraft.client.gui.DrawContext;

/** Uptime: Spielzeit seit Spielstart bzw. seit dem Serverbeitritt. */
public class UptimeHud extends HudModule {

    public enum Mode { SITZUNG, SERVER }

    private static final long START = System.currentTimeMillis();
    private static long joinedAt = START;

    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Modus", "Seit Spielstart oder seit Serverbeitritt.", Mode.SITZUNG));

    public UptimeHud() {
        super("Uptime", "Wie lange du schon spielst.", Category.HUD, "⏱", 8, 145);
        tags("zeit", "session", "spielzeit", "timer");
    }

    public static void onJoin() { joinedAt = System.currentTimeMillis(); }

    private String line() {
        long ms = System.currentTimeMillis() - (mode.get() == Mode.SERVER ? joinedAt : START);
        long s = ms / 1000, m = s / 60, h = m / 60;
        String t = h > 0 ? String.format(java.util.Locale.ROOT, "%dh %02dm %02ds", h, m % 60, s % 60) : String.format(java.util.Locale.ROOT, "%dm %02ds", m, s % 60);
        return (mode.get() == Mode.SERVER ? "Auf Server: " : "Spielzeit: ") + t;
    }

    @Override public int getContentWidth() { return textWidth(line()); }
    @Override public int getContentHeight() { return 10; }
    @Override public void renderContent(DrawContext ctx, int x, int y) { text(ctx, line(), x, y, getContentWidth()); }
}
