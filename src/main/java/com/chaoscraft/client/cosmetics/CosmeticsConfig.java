package com.chaoscraft.client.cosmetics;

import com.chaoscraft.client.ChaosClient;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Vom Chaos Launcher exportierte Cosmetics-Konfiguration
 * ({@code <gamedir>/chaos-cosmetics/config.json}).
 *
 * Felder: version, enabled, showCapes, showOtherCapes, autoLoadCapes, apiUrl,
 * ownerUuid, ownerName, activeCape{id,name,file,sha1}, visibility,
 * players{uuid -> {cape, sha1}}, library[{id,name,file,sha1,source}].
 */
public final class CosmeticsConfig {

    public record LibraryCape(String id, String name, String file, String sha1, String source) {}
    /** Cosmetics eines anderen Spielers (Cape-Datei relativ zu chaos-cosmetics/, Hut-ID, Effekt-ID). */
    public record PlayerCosmetics(String cape, String hat, String effect) {}

    private static final Gson GSON = new Gson();

    public boolean enabled = true;
    public boolean showCapes = true;
    public boolean showOtherCapes = true;
    public boolean autoLoadCapes = true;
    public String apiUrl = "";
    public String ownerUuid = "";
    public String ownerName = "";
    public String activeCapeId = "";
    public String ownCapeFile = null;
    public String ownCapeSha1 = "";
    public String visibility = "everyone";
    public int version = 1;
    public final Map<String, String> players = new HashMap<>();
    public final Map<String, PlayerCosmetics> playerCosmetics = new HashMap<>();
    public final List<LibraryCape> library = new ArrayList<>();
    public String hatId = "";
    public String effectId = "";

    public static CosmeticsConfig load(Path dir) {
        CosmeticsConfig cfg = new CosmeticsConfig();
        Path file = dir.resolve("config.json");
        if (!Files.exists(file)) { cfg.enabled = false; return cfg; }
        try (Reader r = Files.newBufferedReader(file)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) { cfg.enabled = false; return cfg; }
            cfg.version = getInt(root, "version", 1);
            cfg.enabled = getBool(root, "enabled", true);
            cfg.showCapes = getBool(root, "showCapes", true);
            cfg.showOtherCapes = getBool(root, "showOtherCapes", true);
            cfg.autoLoadCapes = getBool(root, "autoLoadCapes", true);
            cfg.apiUrl = getStr(root, "apiUrl", "");
            cfg.ownerUuid = normalizeUuid(getStr(root, "ownerUuid", ""));
            cfg.ownerName = getStr(root, "ownerName", "");
            cfg.visibility = getStr(root, "visibility", "everyone");
            cfg.hatId = getStr(root, "hat", "");
            cfg.effectId = getStr(root, "effect", "");
            if (root.has("activeCape") && root.get("activeCape").isJsonObject()) {
                JsonObject ac = root.getAsJsonObject("activeCape");
                cfg.activeCapeId = getStr(ac, "id", "");
                cfg.ownCapeFile = getStr(ac, "file", "cape.png");
                cfg.ownCapeSha1 = getStr(ac, "sha1", "");
            }
            if (root.has("players") && root.get("players").isJsonObject()) {
                for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("players").entrySet()) {
                    if (e.getValue().isJsonObject()) {
                        JsonObject po = e.getValue().getAsJsonObject();
                        String cape = getStr(po, "cape", "");
                        String key = normalizeUuid(e.getKey());
                        if (!cape.isEmpty()) cfg.players.put(key, cape);
                        cfg.playerCosmetics.put(key, new PlayerCosmetics(cape, getStr(po, "hat", ""), getStr(po, "effect", "")));
                    }
                }
            }
            if (root.has("library") && root.get("library").isJsonArray()) {
                for (JsonElement e : root.getAsJsonArray("library")) {
                    if (!e.isJsonObject()) continue;
                    JsonObject c = e.getAsJsonObject();
                    cfg.library.add(new LibraryCape(getStr(c, "id", ""), getStr(c, "name", "Cape"), getStr(c, "file", ""), getStr(c, "sha1", ""), getStr(c, "source", "custom")));
                }
            }
        } catch (Exception ex) {
            ChaosClient.LOGGER.warn("[ChaosCosmetics] config.json konnte nicht gelesen werden: {}", ex.toString());
            cfg.enabled = false;
        }
        return cfg;
    }

    public static String normalizeUuid(String s) {
        return s == null ? "" : s.replace("-", "").toLowerCase(Locale.ROOT);
    }

    private static boolean getBool(JsonObject o, String k, boolean d) { return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsBoolean() : d; }
    private static int getInt(JsonObject o, String k, int d) { return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsInt() : d; }
    private static String getStr(JsonObject o, String k, String d) { return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsString() : d; }
}
