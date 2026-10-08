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
    private String wingsId = "";
    private final Map<String, String> playerWings = new HashMap<>();
    private final Map<String, String> remoteWings = new java.util.concurrent.ConcurrentHashMap<>();
    /** Spieler, die in der Cosmetics-API bekannt sind (= nutzen den Chaos Launcher/Client). */
    private final java.util.Set<String> chaosPlayers = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public void markChaos(String uuidKey) { if (uuidKey != null && !uuidKey.isEmpty()) chaosPlayers.add(uuidKey); }

    public boolean isChaosPlayer(UUID uuid) {
        if (uuid == null) return false;
        String key = uuid.toString().replace("-", "").toLowerCase(Locale.ROOT);
        return chaosPlayers.contains(key) || playerHats.containsKey(key) || playerWings.containsKey(key) || playerEffects.containsKey(key)
            || CapeManager.get().config().players.containsKey(key);
    }
    private final Map<String, String> playerHats = new HashMap<>();
    private final Map<String, String> playerEffects = new HashMap<>();
    private final Map<String, String> remoteHats = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, String> remoteEffects = new java.util.concurrent.ConcurrentHashMap<>();

    /** Von der Cosmetics-API/aus dem Cache gemeldete Cosmetics eines anderen Spielers. */
    public void setRemote(String uuidKey, String hat, String effect) { setRemote(uuidKey, hat, effect, ""); }

    public void setRemote(String uuidKey, String hat, String effect, String wings) {
        String h = sanitize(hat), e = sanitize(effect), w = sanitize(wings);
        if (h.isEmpty()) remoteHats.remove(uuidKey); else remoteHats.put(uuidKey, h);
        if (e.isEmpty()) remoteEffects.remove(uuidKey); else remoteEffects.put(uuidKey, e);
        if (w.isEmpty()) remoteWings.remove(uuidKey); else remoteWings.put(uuidKey, w);
    }

    /** Ist der Spieler der eigene Account? */
    public boolean isOwner(UUID uuid) {
        return uuid != null && uuid.toString().replace("-", "").toLowerCase(Locale.ROOT).equals(CapeManager.get().config().ownerUuid);
    }

    private CosmeticsManager() {}

    /** Nach {@link CapeManager#reload} aufrufen. */
    public void reload() {
        CapeManager cm = CapeManager.get();
        CosmeticsConfig cfg = cm.config();
        hatId = sanitize(cfg.hatId);
        effectId = sanitize(cfg.effectId);
        wingsId = sanitize(cfg.wingsId);
        playerWings.clear();
        playerHats.clear();
        playerEffects.clear();
        for (Map.Entry<String, CosmeticsConfig.PlayerCosmetics> e : cfg.playerCosmetics.entrySet()) {
            if (!e.getValue().hat().isEmpty()) playerHats.put(e.getKey(), sanitize(e.getValue().hat()));
            if (!e.getValue().effect().isEmpty()) playerEffects.put(e.getKey(), sanitize(e.getValue().effect()));
            if (!e.getValue().wings().isEmpty()) playerWings.put(e.getKey(), sanitize(e.getValue().wings()));
        }
        // Ingame-Zustand hat Vorrang, wenn neuer als der Export
        Path state = cm.baseDir().resolve("ingame-state.json");
        if (Files.exists(state)) {
            try {
                JsonObject o = GSON.fromJson(Files.readString(state), JsonObject.class);
                if (o != null && o.has("stateAt") && o.get("stateAt").getAsLong() > cm.exportedAt()) {
                    if (o.has("hatId")) hatId = sanitize(o.get("hatId").getAsString());
                    if (o.has("effectId")) effectId = sanitize(o.get("effectId").getAsString());
                    if (o.has("wingsId")) wingsId = sanitize(o.get("wingsId").getAsString());
                }
            } catch (Exception ignored) {}
        }
    }

    public String hatId() { return hatId; }
    public String effectId() { return effectId; }
    public String wingsId() { return wingsId; }

    public String wingsFor(UUID uuid) {
        if (uuid == null) return null;
        String key = uuid.toString().replace("-", "").toLowerCase(Locale.ROOT);
        CosmeticsConfig cfg = CapeManager.get().config();
        if (key.equals(cfg.ownerUuid)) return wingsId.isEmpty() ? null : wingsId;
        String local = playerWings.get(key);
        return local != null ? local : remoteWings.get(key);
    }

    /** Ob exklusive Wings für den eigenen Account freigeschaltet sind. */
    public boolean isUnlocked(String id) {
        WingsCatalog.Wings w = WingsCatalog.byId(id);
        return w == null || !w.exclusive() || CapeManager.get().config().unlocks.contains(id);
    }

    public void setWings(String id) {
        if (!isUnlocked(sanitize(id))) {
            ChaosClient.get().getNotifications().error("Diese Wings sind legendär – Code im Chaos Launcher unter Cosmetics › Wings einlösen.");
            return;
        }
        wingsId = sanitize(id);
        writeState();
        WingsCatalog.Wings w = WingsCatalog.byId(wingsId);
        ChaosClient.get().getNotifications().success(w == null ? "Wings abgelegt." : w.name() + " angelegt.");
    }

    /** Hut-ID eines Spielers (eigener Account oder bekannter Chaos-Spieler), sonst null. */
    public String hatFor(UUID uuid) {
        if (uuid == null) return null;
        String key = uuid.toString().replace("-", "").toLowerCase(Locale.ROOT);
        CosmeticsConfig cfg = CapeManager.get().config();
        if (key.equals(cfg.ownerUuid)) return hatId.isEmpty() ? null : hatId;
        String local = playerHats.get(key);
        return local != null ? local : remoteHats.get(key);
    }

    public String effectFor(UUID uuid) {
        if (uuid == null) return null;
        String key = uuid.toString().replace("-", "").toLowerCase(Locale.ROOT);
        CosmeticsConfig cfg = CapeManager.get().config();
        if (key.equals(cfg.ownerUuid)) return effectId.isEmpty() ? null : effectId;
        String local = playerEffects.get(key);
        return local != null ? local : remoteEffects.get(key);
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
            o.addProperty("wingsId", wingsId);
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
