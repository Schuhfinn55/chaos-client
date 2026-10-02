package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Effekt-/Potion-HUD: Icon, Name, Stufe, Restzeit, Fortschrittsbalken. Blendet das Vanilla-Overlay aus. */
public class PotionHud extends HudModule {

    private final BooleanSetting icons = add(new BooleanSetting("Icons", "Effekt-Icons anzeigen.", true));
    private final BooleanSetting progress = add(new BooleanSetting("Fortschrittsbalken", "Restzeit als Balken.", true));
    private final BooleanSetting hideVanilla = add(new BooleanSetting("Vanilla ausblenden", "Das normale Effekt-Overlay oben rechts ausblenden.", true));
    private final BooleanSetting colorByType = add(new BooleanSetting("Effektfarbe", "Name in der Effektfarbe.", true));

    private final Map<String, Integer> maxDuration = new HashMap<>();

    public PotionHud() {
        super("Potion Effects", "Aktive Effekte mit Name, Stufe, Zeit und Fortschritt.", Category.HUD, "⚗", 8, 300);
        tags("potion", "effects", "effekte", "trank");
    }

    public boolean hideVanilla() { return isEnabled() && hideVanilla.isEnabled(); }

    private List<StatusEffectInstance> effects() {
        List<StatusEffectInstance> l = new ArrayList<>();
        if (mc.player == null) return l;
        for (StatusEffectInstance e : mc.player.getStatusEffects()) if (e.shouldShowIcon() || true) l.add(e);
        l.sort((a, b) -> Integer.compare(b.getDuration(), a.getDuration()));
        return l;
    }

    private static String roman(int n) { return switch (n) { case 0 -> ""; case 1 -> " II"; case 2 -> " III"; case 3 -> " IV"; case 4 -> " V"; default -> " " + (n + 1); }; }
    private static String time(StatusEffectInstance e) { if (e.isInfinite()) return "∞"; int s = e.getDuration() / 20; return String.format("%d:%02d", s / 60, s % 60); }

    @Override public int getContentWidth() { int w = 90; for (StatusEffectInstance e : effects()) w = Math.max(w, (icons.isEnabled() ? 22 : 0) + textWidth(e.getEffectType().value().getName().getString() + roman(e.getAmplifier())) + 8 + textWidth(time(e))); return w; }
    @Override public int getContentHeight() { int n = effects().size(); return n == 0 ? 10 : n * 22; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        List<StatusEffectInstance> list = effects();
        if (list.isEmpty()) { text(ctx, "Keine Effekte", x, y, getContentWidth(), textColor.withAlpha(0.6)); return; }
        int w = getContentWidth();
        int cy = y;
        for (StatusEffectInstance e : list) {
            String key = e.getEffectType().getIdAsString();
            int max = maxDuration.merge(key, e.getDuration(), Math::max);
            if (e.getDuration() > max) maxDuration.put(key, e.getDuration());
            int tx = x;
            if (icons.isEnabled()) {
                Identifier id = e.getEffectType().getKey().map(k -> Identifier.of(k.getValue().getNamespace(), "textures/mob_effect/" + k.getValue().getPath() + ".png")).orElse(null);
                if (id != null) ctx.drawTexture(RenderPipelines.GUI_TEXTURED, id, tx, cy, 0f, 0f, 18, 18, 18, 18);
                tx += 22;
            }
            int col = colorByType.isEnabled() ? 0xFF000000 | e.getEffectType().value().getColor() : color();
            String name = e.getEffectType().value().getName().getString() + roman(e.getAmplifier());
            ctx.drawText(mc.textRenderer, name, tx, cy + 1, col, textShadow.isEnabled());
            String t = time(e);
            ctx.drawText(mc.textRenderer, t, x + w - textWidth(t), cy + 1, color(), textShadow.isEnabled());
            if (progress.isEnabled() && !e.isInfinite()) {
                float p = max > 0 ? (float) e.getDuration() / max : 1f;
                Draw.roundedRect(ctx, tx, cy + 12, w - (tx - x), 3, 1, Draw.alpha(0xFFFFFF, 40));
                Draw.roundedRect(ctx, tx, cy + 12, Math.max(2, (int) ((w - (tx - x)) * p)), 3, 1, col);
            }
            cy += 22;
        }
    }
}
