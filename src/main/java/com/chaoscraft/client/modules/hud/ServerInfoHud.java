package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.server.ServerManager;
import com.chaoscraft.client.settings.BooleanSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** Kombiniertes Info-HUD: FPS, Ping, Server-TPS, Server-IP/-Name, Spieler. */
public class ServerInfoHud extends HudModule {

    private final BooleanSetting fps = add(new BooleanSetting("FPS", "Bildrate anzeigen.", true));
    private final BooleanSetting ping = add(new BooleanSetting("Ping", "Latenz anzeigen.", true));
    private final BooleanSetting tps = add(new BooleanSetting("TPS", "Geschätzte Server-TPS anzeigen.", true));
    private final BooleanSetting server = add(new BooleanSetting("Server", "Servername/IP anzeigen.", false));
    private final BooleanSetting players = add(new BooleanSetting("Spieler", "Spieler online anzeigen.", false));

    public ServerInfoHud() {
        super("Server Info", "FPS, Ping, TPS, Server-IP und Spielerzahl in einem HUD.", Category.HUD, "▤", 8, 40);
        tags("ping", "tps", "server", "latency", "ip");
    }

    private List<String> lines() {
        List<String> l = new ArrayList<>();
        ServerManager sm = ChaosClient.get().getServers();
        if (fps.isEnabled()) l.add("FPS " + mc.getCurrentFps());
        if (ping.isEnabled() && sm.ping() >= 0) l.add("PING " + sm.ping() + "ms");
        if (tps.isEnabled() && mc.getNetworkHandler() != null) l.add("TPS " + sm.tps());
        if (server.isEnabled() && !sm.currentAddress().isEmpty()) l.add(sm.currentAddress());
        if (players.isEnabled() && mc.getNetworkHandler() != null) l.add(sm.playerCount() + " Spieler");
        if (l.isEmpty()) l.add("Server Info");
        return l;
    }

    private int pingColor(int p) { return p < 60 ? 0xFF4ADE80 : p < 150 ? 0xFFFBBF24 : 0xFFF87171; }

    @Override public int getContentWidth() { int w = 0; for (String s : lines()) w = Math.max(w, textWidth(s)); return w; }
    @Override public int getContentHeight() { return lines().size() * 10; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        int w = getContentWidth();
        int cy = y;
        for (String s : lines()) {
            int c = color();
            if (s.startsWith("PING")) c = pingColor(ChaosClient.get().getServers().ping());
            else if (s.startsWith("TPS")) c = ChaosClient.get().getServers().tps() >= 19 ? 0xFF4ADE80 : ChaosClient.get().getServers().tps() >= 15 ? 0xFFFBBF24 : 0xFFF87171;
            text(ctx, s, x, cy, w, c);
            cy += 10;
        }
    }
}
