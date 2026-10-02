package com.chaoscraft.client.social;

import com.chaoscraft.client.config.SharedData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Social: Freundesliste aus dem Launcher-Export, Online-Erkennung über die
 * Spielerliste des aktuellen Servers. Party und Direktnachrichten benötigen
 * eine serverseitige Chaoscraft-Komponente und sind hier als Schnittstelle
 * vorbereitet (siehe {@link PartyState}).
 */
public final class SocialManager {

    public record Friend(String name, String uuid, boolean online, int ping) {}

    /** Party-Zustand (serverseitig abgesichert; ohne Server: unavailable). */
    public record PartyState(boolean available, String leader, List<String> members) {
        public static PartyState unavailable() { return new PartyState(false, "", List.of()); }
    }

    private PartyState party = PartyState.unavailable();

    public List<Friend> friends() {
        List<Friend> out = new ArrayList<>();
        MinecraftClient mc = MinecraftClient.getInstance();
        for (SharedData.FriendEntry f : SharedData.get().friends) {
            boolean online = false;
            int ping = -1;
            if (mc.getNetworkHandler() != null) {
                for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList()) {
                    if (e.getProfile().name().equalsIgnoreCase(f.name())) { online = true; ping = e.getLatency(); break; }
                }
            }
            out.add(new Friend(f.name(), f.uuid(), online, ping));
        }
        out.sort((a, b) -> a.online() == b.online() ? a.name().compareToIgnoreCase(b.name()) : (a.online() ? -1 : 1));
        return out;
    }

    public int onlineCount() { return (int) friends().stream().filter(Friend::online).count(); }

    public PartyState party() { return party; }

    /** Wird von einer späteren Server-Integration gesetzt. */
    public void setParty(PartyState p) { party = p == null ? PartyState.unavailable() : p; }

    /** Spieler auf dem aktuellen Server (für „Freund hinzufügen“ im Launcher). */
    public List<String> onlinePlayers() {
        List<String> out = new ArrayList<>();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null) return out;
        for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList()) out.add(e.getProfile().name());
        out.sort(String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    public boolean isFriend(String name) {
        for (SharedData.FriendEntry f : SharedData.get().friends) if (f.name().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))) return true;
        return false;
    }
}
