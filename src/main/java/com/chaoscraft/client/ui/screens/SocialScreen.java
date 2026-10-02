package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.social.SocialManager;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.Label;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

/**
 * Social: Freunde (aus dem Launcher) mit Online-Status, Spieler auf dem
 * Server, Party-Status. Party/Direktnachrichten brauchen die serverseitige
 * Chaoscraft-Komponente und werden als "nicht verfügbar" gekennzeichnet.
 */
public class SocialScreen extends ChaosScreen {

    private ScrollPanel friends;
    private ScrollPanel online;
    private int px, py, pw, ph;

    public SocialScreen(Screen parent) { super("Social", parent); }

    @Override
    protected void build() {
        pw = panelWidth(680);
        ph = panelHeight(460);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        add(new Button(px + pw - 62, py + 13, 50, 20, "Zurück", this::close));
        add(new Button(px + pw - 150, py + 13, 84, 20, "Aktualisieren", this::init));
        SocialManager sm = ChaosClient.get().getSocial();

        int half = pw / 2 - 12;
        friends = add(new ScrollPanel(px + 8, py + 66, half, ph - 110));
        int y = friends.y + 2;
        List<SocialManager.Friend> fl = sm.friends();
        if (fl.isEmpty()) friends.add(new Label(friends.x + 6, y + 4, half - 12, "Keine Freunde – im Launcher unter Extras → Freunde hinzufügen.", theme().textFaint()));
        for (SocialManager.Friend f : fl) {
            friends.add(new Label(friends.x + 6, y + 4, half - 80, (f.online() ? "§a● " : "§8○ ") + f.name(), theme().text()));
            friends.add(new Label(friends.x + half - 70, y + 4, 64, f.online() ? "online · " + f.ping() + "ms" : "offline", f.online() ? theme().success() : theme().textFaint()));
            y += 18;
        }
        friends.setContentHeight(y + 6 - friends.y);

        online = add(new ScrollPanel(px + pw / 2 + 4, py + 66, half, ph - 110));
        y = online.y + 2;
        List<String> players = sm.onlinePlayers();
        if (players.isEmpty()) online.add(new Label(online.x + 6, y + 4, half - 12, "Nicht auf einem Server.", theme().textFaint()));
        for (String p : players) {
            boolean isFriend = sm.isFriend(p);
            online.add(new Label(online.x + 6, y + 4, half - 12, (isFriend ? "§a★ " : "§7") + p, theme().text()));
            y += 14;
        }
        online.setContentHeight(y + 6 - online.y);

        // Party
        SocialManager.PartyState party = sm.party();
        int by = py + ph - 36;
        add(new Button(px + 8, by, 110, 18, "Party erstellen", () -> ChaosClient.get().getNotifications().warn("Party benötigt die Chaoscraft-Server-Integration.")).style(Button.Style.PRIMARY)).enabled = party.available();
        add(new Button(px + 122, by, 90, 18, "Einladen", () -> {})).enabled = party.available();
        add(new Button(px + 216, by, 90, 18, "Verlassen", () -> {}).style(Button.Style.DANGER)).enabled = party.available();
    }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        panel(ctx, px, py, pw, ph);
        header(ctx, px + 16, py + 12, "SOCIAL", null);
        ctx.fill(px + 8, py + 44, px + pw - 8, py + 45, theme().border());
        SocialManager sm = ChaosClient.get().getSocial();
        ctx.drawTextWithShadow(textRenderer, "FREUNDE  §7" + sm.onlineCount() + " online", px + 12, py + 52, theme().accentLight());
        ctx.drawTextWithShadow(textRenderer, "AUF DIESEM SERVER", px + pw / 2 + 8, py + 52, theme().accentLight());
        ctx.fill(px + pw / 2 - 2, py + 50, px + pw / 2 - 1, py + ph - 44, theme().border());
        String party = sm.party().available() ? "Party: " + sm.party().members().size() + " Mitglieder" : "Party & Direktnachrichten: serverseitige Chaoscraft-Komponente erforderlich (noch nicht verfügbar).";
        ctx.drawTextWithShadow(textRenderer, party, px + 12, py + ph - 14, theme().textFaint());
    }
}
