package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.compat.Compat;
import com.chaoscraft.client.hud.HudRenderer;
import com.chaoscraft.client.modules.performance.PerformanceModule;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import com.chaoscraft.client.ui.widgets.SettingWidgets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.lang.management.ManagementFactory;

/**
 * Performance Center: Live-Werte (FPS, Frametime, Speicher, CPU, GPU, Chunks,
 * Entities, Partikel) + Performance Mode (LOW/BALANCED/HIGH/CUSTOM) +
 * einzelne Optionen. Es wird nichts geändert, was Gameplay oder Serverregeln
 * beeinflusst – nur Grafik-/Render-Einstellungen.
 */
public class PerformanceScreen extends ChaosScreen {

    private ScrollPanel options;
    private int px, py, pw, ph;
    private String gpu = "";

    public PerformanceScreen(Screen parent) { super("Performance", parent); }

    private PerformanceModule mod() { return ChaosClient.get().getModuleManager().get(PerformanceModule.class); }

    @Override
    protected void build() {
        pw = panelWidth(720);
        ph = panelHeight(480);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        add(new Button(px + pw - 62, py + 13, 50, 20, "Zurück", this::close));
        try { gpu = RenderSystem.getDevice().getRenderer(); } catch (Exception e) { gpu = "?"; }
        PerformanceModule m = mod();
        int bx = px + 8, by = py + 150;
        for (PerformanceModule.Mode mode : PerformanceModule.Mode.values()) {
            final PerformanceModule.Mode md = mode;
            add(new Button(bx, by, 80, 20, mode.name(), () -> { m.applyMode(md); init(); }).style(m.mode() == mode ? Button.Style.PRIMARY : Button.Style.DEFAULT).tooltip(mode.description()));
            bx += 84;
        }
        options = add(new ScrollPanel(px + 8, py + 178, pw - 16, ph - 192));
        int y = SettingWidgets.build(options, m, options.x + 8, options.y + 4, options.w - 24, false);
        options.setContentHeight(y + 8 - options.y);
    }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        panel(ctx, px, py, pw, ph);
        header(ctx, px + 16, py + 12, "PERFORMANCE", null);
        ctx.fill(px + 8, py + 44, px + pw - 8, py + 45, theme().border());
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L, max = rt.maxMemory() / 1048576L;
        double cpu = -1;
        try {
            var os = ManagementFactory.getOperatingSystemMXBean();
            if (os instanceof com.sun.management.OperatingSystemMXBean sun) cpu = sun.getProcessCpuLoad() * 100;
        } catch (Throwable ignored) {}
        String[][] stats = {
            {"FPS", client.getCurrentFps() + ""},
            {"Frametime", String.format(java.util.Locale.ROOT, "%.1f ms", HudRenderer.frameMs())},
            {"Memory", used + " / " + max + " MB"},
            {"CPU", cpu < 0 ? "n/a" : String.format(java.util.Locale.ROOT, "%.0f %%", cpu)},
            {"GPU", Draw.trim(gpu, 150)},
            {"Chunks", client.worldRenderer != null ? Draw.trim(client.worldRenderer.getChunksDebugString(), 150) : "-"},
            {"Entities", client.world != null ? client.world.getRegularEntityCount() + "" : "-"},
            {"Particles", Draw.trim(client.particleManager.getDebugString(), 150)},
        };
        int cols = 4, cw = (pw - 16) / cols;
        for (int i = 0; i < stats.length; i++) {
            int cx = px + 8 + (i % cols) * cw, cy = py + 52 + (i / cols) * 44;
            Draw.roundedRect(ctx, cx, cy, cw - 6, 40, 6, theme().bg2());
            ctx.drawTextWithShadow(textRenderer, stats[i][0].toUpperCase(), cx + 8, cy + 6, theme().textFaint());
            ctx.drawTextWithShadow(textRenderer, Draw.trim(stats[i][1], cw - 22), cx + 8, cy + 20, theme().accentLight());
        }
        ctx.drawTextWithShadow(textRenderer, "PERFORMANCE MODE  §8· aktiv: §f" + mod().mode().name() + (Compat.hasSodium() ? "  §8· Sodium erkannt" : ""), px + 12, py + 140, theme().accentLight());
        String t = options.tooltipAt(mx, my);
        if (t != null) setTooltip(t, mx, my);
    }

    @Override protected void renderOverlays(DrawContext ctx, int mx, int my) { options.renderOverlays(ctx, mx, my); }
}
