package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

/**
 * Tablist-Verbesserungen: Ping als Zahl (farbig) statt Balken, Freunde
 * markieren. Weitere Darstellung (Köpfe, Rang, Team) bleibt Vanilla-nah,
 * damit Server-Tablists nicht zerstört werden.
 */
public class TablistModule extends Module {

    private final BooleanSetting pingNumbers = add(new BooleanSetting("Ping als Zahl", "Zeigt die Latenz als farbige Zahl.", true));
    private final BooleanSetting markFriends = add(new BooleanSetting("Freunde markieren", "Freunde aus dem Launcher mit ★ kennzeichnen.", true));

    public TablistModule() {
        super("Tablist", "Ping-Zahlen und Freundesmarkierung in der Spielerliste.", Category.HUD, "☷");
        tags("tab", "spielerliste", "ping", "playerlist");
    }

    /** Vom PlayerListHudMixin aufgerufen; true = Vanilla-Balken nicht zeichnen. */
    public boolean renderLatency(DrawContext ctx, int width, int x, int y, PlayerListEntry entry) {
        if (!isEnabled() || !pingNumbers.isEnabled()) return false;
        int p = entry.getLatency();
        String s = p < 0 ? "?" : p + "ms";
        int col = p < 0 ? 0xFF888888 : p < 60 ? 0xFF4ADE80 : p < 150 ? 0xFFFBBF24 : 0xFFF87171;
        var font = mc.textRenderer;
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(x + width - 2, y);
        m.scale(0.75f, 0.75f);
        ctx.drawTextWithShadow(font, s, -font.getWidth(s), 1, col);
        m.popMatrix();
        if (markFriends.isEnabled() && com.chaoscraft.client.ChaosClient.get().getSocial().isFriend(entry.getProfile().name())) {
            ctx.drawTextWithShadow(font, "★", x + width - 2 - (int) (font.getWidth(s) * 0.75f) - 8, y, Draw.alpha(0xFFFCD34D, 255));
        }
        return true;
    }
}
