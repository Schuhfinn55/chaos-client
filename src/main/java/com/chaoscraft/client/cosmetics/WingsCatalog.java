package com.chaoscraft.client.cosmetics;

import com.chaoscraft.client.ChaosClient;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Wings-Katalog aus {@code assets/chaosclient/wings.json} (gleiche Datei wie
 * im Launcher). Jeder Flügel ist eine flache Textur-Ebene
 * ({@code textures/wings/<id>.png}, 2 px pro Einheit, links gespiegelt für die
 * Nordseite, rechts Original für die Südseite).
 */
public final class WingsCatalog {
    private WingsCatalog() {}

    public record Wings(String id, String name, String description, String icon, Identifier texture,
                        float flapSpeed, float flapAmp, float openAngle, float tilt, float scale,
                        boolean glow, String particle, List<Integer> colors, int frames, int fps, boolean exclusive) {
        /** Textur des aktuellen Animations-Frames (statische Wings: immer die Haupttextur). */
        public Identifier frameTexture(long nowMs) {
            if (frames <= 1) return texture;
            int i = (int) ((nowMs * Math.max(1, fps) / 1000L) % frames);
            return Identifier.of(ChaosClient.MOD_ID, "textures/wings/" + id + "_f" + i + ".png");
        }
    }

    private static final Map<String, Wings> WINGS = new LinkedHashMap<>();
    private static float rootX = 2f, rootY = 1.5f, rootZ = 2.3f;
    private static float planeW = 26f, planeH = 30f, planeTop = 16f, texW = 64f, texH = 32f;
    private static boolean loaded;

    private static float f(JsonObject o, String k, float def) { return o.has(k) ? o.get(k).getAsFloat() : def; }
    private static String s(JsonObject o, String k, String def) { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : def; }

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;
        try (InputStream in = WingsCatalog.class.getResourceAsStream("/assets/chaosclient/wings.json")) {
            if (in == null) { ChaosClient.LOGGER.warn("[ChaosCosmetics] wings.json fehlt"); return; }
            JsonObject root = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            if (root.has("root")) {
                JsonObject r = root.getAsJsonObject("root");
                rootX = f(r, "x", rootX); rootY = f(r, "y", rootY); rootZ = f(r, "z", rootZ);
            }
            if (root.has("plane")) {
                JsonObject p = root.getAsJsonObject("plane");
                planeW = f(p, "w", planeW); planeH = f(p, "h", planeH); planeTop = f(p, "top", planeTop);
                texW = f(p, "texW", texW); texH = f(p, "texH", texH);
            }
            for (JsonElement e : root.getAsJsonArray("wings")) {
                JsonObject w = e.getAsJsonObject();
                String id = w.get("id").getAsString();
                List<Integer> colors = new ArrayList<>();
                if (w.has("colors")) for (JsonElement c : w.getAsJsonArray("colors")) {
                    try { colors.add((int) Long.parseLong(c.getAsString().replace("#", ""), 16)); } catch (Exception ignored) {}
                }
                Wings wings = new Wings(id, s(w, "name", id), s(w, "description", ""), s(w, "icon", "✦"),
                    Identifier.of(ChaosClient.MOD_ID, "textures/wings/" + id + ".png"),
                    f(w, "flapSpeed", 0.08f), f(w, "flapAmp", 18f), f(w, "openAngle", 38f), f(w, "tilt", 10f), f(w, "scale", 1f),
                    w.has("glow") && w.get("glow").getAsBoolean(), s(w, "particle", ""), colors,
                    (int) f(w, "frames", 1f), (int) f(w, "fps", 10f), w.has("exclusive") && w.get("exclusive").getAsBoolean());
                WINGS.put(id, wings);
            }
            ChaosClient.LOGGER.info("[ChaosCosmetics] {} Wings geladen.", WINGS.size());
        } catch (Exception ex) {
            ChaosClient.LOGGER.warn("[ChaosCosmetics] wings.json konnte nicht gelesen werden: {}", ex.toString());
        }
    }

    public static List<Wings> all() { load(); return List.copyOf(WINGS.values()); }
    public static Wings byId(String id) { load(); return id == null ? null : WINGS.get(id); }
    public static float rootX() { load(); return rootX; }
    public static float rootY() { load(); return rootY; }
    public static float rootZ() { load(); return rootZ; }
    public static float planeW() { load(); return planeW; }
    public static float planeH() { load(); return planeH; }
    public static float planeTop() { load(); return planeTop; }
    public static int texW() { load(); return (int) texW; }
    public static int texH() { load(); return (int) texH; }
}
