package com.onyx.visuals.cosmetics;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.onyx.visuals.OnyxVisuals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Chaos-Cosmetics: Cape-Verwaltung im Spiel.
 *
 * Quellen (in dieser Reihenfolge):
 *   1. eigenes Cape          chaos-cosmetics/cape.png        (vom Launcher exportiert)
 *   2. lokal bekannte Spieler chaos-cosmetics/players/<uuid>.png
 *   3. Cache                 chaos-cosmetics/cache/<uuid>.png (von der Cosmetics-API)
 *   4. Cosmetics-API         GET {apiUrl}/v1/cosmetics/<uuid> → cape.url → PNG
 *
 * Spieler ohne Chaos-Cape behalten das normale Minecraft-Verhalten.
 * Texturen werden ausschließlich auf dem Render-Thread registriert.
 */
public final class CapeManager {

    private static final CapeManager INSTANCE = new CapeManager();
    private static final Gson GSON = new Gson();
    private static final String NAMESPACE = "chaosclient";
    private static final long MAX_PNG_BYTES = 4L * 1024 * 1024;

    private final Map<String, Identifier> capes = new ConcurrentHashMap<>();
    private final Set<String> pending = ConcurrentHashMap.newKeySet();
    private final Set<String> negative = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "ChaosCosmetics-Fetch");
        t.setDaemon(true);
        return t;
    });
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private CosmeticsConfig config = new CosmeticsConfig();
    private Path baseDir;
    private boolean initialized;

    private CapeManager() {}

    public static CapeManager get() { return INSTANCE; }

    public boolean isEnabled() { return initialized && config.enabled && config.showCapes; }

    /** Lädt Konfiguration und lokale Capes. Wird beim Client-Start aufgerufen. */
    public void init(MinecraftClient client) {
        baseDir = client.runDirectory.toPath().resolve("chaos-cosmetics");
        reload(client);
    }

    public void reload(MinecraftClient client) {
        capes.clear();
        pending.clear();
        negative.clear();
        config = CosmeticsConfig.load(baseDir);
        initialized = true;
        if (!config.enabled) {
            OnyxVisuals.LOGGER.info("[ChaosCosmetics] deaktiviert (keine/abgeschaltete Konfiguration).");
            return;
        }
        // 1. eigenes Cape
        if (config.showCapes && config.ownCapeFile != null && !config.ownerUuid.isEmpty()) {
            Path own = baseDir.resolve(config.ownCapeFile);
            loadFromFile(client, config.ownerUuid, own, "own");
        }
        // 2. lokal bekannte Spieler
        if (config.showOtherCapes) {
            for (Map.Entry<String, String> e : config.players.entrySet()) {
                loadFromFile(client, e.getKey(), baseDir.resolve(e.getValue()), "player");
            }
        }
        OnyxVisuals.LOGGER.info("[ChaosCosmetics] bereit: {} Cape(s) lokal, API={}", capes.size(),
                config.apiUrl.isEmpty() ? "aus" : "an");
    }

    /**
     * Liefert die Cape-Textur für einen Spieler oder {@code null}
     * (→ normales Minecraft-Verhalten). Unbekannte Spieler werden bei
     * aktivierter API asynchron nachgeladen.
     */
    public Identifier capeFor(UUID uuid) {
        if (uuid == null || !isEnabled()) return null;
        String key = CosmeticsConfig.normalizeUuid(uuid.toString());
        Identifier id = capes.get(key);
        if (id != null) return id;
        if (key.equals(config.ownerUuid)) return null;
        if (!config.showOtherCapes) return null;
        if (negative.contains(key) || pending.contains(key)) return null;
        if (!pending.add(key)) return null;
        executor.submit(() -> resolveRemote(key));
        return null;
    }

    /* ----------------------- Laden ----------------------- */

    private void loadFromFile(MinecraftClient client, String uuidKey, Path file, String tag) {
        if (!Files.exists(file)) return;
        try {
            byte[] bytes = Files.readAllBytes(file);
            registerBytes(client, uuidKey, bytes, tag);
        } catch (IOException e) {
            OnyxVisuals.LOGGER.warn("[ChaosCosmetics] Cape {} nicht lesbar: {}", file, e.toString());
        }
    }

    /** Validiert das PNG und registriert es auf dem Render-Thread. */
    private void registerBytes(MinecraftClient client, String uuidKey, byte[] bytes, String tag) {
        if (!isValidCapePng(bytes)) {
            OnyxVisuals.LOGGER.warn("[ChaosCosmetics] Ungültiges Cape-PNG für {} ({}).", uuidKey, tag);
            negative.add(uuidKey);
            return;
        }
        Runnable task = () -> {
            try {
                NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
                Identifier id = Identifier.of(NAMESPACE, "capes/" + uuidKey);
                NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "chaos_cape_" + uuidKey, image);
                client.getTextureManager().registerTexture(id, tex);
                capes.put(uuidKey, id);
            } catch (Exception e) {
                OnyxVisuals.LOGGER.warn("[ChaosCosmetics] Cape-Textur für {} fehlgeschlagen: {}", uuidKey, e.toString());
                negative.add(uuidKey);
            } finally {
                pending.remove(uuidKey);
            }
        };
        if (client.isOnThread()) task.run();
        else client.execute(task);
    }

    /** Prüft PNG-Signatur und 2:1-Seitenverhältnis (64×32-Vielfache). */
    static boolean isValidCapePng(byte[] b) {
        if (b == null || b.length < 33 || b.length > MAX_PNG_BYTES) return false;
        if ((b[0] & 0xFF) != 0x89 || b[1] != 'P' || b[2] != 'N' || b[3] != 'G') return false;
        int w = ((b[16] & 0xFF) << 24) | ((b[17] & 0xFF) << 16) | ((b[18] & 0xFF) << 8) | (b[19] & 0xFF);
        int h = ((b[20] & 0xFF) << 24) | ((b[21] & 0xFF) << 16) | ((b[22] & 0xFF) << 8) | (b[23] & 0xFF);
        if (w <= 0 || h <= 0 || w > 2048 || h > 1024) return false;
        return w == h * 2 && w % 64 == 0;
    }

    /* ----------------------- API / Cache ----------------------- */

    private void resolveRemote(String uuidKey) {
        MinecraftClient client = MinecraftClient.getInstance();
        try {
            // 3. Cache
            Path cached = baseDir.resolve("cache").resolve(uuidKey + ".png");
            Path meta = baseDir.resolve("cache").resolve(uuidKey + ".json");
            String cachedSha1 = null;
            if (Files.exists(cached) && Files.exists(meta)) {
                try {
                    JsonObject m = GSON.fromJson(Files.readString(meta), JsonObject.class);
                    if (m != null && m.has("sha1")) cachedSha1 = m.get("sha1").getAsString();
                } catch (Exception ignored) {}
            }
            // 4. API
            if (config.apiUrl.isEmpty()) {
                if (Files.exists(cached)) {
                    registerBytes(client, uuidKey, Files.readAllBytes(cached), "cache");
                } else {
                    negative.add(uuidKey);
                    pending.remove(uuidKey);
                }
                return;
            }
            String base = config.apiUrl.replaceAll("/+$", "");
            HttpRequest req = HttpRequest.newBuilder(URI.create(base + "/v1/cosmetics/" + uuidKey))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .header("User-Agent", "chaos-client/" + OnyxVisuals.VERSION)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 404) {
                negative.add(uuidKey);
                pending.remove(uuidKey);
                return;
            }
            if (resp.statusCode() != 200) {
                useCacheOrGiveUp(client, uuidKey, cached);
                return;
            }
            JsonObject body = GSON.fromJson(resp.body(), JsonObject.class);
            if (body == null || !body.has("activeCape") || !body.get("activeCape").isJsonObject()) {
                negative.add(uuidKey);
                pending.remove(uuidKey);
                return;
            }
            String vis = body.has("visibility") ? body.get("visibility").getAsString() : "everyone";
            if ("none".equals(vis)) {
                negative.add(uuidKey);
                pending.remove(uuidKey);
                return;
            }
            JsonObject cape = body.getAsJsonObject("activeCape");
            String url = cape.has("url") ? cape.get("url").getAsString() : "";
            String sha1 = cape.has("sha1") ? cape.get("sha1").getAsString() : "";
            if (url.isEmpty() || !url.startsWith("https://")) {
                useCacheOrGiveUp(client, uuidKey, cached);
                return;
            }
            if (cachedSha1 != null && !sha1.isEmpty() && cachedSha1.equals(sha1) && Files.exists(cached)) {
                registerBytes(client, uuidKey, Files.readAllBytes(cached), "cache");
                return;
            }
            HttpRequest dl = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "chaos-client/" + OnyxVisuals.VERSION)
                    .GET().build();
            HttpResponse<byte[]> png = http.send(dl, HttpResponse.BodyHandlers.ofByteArray());
            if (png.statusCode() != 200 || !isValidCapePng(png.body())) {
                useCacheOrGiveUp(client, uuidKey, cached);
                return;
            }
            Files.createDirectories(cached.getParent());
            Files.write(cached, png.body());
            JsonObject m = new JsonObject();
            m.addProperty("sha1", sha1);
            m.addProperty("cachedAt", System.currentTimeMillis());
            Files.writeString(meta, GSON.toJson(m));
            registerBytes(client, uuidKey, png.body(), "api");
        } catch (Exception e) {
            OnyxVisuals.LOGGER.debug("[ChaosCosmetics] Remote-Cape {} fehlgeschlagen: {}", uuidKey, e.toString());
            negative.add(uuidKey);
            pending.remove(uuidKey);
        }
    }

    private void useCacheOrGiveUp(MinecraftClient client, String uuidKey, Path cached) throws IOException {
        if (Files.exists(cached)) {
            registerBytes(client, uuidKey, Files.readAllBytes(cached), "cache");
        } else {
            negative.add(uuidKey);
            pending.remove(uuidKey);
        }
    }
}
