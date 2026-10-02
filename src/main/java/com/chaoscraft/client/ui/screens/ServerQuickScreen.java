package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.config.SharedData;
import com.chaoscraft.client.server.ServerManager;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.Label;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

/** Server-Schnellmenü: Chaoscraft SMP, Lobby, Citybuild, PvP, gespeicherte Server – sofort beitreten. */
public class ServerQuickScreen extends ChaosScreen {

    private ScrollPanel list;
    private int px, py, pw, ph;

    public ServerQuickScreen(Screen parent) { super("Server", parent); }

    @Override
    protected void build() {
        pw = panelWidth(420);
        ph = panelHeight(380);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        add(new Button(px + pw - 62, py + 13, 50, 20, "Zurück", this::close));
        ServerManager sm = ChaosClient.get().getServers();
        list = add(new ScrollPanel(px + 8, py + 70, pw - 16, ph - 84));
        int y = list.y + 2, x = list.x + 6, w = list.w - 20;
        var servers = sm.servers();
        if (servers.isEmpty()) {
            list.add(new Label(x, y + 4, w, "Keine Server. Im Launcher unter Server hinzufügen.", theme().textFaint()));
        }
        for (SharedData.ServerEntry s : servers) {
            final SharedData.ServerEntry entry = s;
            boolean current = sm.currentAddress().equalsIgnoreCase(s.address());
            list.add(new Label(x, y + 3, w - 90, (s.chaoscraft() ? "✸ " : "▤ ") + s.name(), s.chaoscraft() ? theme().accentLight() : theme().text()));
            list.add(new Label(x, y + 13, w - 90, s.address(), theme().textFaint()));
            list.add(new Button(x + w - 80, y + 3, 76, 18, current ? "Verbunden" : "Beitreten", () -> sm.join(entry)).style(s.chaoscraft() ? Button.Style.PRIMARY : Button.Style.DEFAULT)).enabled = !current;
            y += 28;
        }
        list.setContentHeight(y + 6 - list.y);
    }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        panel(ctx, px, py, pw, ph);
        header(ctx, px + 16, py + 12, "SERVER", null);
        ctx.fill(px + 8, py + 44, px + pw - 8, py + 45, theme().border());
        ServerManager sm = ChaosClient.get().getServers();
        String cur = sm.currentAddress().isEmpty() ? "Nicht verbunden" : sm.currentName() + " §8· §7" + sm.currentAddress() + " §8· §7" + sm.ping() + "ms · TPS " + sm.tps() + " · " + sm.playerCount() + " Spieler";
        ctx.drawTextWithShadow(textRenderer, Draw.trim(cur, pw - 24), px + 12, py + 52, theme().textDim());
    }
}
