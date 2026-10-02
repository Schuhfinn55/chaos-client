package com.chaoscraft.client.config;

import com.chaoscraft.client.ChaosClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Gemeinsame Daten zwischen Launcher und Ingame-Client. Der Chaos Launcher
 * exportiert beim Start {@code <gamedir>/chaos-client/shared.json}:
 *
 *   { "launcherVersion", "menuKey", "servers":[{name,address,chaoscraft}],
 *     "friends":[{name,uuid}], "musicDir", "profileName", "accountName",
 *     "newsUrl", "chaoscraftAddress" }
 *
 * Alle Felder sind optional; ohne Datei läuft der Client mit Standardwerten.
 */
public final class SharedData {

    public record ServerEntry(String name, String address, boolean chaoscraft) {}
    public record FriendEntry(String name, String uuid) {}

    private static final Gson GSON = new Gson();
    private static SharedData instance = new SharedData();

    public String launcherVersion = "";
    public int menuKey = -1;
    public String musicDir = "";
    public String profileName = "";
    public String accountName = "";
    public String chaoscraftAddress = "";
    public final List<ServerEntry> servers = new ArrayList<>();
    public final List<FriendEntry> friends = new ArrayList<>();
    public boolean present;

    public static SharedData get() { return instance; }

    public static Path dir() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("chaos-client");
    }

    public static void reload() {
        SharedData d = new SharedData();
        Path f = dir().resolve("shared.json");
        if (Files.exists(f)) {
            try (Reader r = Files.newBufferedReader(f)) {
                JsonObject o = GSON.fromJson(r, JsonObject.class);
                if (o != null) {
                    d.present = true;
                    d.launcherVersion = str(o, "launcherVersion");
                    d.menuKey = o.has("menuKey") && o.get("menuKey").isJsonPrimitive() ? o.get("menuKey").getAsInt() : -1;
                    d.musicDir = str(o, "musicDir");
                    d.profileName = str(o, "profileName");
                    d.accountName = str(o, "accountName");
                    d.chaoscraftAddress = str(o, "chaoscraftAddress");
                    if (o.has("servers") && o.get("servers").isJsonArray()) {
                        for (JsonElement e : o.getAsJsonArray("servers")) {
                            if (!e.isJsonObject()) continue;
                            JsonObject s = e.getAsJsonObject();
                            d.servers.add(new ServerEntry(str(s, "name"), str(s, "address"), s.has("chaoscraft") && s.get("chaoscraft").getAsBoolean()));
                        }
                    }
                    if (o.has("friends") && o.get("friends").isJsonArray()) {
                        JsonArray arr = o.getAsJsonArray("friends");
                        for (JsonElement e : arr) {
                            if (!e.isJsonObject()) continue;
                            JsonObject s = e.getAsJsonObject();
                            d.friends.add(new FriendEntry(str(s, "name"), str(s, "uuid")));
                        }
                    }
                }
            } catch (Exception e) {
                ChaosClient.LOGGER.warn("[ChaosClient] shared.json nicht lesbar: {}", e.toString());
            }
        }
        instance = d;
    }

    private static String str(JsonObject o, String k) {
        return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsString() : "";
    }
}
