package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.EnumSetting;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/** CPS Counter: LMB / RMB / beide, Verlauf, höchste CPS, Anzeigeformat. */
public class CpsHud extends HudModule {

    public enum Mode { BOTH, LMB, RMB }
    public enum Format { LABELS, SLASH, VERTICAL }

    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Tasten", "Welche Maustasten gezählt werden.", Mode.BOTH));
    private final EnumSetting<Format> format = add(new EnumSetting<>("Format", "Anzeigeformat.", Format.LABELS));
    private final BooleanSetting showMax = add(new BooleanSetting("Höchste CPS", "Höchsten Wert der Sitzung anzeigen.", false));
    private final BooleanSetting history = add(new BooleanSetting("CPS-Verlauf", "Kleines Balkendiagramm der letzten Sekunden.", false));

    private final Deque<Long> left = new ArrayDeque<>();
    private final Deque<Long> right = new ArrayDeque<>();
    private final int[] hist = new int[20];
    private long lastHist;
    private boolean lmbWas, rmbWas;
    private int maxCps;

    public CpsHud() {
        super("CPS Counter", "Klicks pro Sekunde für linke und rechte Maustaste.", Category.HUD, "◉", 8, 26);
        tags("cps", "clicks", "klicks", "maus");
    }

    /** Vom CpsMixin (doAttack) aufgerufen. */
    public void recordClick() { left.add(System.currentTimeMillis()); }

    @Override
    public void onTick() {
        long h = mc.getWindow().getHandle();
        boolean l = GLFW.glfwGetMouseButton(h, 0) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(h, 1) == GLFW.GLFW_PRESS;
        // LMB wird über den Mixin gezählt; hier nur als Fallback bei Mausklick ohne Angriff (z.B. GUI geschlossen)
        if (r && !rmbWas && mc.currentScreen == null) right.add(System.currentTimeMillis());
        if (l && !lmbWas && mc.currentScreen == null && left.isEmpty()) left.add(System.currentTimeMillis());
        lmbWas = l; rmbWas = r;
        long now = System.currentTimeMillis();
        if (now - lastHist > 500) {
            System.arraycopy(hist, 1, hist, 0, hist.length - 1);
            hist[hist.length - 1] = cps(left) + cps(right);
            lastHist = now;
        }
        maxCps = Math.max(maxCps, Math.max(cps(left), cps(right)));
    }

    private int cps(Deque<Long> q) {
        long now = System.currentTimeMillis();
        while (!q.isEmpty() && now - q.peekFirst() > 1000) q.pollFirst();
        return q.size();
    }

    private String text() {
        int l = cps(left), r = cps(right);
        return switch (mode.get()) {
            case LMB -> format.get() == Format.LABELS ? "LMB " + l + " CPS" : l + " CPS";
            case RMB -> format.get() == Format.LABELS ? "RMB " + r + " CPS" : r + " CPS";
            default -> switch (format.get()) { case SLASH -> l + " / " + r + " CPS"; case VERTICAL -> "LMB " + l; default -> "LMB " + l + "  RMB " + r; };
        };
    }

    @Override public int getContentWidth() { int w = textWidth(text()); if (showMax.isEnabled()) w = Math.max(w, textWidth("max " + maxCps)); return Math.max(w, history.isEnabled() ? 60 : 0); }
    @Override public int getContentHeight() { int h = 10; if (mode.get() == Mode.BOTH && format.get() == Format.VERTICAL) h += 10; if (showMax.isEnabled()) h += 10; if (history.isEnabled()) h += 14; return h; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        int w = getContentWidth();
        text(ctx, text(), x, y, w);
        int cy = y + 10;
        if (mode.get() == Mode.BOTH && format.get() == Format.VERTICAL) { text(ctx, "RMB " + cps(right), x, cy, w); cy += 10; }
        if (showMax.isEnabled()) { text(ctx, "max " + maxCps, x, cy, w, textColor.withAlpha(0.7)); cy += 10; }
        if (history.isEnabled()) {
            int bw = Math.max(2, w / hist.length);
            int peak = 1;
            for (int v : hist) peak = Math.max(peak, v);
            for (int i = 0; i < hist.length; i++) {
                int bh = Math.max(1, hist[i] * 12 / peak);
                ctx.fill(x + i * bw, cy + 12 - bh, x + i * bw + bw - 1, cy + 12, i == hist.length - 1 ? accent() : textColor.withAlpha(0.5));
            }
        }
    }
}
