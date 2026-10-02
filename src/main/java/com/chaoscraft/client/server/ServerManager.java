package com.chaoscraft.client.server;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.config.SharedData;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-Infos: aktueller Server, Ping, geschätzte TPS (aus dem Fortschritt
 * der Weltzeit), Spielerzahl, Schnellverbindung zu gespeicherten Servern.
 */
public final class ServerManager {

    private long lastWorldTime = -1;
    private long lastSampleNanos = System.nanoTime();
    private double tps = 20.0;
    private int ticksSinceSample = 0;

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) { lastWorldTime = -1; tps = 20.0; return; }
            ticksSinceSample++;
            long now = System.nanoTime();
            long elapsed = now - lastSampleNanos;
            if (elapsed >= 2_000_000_000L) {
                long wt = client.world.getTime();
                if (lastWorldTime >= 0) {
                    long ticks = wt - lastWorldTime;
                    double seconds = elapsed / 1_000_000_000.0;
                    double est = ticks / seconds;
                    if (ticks >= 0 && ticks < 400) tps = tps * 0.5 + Math.min(20.0, est) * 0.5;
                }
                lastWorldTime = wt;
                lastSampleNanos = now;
                ticksSinceSample = 0;
            }
        });
    }

    public double tps() { return Math.round(tps * 10.0) / 10.0; }

    public String currentAddress() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getCurrentServerEntry() != null) return mc.getCurrentServerEntry().address;
        return mc.isInSingleplayer() ? "Singleplayer" : "";
    }

    public String currentName() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getCurrentServerEntry() != null) return mc.getCurrentServerEntry().name;
        return mc.isInSingleplayer() ? "Einzelspieler" : "";
    }

    public boolean isOnChaoscraft() {
        String a = currentAddress().toLowerCase();
        String c = SharedData.get().chaoscraftAddress.toLowerCase();
        return !c.isEmpty() && a.contains(c.split(":")[0]) || a.contains("chaoscraft");
    }

    public int ping() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null || mc.player == null) return -1;
        PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return e != null ? e.getLatency() : -1;
    }

    public int playerCount() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null) return 0;
        return mc.getNetworkHandler().getPlayerList().size();
    }

    /** Gespeicherte Server: Launcher-Export + Chaoscraft zuerst. */
    public List<SharedData.ServerEntry> servers() {
        List<SharedData.ServerEntry> out = new ArrayList<>();
        SharedData sd = SharedData.get();
        if (!sd.chaoscraftAddress.isEmpty() && sd.servers.stream().noneMatch(SharedData.ServerEntry::chaoscraft)) {
            out.add(new SharedData.ServerEntry("Chaoscraft SMP", sd.chaoscraftAddress, true));
        }
        out.addAll(sd.servers);
        return out;
    }

    /** Verbindet mit einem Server (trennt ggf. die aktuelle Verbindung). */
    public void join(SharedData.ServerEntry entry) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.execute(() -> {
            try {
                if (mc.world != null) {
                    mc.world.disconnect(net.minecraft.text.Text.literal("Chaos Client: Serverwechsel"));
                    mc.disconnectWithProgressScreen();
                }
                ServerInfo info = new ServerInfo(entry.name(), entry.address(), ServerInfo.ServerType.OTHER);
                ConnectScreen.connect(new TitleScreen(), mc, ServerAddress.parse(entry.address()), info, false, null);
            } catch (Exception e) {
                ChaosClient.LOGGER.warn("[ChaosClient] Verbindung zu {} fehlgeschlagen: {}", entry.address(), e.toString());
                ChaosClient.get().getNotifications().error("Verbindung fehlgeschlagen: " + entry.address());
            }
        });
    }
}
