package com.chaoscraft.client.cosmetics;

import com.chaoscraft.client.ChaosClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Hüte und Effekte: eigener Zustand (aus der Launcher-Konfiguration bzw.
 * dem Ingame-Wechsel) und Zuordnung für andere Spieler. Schreibt zusammen
 * mit dem Cape den gemeinsamen {@code ingame-state.json}, den der Launcher
 * beim nächsten Start/Öffnen übernimmt.
 */
public final class CosmeticsManager {

    private static final CosmeticsManager INSTANCE = new CosmeticsManager();
    private static final Gson GSON = new Gson();

    public static CosmeticsManager get() { return INSTANCE; }

    private String hatId = "";
    private String effectId = "";
    private final Map<String, String> playerHats = new HashMap<>();
    private final Map<String, String> playerEffects = new HashMap<>();

    private CosmeticsManager() {}

    /** Nach {@link CapeManager#reload} aufrufen. */
    public void reload() {
        CapeManager cm = CapeManager.get();
        CosmeticsConfig cfg = cm.config();
        hatId = sanitize(cfg.hatId);
        effectId = sanitize(cfg.effectId);
        playerHats.clear();
        playerEffects.clear();
        for (Map.Entry<String, CosmeticsConfig.PlayerCosmetics> e : cfg.playerCosmetics.entrySet()) {
            if (!e.getValue().hat().isEmpty()) playerHats.put(e.getKey(), sanitize(e.getValue().hat()));
            if (!e.getValue().effect().isEmpty()) playerEffects.put(e.getKey(), sanitize(e.getValue().effect()));
        }
        // Ingame-Zustand hat Vorrang, wenn neuer als der Export
        Path state = cm.baseDir().resolve("ingame-state.json");
        if (Files.exists(state)) {
            try {
                JsonObject o = GSON.fromJson(Files.readString(state), JsonObject.class);
                if (o != null && o.has("stateAt") && o.get("stateAt").getAsLong() > cm.exportedAt()) {
                    if (o.has("hatId")) hatId = sanitize(o.get("hatId").getAsString());
                    if (o.has("effectId")) effectId = sanitize(o.get("effectId").getAsString());
                }
            } catch (Exception ignored) {}
        }
    }

    public String hatId() { return hatId; }
    public String effectId() { return effectId; }

    /** Hut-ID eines Spielers (eigener Account oder bekannter Chaos-Spieler), sonst null. */
    public String hatFor(UUID uuid) {
        if (uuid == null) return null;
        String key = uuid.toString().replace("-", "").toLowerCase(Locale.ROOT);
        CosmeticsConfig cfg = CapeManager.get().config();
        if (key.equals(cfg.ownerUuid)) return hatId.isEmpty() ? null : hatId;
        return playerHats.get(key);
    }

    public String effectFor(UUID uuid) {
        if (uuid == null) return null;
        String key = uuid.toString().replace("-", "").toLowerCase(Locale.ROOT);
        CosmeticsConfig cfg = CapeManager.get().config();
        if (key.equals(cfg.ownerUuid)) return effectId.isEmpty() ? null : effectId;
        return playerEffects.get(key);
    }

    public void setHat(String id) {
        hatId = sanitize(id);
        writeState();
        HatCatalog.Hat h = HatCatalog.byId(hatId);
        ChaosClient.get().getNotifications().success(h == null ? "Hut abgenommen." : h.name() + " aufgesetzt.");
    }

    public void setEffect(String id) {
        effectId = sanitize(id);
        writeState();
        EffectCatalog.Effect e = EffectCatalog.byId(effectId);
        ChaosClient.get().getNotifications().success(e == null ? "Effekt entfernt." : "Effekt " + e.name() + " aktiviert.");
    }

    /** Gemeinsamer Ingame-Zustand (Cape + Hut + Effekt) für den Launcher. */
    public void writeState() {
        CapeManager cm = CapeManager.get();
        try {
            JsonObject o = new JsonObject();
            o.addProperty("activeCapeId", cm.activeCapeId());
            o.addProperty("hatId", hatId);
            o.addProperty("effectId", effectId);
            o.addProperty("ownerUuid", cm.config().ownerUuid);
            o.addProperty("stateAt", System.currentTimeMillis());
            o.addProperty("exportedAt", cm.exportedAt());
            Files.createDirectories(cm.baseDir());
            Files.writeString(cm.baseDir().resolve("ingame-state.json"), GSON.toJson(o));
        } catch (IOException e) {
            ChaosClient.LOGGER.warn("[ChaosCosmetics] ingame-state.json: {}", e.toString());
        }
    }

    /** Nur harmlose IDs (a-z, 0-9, -, _), max. 40 Zeichen. */
    public static String sanitize(String id) {
        if (id == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : id.toLowerCase(Locale.ROOT).toCharArray()) {
            if (Character.isLetterOrDigit(c) && c < 128 || c == '-' || c == '_') sb.append(c);
            if (sb.length() >= 40) break;
        }
        return sb.toString();
    }
}
