package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.ColorSetting;
import com.chaoscraft.client.settings.DoubleSetting;
import com.chaoscraft.client.settings.IntSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Eigene Scoreboard-Darstellung (Sidebar): Hintergrund, Transparenz,
 * Skalierung, Position, Farben, Zeilenabstand, Rahmen, Schatten, Zahlen
 * ausblenden. Vanilla-Sidebar wird per Mixin ersetzt.
 */
public class ScoreboardModule extends Module {

    private final DoubleSetting scale = add(new DoubleSetting("Scale", "Größe.", 1.0, 0.5, 2.0, 0.05));
    private final IntSetting offsetX = add(new IntSetting("Abstand rechts", "Abstand vom rechten Rand.", 4, 0, 200));
    private final IntSetting offsetY = add(new IntSetting("Position Y", "Vertikale Verschiebung von der Mitte.", 0, -200, 200));
    private final IntSetting lineSpacing = add(new IntSetting("Zeilenabstand", "Zusätzlicher Abstand pro Zeile.", 1, 0, 6));
    private final ColorSetting bgColor = add(new ColorSetting("Hintergrund", "Hintergrundfarbe (mit Transparenz).", 0x80000000));
    private final ColorSetting titleBg = add(new ColorSetting("Titel-Hintergrund", "Farbe hinter dem Titel.", 0xA0E11D2E));
    private final ColorSetting textColor = add(new ColorSetting("Textfarbe", "Standardfarbe für Zeilen ohne eigene Farbe.", 0xFFFFFFFF, false));
    private final BooleanSetting showNumbers = add(new BooleanSetting("Zahlen", "Punktzahlen rechts anzeigen.", false));
    private final BooleanSetting border = add(new BooleanSetting("Rahmen", "Rahmen in Akzentfarbe.", true));
    private final BooleanSetting shadow = add(new BooleanSetting("Schatten", "Weicher Schatten.", true));
    private final IntSetting radius = add(new IntSetting("Eckenradius", "Rundung.", 4, 0, 10));

    public ScoreboardModule() {
        super("Scoreboard", "Modernes Scoreboard im Chaos-Design statt der Vanilla-Sidebar.", Category.HUD, "▥");
        tags("sidebar", "scoreboard", "punkte");
    }

    /** Vom InGameHudMixin aufgerufen; true = Vanilla nicht zeichnen. */
    public boolean render(DrawContext ctx, ScoreboardObjective objective) {
        if (!isEnabled() || objective == null || mc.world == null) return false;
        Scoreboard sb = mc.world.getScoreboard();
        List<ScoreboardEntry> entries = new ArrayList<>();
        for (ScoreboardEntry e : sb.getScoreboardEntries(objective)) if (!e.hidden()) entries.add(e);
        entries.sort(Comparator.comparingInt(ScoreboardEntry::value).reversed().thenComparing(ScoreboardEntry::owner));
        if (entries.size() > 15) entries = entries.subList(0, 15);

        var font = mc.textRenderer;
        Text title = objective.getDisplayName();
        List<Text> names = new ArrayList<>();
        List<String> scores = new ArrayList<>();
        int w = font.getWidth(title) + 8;
        for (ScoreboardEntry e : entries) {
            Team team = sb.getScoreHolderTeam(e.owner());
            Text name = Team.decorateName(team, e.name());
            names.add(name);
            String sc = showNumbers.isEnabled() ? e.formatted(objective.getNumberFormatOr(StyledNumberFormat.RED)).getString() : "";
            scores.add(sc);
            w = Math.max(w, font.getWidth(name) + (sc.isEmpty() ? 0 : font.getWidth(sc) + 10) + 8);
        }
        int lh = font.fontHeight + lineSpacing.getInt();
        int h = (entries.size() + 1) * lh + 6;
        float s = scale.getFloat();
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        int x = (int) (sw - w * s) - offsetX.getInt();
        int y = (int) (sh / 2 - h * s / 2) + offsetY.getInt();

        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(s, s);
        int r = radius.getInt();
        if (shadow.isEnabled()) Draw.shadow(ctx, 0, 0, w, h, r, 100);
        Draw.roundedRect(ctx, 0, 0, w, h, r, bgColor.argb());
        Draw.roundedRect(ctx, 0, 0, w, lh + 3, r, titleBg.argb());
        if (border.isEnabled()) Draw.roundedBorder(ctx, 0, 0, w, h, r, com.chaoscraft.client.config.ChaosTheme.get().accentGlow(200));
        ctx.drawTextWithShadow(font, title, (w - font.getWidth(title)) / 2, 3, 0xFFFFFFFF);
        int cy = lh + 5;
        for (int i = 0; i < names.size(); i++) {
            ctx.drawTextWithShadow(font, names.get(i), 4, cy, textColor.argb());
            String sc = scores.get(i);
            if (!sc.isEmpty()) ctx.drawTextWithShadow(font, sc, w - 4 - font.getWidth(sc), cy, 0xFFFF5555);
            cy += lh;
        }
        m.popMatrix();
        return true;
    }

    /** Ermittelt wie Vanilla das aktive Sidebar-Objective (Team-Farbe zuerst). */
    public ScoreboardObjective currentObjective() {
        if (mc.world == null || mc.player == null) return null;
        Scoreboard sb = mc.world.getScoreboard();
        Team team = sb.getScoreHolderTeam(mc.player.getNameForScoreboard());
        if (team != null && team.getColor().getColorValue() != null) {
            ScoreboardDisplaySlot slot = ScoreboardDisplaySlot.fromFormatting(team.getColor());
            if (slot != null) {
                ScoreboardObjective o = sb.getObjectiveForSlot(slot);
                if (o != null) return o;
            }
        }
        return sb.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
    }
}
