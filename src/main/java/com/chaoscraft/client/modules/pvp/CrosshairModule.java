package com.chaoscraft.client.modules.pvp;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.ColorSetting;
import com.chaoscraft.client.settings.EnumSetting;
import com.chaoscraft.client.settings.IntSetting;
import com.chaoscraft.client.ui.Anim;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;

/**
 * Crosshair-Editor: Größe, Stärke, Abstand, Punkt, Linien, Farbe,
 * Transparenz, Outline, Trefferanimation und Presets.
 */
public class CrosshairModule extends Module {

    public enum Preset { CUSTOM, CLASSIC, DOT, PLUS, CIRCLE, PVP, MINIMAL }

    private final EnumSetting<Preset> preset = add(new EnumSetting<>("Preset", "Vorlage auswählen (setzt die Werte unten).", Preset.CLASSIC));
    private final IntSetting size = add(new IntSetting("Größe", "Länge der Linien.", 6, 0, 20));
    private final IntSetting thickness = add(new IntSetting("Stärke", "Linienstärke.", 1, 1, 6));
    private final IntSetting gap = add(new IntSetting("Abstand", "Lücke zur Mitte.", 2, 0, 12));
    private final BooleanSetting dot = add(new BooleanSetting("Punkt", "Mittelpunkt zeichnen.", true));
    private final BooleanSetting lines = add(new BooleanSetting("Linien", "Vier Linien zeichnen.", true));
    private final BooleanSetting circle = add(new BooleanSetting("Kreis", "Kreis um die Mitte.", false));
    private final ColorSetting color = add(new ColorSetting("Farbe", "Farbe mit Transparenz.", 0xFFFFFFFF));
    private final BooleanSetting outline = add(new BooleanSetting("Outline", "Dunkle Kontur für Kontrast.", true));
    private final BooleanSetting hitAnim = add(new BooleanSetting("Trefferanimation", "Kurzes Aufblitzen/Vergrößern bei Treffer.", true));
    private final ColorSetting hitColor = add(new ColorSetting("Trefferfarbe", "Farbe bei Treffer.", 0xFFE11D2E));
    private final BooleanSetting hideVanilla = add(new BooleanSetting("Vanilla ausblenden", "Standard-Fadenkreuz ausblenden.", true));
    private final BooleanSetting hideInThird = add(new BooleanSetting("In 3rd Person ausblenden", "Kein Crosshair in der Außenansicht.", false));

    private Preset lastPreset = Preset.CLASSIC;
    private final Anim hit = new Anim(0f, 10f);
    private int lastHurt;

    public CrosshairModule() {
        super("Crosshair", "Eigenes Fadenkreuz mit Presets, Farbe, Outline und Trefferanimation.", Category.PVP, "✛");
        tags("fadenkreuz", "crosshair", "aim");
        applyPreset(Preset.CLASSIC);
    }

    public boolean hideVanilla() { return isEnabled() && hideVanilla.isEnabled(); }

    /** Treffer registrieren (vom Mixin/Damage-Indicator). */
    public void onHit() { hit.snap(1f); hit.setTarget(0f); }

    private void applyPreset(Preset p) {
        switch (p) {
            case CLASSIC -> { size.setInt(6); thickness.setInt(1); gap.setInt(2); dot.set(true); lines.set(true); circle.set(false); }
            case DOT -> { size.setInt(0); thickness.setInt(2); gap.setInt(0); dot.set(true); lines.set(false); circle.set(false); }
            case PLUS -> { size.setInt(5); thickness.setInt(1); gap.setInt(0); dot.set(false); lines.set(true); circle.set(false); }
            case CIRCLE -> { size.setInt(3); thickness.setInt(1); gap.setInt(4); dot.set(true); lines.set(false); circle.set(true); }
            case PVP -> { size.setInt(4); thickness.setInt(2); gap.setInt(3); dot.set(true); lines.set(true); circle.set(false); }
            case MINIMAL -> { size.setInt(3); thickness.setInt(1); gap.setInt(3); dot.set(false); lines.set(true); circle.set(false); }
            default -> {}
        }
    }

    @Override
    public void onTick() {
        if (preset.get() != lastPreset) { lastPreset = preset.get(); applyPreset(lastPreset); }
        if (hitAnim.isEnabled() && mc.targetedEntity instanceof LivingEntity le) {
            if (le.hurtTime > 0 && le.hurtTime != lastHurt && le.hurtTime >= 9) onHit();
            lastHurt = le.hurtTime;
        }
    }

    /** Vom HudRenderer jeden Frame aufgerufen. */
    public void render(DrawContext ctx) {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof com.chaoscraft.client.ui.screens.HudEditorScreen)) return;
        if (hideInThird.isEnabled() && !mc.options.getPerspective().isFirstPerson()) return;
        int cx = ctx.getScaledWindowWidth() / 2, cy = ctx.getScaledWindowHeight() / 2;
        float h = hitAnim.isEnabled() ? hit.get() : 0f;
        int col = Draw.mix(color.argb(), hitColor.argb(), h);
        int s = size.getInt() + Math.round(h * 2), t = thickness.getInt(), g = gap.getInt() + Math.round(h * 2);
        int t0 = -t / 2, t1 = (t + 1) / 2;
        if (outline.isEnabled()) {
            int oc = Draw.alpha(0x000000, (color.alpha() * 3) / 4);
            if (lines.isEnabled() && s > 0) {
                ctx.fill(cx - g - s - 1, cy + t0 - 1, cx - g + 1, cy + t1 + 1, oc);
                ctx.fill(cx + g - 1, cy + t0 - 1, cx + g + s + 1, cy + t1 + 1, oc);
                ctx.fill(cx + t0 - 1, cy - g - s - 1, cx + t1 + 1, cy - g + 1, oc);
                ctx.fill(cx + t0 - 1, cy + g - 1, cx + t1 + 1, cy + g + s + 1, oc);
            }
            if (dot.isEnabled()) ctx.fill(cx + t0 - 1, cy + t0 - 1, cx + t1 + 1, cy + t1 + 1, oc);
        }
        if (lines.isEnabled() && s > 0) {
            ctx.fill(cx - g - s, cy + t0, cx - g, cy + t1, col);
            ctx.fill(cx + g, cy + t0, cx + g + s, cy + t1, col);
            ctx.fill(cx + t0, cy - g - s, cx + t1, cy - g, col);
            ctx.fill(cx + t0, cy + g, cx + t1, cy + g + s, col);
        }
        if (dot.isEnabled()) ctx.fill(cx + t0, cy + t0, cx + t1, cy + t1, col);
        if (circle.isEnabled()) {
            int r = g + s;
            for (int a = 0; a < 360; a += 10) {
                int px = cx + (int) Math.round(Math.cos(Math.toRadians(a)) * r), py = cy + (int) Math.round(Math.sin(Math.toRadians(a)) * r);
                ctx.fill(px, py, px + 1, py + 1, col);
            }
        }
    }
}
