package com.chaoscraft.client.cosmetics;

import com.chaoscraft.client.ChaosClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Chaos-Cosmetics: Cape-Verwaltung im Spiel.
 *
 * Quellen: eigenes Cape (Launcher-Export), Cape-Bibliothek des Accounts
 * (zum Wechseln ingame), lokal bekannte Spieler, Cache, Cosmetics-API.
 * Ein Wechsel ingame wird in {@code ingame-state.json} gespeichert und vom
 * Launcher beim nächsten Start übernommen (gemeinsames System).
 * Texturen werden ausschließlich auf dem Render-Thread registriert.
 */
public final class CapeManager {

    private static final CapeManager INSTANCE = new CapeManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String NAMESPACE = "chaosclient";
    private static final long MAX_PNG_BYTES = 4L * 1024 * 1024;

    private final Map<String, Identifier> capes = new ConcurrentHashMap<>();
    private final Map<String, Identifier> libraryTextures = new ConcurrentHashMap<>();
    /** Animierte Capes (Frame-Streifen): Schlüssel = UUID-Key oder "lib:<id>". */
    public record AnimatedCape(Identifier[] frames, int fps) {}
    private final Map<String, AnimatedCape> animated = new ConcurrentHashMap<>();
    public static final int DEFAULT_FPS = 8;
    private final Set<String> pending = ConcurrentHashMap.newKeySet();
    private final Set<String> negative = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "ChaosCosmetics-Fetch");
        t.setDaemon(true);
        return t;
    });
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NORMAL).build();

    private CosmeticsConfig config = new CosmeticsConfig();
    private Path baseDir;
    private boolean initialized;
    private String activeCapeId = "";
    private boolean showOwn = true;
    private boolean showOthers = true;

    private CapeManager() {}

    public static CapeManager get() { return INSTANCE; }

    public boolean isEnabled() { return initialized && config.enabled; }
    public CosmeticsConfig config() { return config; }
    public String activeCapeId() { return activeCapeId; }
    public Path baseDir() { return baseDir; }
    public void setShowOwn(boolean v) { showOwn = v; }
    public void setShowOthers(boolean v) { showOthers = v; }

    public void init(MinecraftClient client) {
        baseDir = client.runDirectory.toPath().resolve("chaos-cosmetics");
        reload(client);
    }

    public void reload(MinecraftClient client) {
        capes.clear();
        animated.clear();
        libraryTextures.clear();
        pending.clear();
        negative.clear();
        config = CosmeticsConfig.load(baseDir);
        initialized = true;
        activeCapeId = config.activeCapeId;
        // Ingame-Zustand (letzter Wechsel im Spiel) hat Vorrang, wenn der Launcher ihn noch nicht übernommen hat
        Path state = baseDir.resolve("ingame-state.json");
        if (Files.exists(state)) {
            try {
                JsonObject o = GSON.fromJson(Files.readString(state), JsonObject.class);
                if (o != null && o.has("activeCapeId") && o.has("exportedAt") && o.has("stateAt")) {
                    long exported = exportedAt();
                    if (o.get("stateAt").getAsLong() > exported) activeCapeId = o.get("activeCapeId").getAsString();
                }
            } catch (Exception ignored) {}
        }
        if (!config.enabled) {
            ChaosClient.LOGGER.info("[ChaosCosmetics] deaktiviert (keine/abgeschaltete Konfiguration).");
            CosmeticsManager.get().reload();
            return;
        }
        applyOwnCape(client);
        if (config.showOtherCapes) {
            for (Map.Entry<String, String> e : config.players.entrySet()) {
                loadFromFile(client, e.getKey(), baseDir.resolve(e.getValue()), "player", config.playerFps.getOrDefault(e.getKey(), DEFAULT_FPS));
            }
        }
        ChaosClient.LOGGER.info("[ChaosCosmetics] bereit: {} Cape(s) lokal, Bibliothek {}, API={}", capes.size(), config.library.size(), config.apiUrl.isEmpty() ? "aus" : "an");
        CosmeticsManager.get().reload();
    }

    public long exportedAt() {
        try {
            JsonObject o = GSON.fromJson(Files.readString(baseDir.resolve("config.json")), JsonObject.class);
            return o != null && o.has("exportedAt") ? o.get("exportedAt").getAsLong() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    /** Lädt das aktive Cape des eigenen Accounts (aus Bibliothek oder cape.png). */
    private void applyOwnCape(MinecraftClient client) {
        if (config.ownerUuid.isEmpty()) return;
        capes.remove(config.ownerUuid);
        if (activeCapeId.isEmpty()) return;
        CosmeticsConfig.LibraryCape lib = libraryCape(activeCapeId);
        Path file = lib != null && !lib.file().isEmpty() ? baseDir.resolve(lib.file()) : (config.ownCapeFile != null ? baseDir.resolve(config.ownCapeFile) : null);
        if (file != null) loadFromFile(client, config.ownerUuid, file, "own", lib != null ? lib.fps() : config.ownCapeFps);
    }

    public CosmeticsConfig.LibraryCape libraryCape(String id) {
        for (CosmeticsConfig.LibraryCape c : config.library) if (c.id().equals(id)) return c;
        return null;
    }

    public List<CosmeticsConfig.LibraryCape> library() { return new ArrayList<>(config.library); }

    /** Cape ingame wechseln (leer = keins). Wird persistiert und vom Launcher übernommen. */
    public void setActiveCape(String id) {
        MinecraftClient client = MinecraftClient.getInstance();
        activeCapeId = id == null ? "" : id;
        applyOwnCape(client);
        CosmeticsManager.get().writeState();
        ChaosClient.get().getNotifications().success(activeCapeId.isEmpty() ? "Cape deaktiviert." : "Dein Cape wurde aktiviert.");
    }

    /** Textur eines Bibliotheks-Capes (für die Vorschau), lädt bei Bedarf. */
    public Identifier previewTexture(CosmeticsConfig.LibraryCape cape) {
        Identifier id = libraryTextures.get(cape.id());
        if (id != null) return frameNow("lib:" + cape.id(), id);
        Path file = baseDir.resolve(cape.file());
        if (!Files.exists(file)) return null;
        try {
            byte[] bytes = Files.readAllBytes(file);
            if (!isValidCapePng(bytes)) return null;
            NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
            String safe = cape.id().toLowerCase().replaceAll("[^a-z0-9_]", "_");
            Identifier[] frames = registerFrames(MinecraftClient.getInstance(), "library/" + safe, "chaos_lib_" + safe, image);
            libraryTextures.put(cape.id(), frames[0]);
            if (frames.length > 1) animated.put("lib:" + cape.id(), new AnimatedCape(frames, cape.fps()));
            return frames[0];
        } catch (Exception e) {
            return null;
        }
    }

    /** Aktuelles Frame eines (ggf. animierten) Capes. */
    private Identifier frameNow(String key, Identifier base) {
        AnimatedCape a = animated.get(key);
        if (a == null || a.frames().length <= 1) return base;
        int fps = Math.max(1, Math.min(60, a.fps()));
        int idx = (int) ((System.currentTimeMillis() * fps / 1000L) % a.frames().length);
        return a.frames()[idx];
    }

    /** Anzahl der Frames eines Cape-Streifens (Breite:Höhe je Frame = 2:1), 0 = ungültig. */
    public static int frameCount(int w, int h) {
        int fh = w / 2;
        return fh > 0 && h % fh == 0 ? h / fh : 0;
    }

    /** Registriert die Textur(en) eines Capes; Streifen werden in einzelne Frame-Texturen zerlegt. */
    private Identifier[] registerFrames(MinecraftClient client, String baseName, String label, NativeImage image) {
        int w = image.getWidth(), h = image.getHeight();
        int n = Math.max(1, frameCount(w, h));
        if (n == 1) {
            Identifier id = Identifier.of(NAMESPACE, baseName);
            client.getTextureManager().registerTexture(id, new NativeImageBackedTexture(() -> label, image));
            return new Identifier[]{id};
        }
        int fh = w / 2;
        Identifier[] out = new Identifier[n];
        for (int i = 0; i < n; i++) {
            NativeImage f = new NativeImage(w, fh, true);
            for (int y = 0; y < fh; y++) for (int x = 0; x < w; x++) f.setColorArgb(x, y, image.getColorArgb(x, y + i * fh));
            Identifier id = Identifier.of(NAMESPACE, baseName + "_f" + i);
            final int fi = i;
            client.getTextureManager().registerTexture(id, new NativeImageBackedTexture(() -> label + "_f" + fi, f));
            out[i] = id;
        }
        image.close();
        return out;
    }

    /** Cape-Textur für einen Spieler oder null (→ Vanilla-Verhalten). */
    private volatile long lastNegativeReset = System.currentTimeMillis();

    public Identifier capeFor(UUID uuid) {
        if (uuid == null || !isEnabled() || !config.showCapes) return null;
        // Spieler, die (noch) nicht in der API waren, alle 2 Minuten erneut prüfen – z.B. wenn sie gerade erst synchronisiert haben
        long nowMs = System.currentTimeMillis();
        if (nowMs - lastNegativeReset > 120_000L) { lastNegativeReset = nowMs; negative.clear(); }
        String key = CosmeticsConfig.normalizeUuid(uuid.toString());
        boolean own = key.equals(config.ownerUuid);
        if (own && !showOwn) return null;
        if (!own && !showOthers) return null;
        Identifier id = capes.get(key);
        if (id != null) return frameNow(key, id);
        if (own) return null;
        if (negative.contains(key) || pending.contains(key)) return null;
        if (!pending.add(key)) return null;
        executor.submit(() -> resolveRemote(key));
        return null;
    }

    /* ----------------------- Laden ----------------------- */

    private void loadFromFile(MinecraftClient client, String uuidKey, Path file, String tag) { loadFromFile(client, uuidKey, file, tag, DEFAULT_FPS); }

    private void loadFromFile(MinecraftClient client, String uuidKey, Path file, String tag, int fps) {
        if (!Files.exists(file)) return;
        try {
            registerBytes(client, uuidKey, Files.readAllBytes(file), tag, fps);
        } catch (IOException e) {
            ChaosClient.LOGGER.warn("[ChaosCosmetics] Cape {} nicht lesbar: {}", file, e.toString());
        }
    }

    private void registerBytes(MinecraftClient client, String uuidKey, byte[] bytes, String tag) { registerBytes(client, uuidKey, bytes, tag, metaFps(uuidKey)); }

    private void registerBytes(MinecraftClient client, String uuidKey, byte[] bytes, String tag, int fps) {
        if (!isValidCapePng(bytes)) {
            ChaosClient.LOGGER.warn("[ChaosCosmetics] Ungültiges Cape-PNG für {} ({}).", uuidKey, tag);
            negative.add(uuidKey);
            pending.remove(uuidKey);
            return;
        }
        Runnable task = () -> {
            try {
                NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
                Identifier[] frames = registerFrames(client, "capes/" + uuidKey + "_" + Integer.toHexString(java.util.Arrays.hashCode(bytes)), "chaos_cape_" + uuidKey, image);
                capes.put(uuidKey, frames[0]);
                if (frames.length > 1) animated.put(uuidKey, new AnimatedCape(frames, fps)); else animated.remove(uuidKey);
            } catch (Exception e) {
                ChaosClient.LOGGER.warn("[ChaosCosmetics] Cape-Textur für {} fehlgeschlagen: {}", uuidKey, e.toString());
                negative.add(uuidKey);
            } finally {
                pending.remove(uuidKey);
            }
        };
        if (client.isOnThread()) task.run(); else client.execute(task);
    }

    public static boolean isValidCapePng(byte[] b) {
        if (b == null || b.length < 33 || b.length > MAX_PNG_BYTES) return false;
        if ((b[0] & 0xFF) != 0x89 || b[1] != 'P' || b[2] != 'N' || b[3] != 'G') return false;
        int w = ((b[16] & 0xFF) << 24) | ((b[17] & 0xFF) << 16) | ((b[18] & 0xFF) << 8) | (b[19] & 0xFF);
        int h = ((b[20] & 0xFF) << 24) | ((b[21] & 0xFF) << 16) | ((b[22] & 0xFF) << 8) | (b[23] & 0xFF);
        if (w < 64 || h <= 0 || w > 2048 || h > 8192 || w % 64 != 0) return false;
        int frames = frameCount(w, h);
        return frames >= 1 && frames <= 64; // 1 Frame = normales Cape, mehrere = animierter Streifen
    }

    /** FPS aus der Cache-Meta eines Spielers (Standard 8). */
    private int metaFps(String uuidKey) {
        try {
            Path meta = baseDir.resolve("cache").resolve(uuidKey + ".json");
            if (Files.exists(meta)) {
                JsonObject m = GSON.fromJson(Files.readString(meta), JsonObject.class);
                if (m != null && m.has("fps")) return Math.max(1, Math.min(60, m.get("fps").getAsInt()));
            }
        } catch (Exception ignored) {}
        return DEFAULT_FPS;
    }

    /* ----------------------- API / Cache ----------------------- */

    private void resolveRemote(String uuidKey) {
        MinecraftClient client = MinecraftClient.getInstance();
        try {
            Path cached = baseDir.resolve("cache").resolve(uuidKey + ".png");
            Path meta = baseDir.resolve("cache").resolve(uuidKey + ".json");
            String cachedSha1 = null;
            if (Files.exists(cached) && Files.exists(meta)) {
                try {
                    JsonObject m = GSON.fromJson(Files.readString(meta), JsonObject.class);
                    if (m != null && m.has("sha1")) cachedSha1 = m.get("sha1").getAsString();
                } catch (Exception ignored) {}
            }
            // Hut/Effekt aus dem Cache vormerken (falls API nicht erreichbar)
            try {
                if (Files.exists(meta)) {
                    JsonObject m = GSON.fromJson(Files.readString(meta), JsonObject.class);
                    if (m != null) CosmeticsManager.get().setRemote(uuidKey, m.has("hat") ? m.get("hat").getAsString() : "", m.has("effect") ? m.get("effect").getAsString() : "", m.has("wings") ? m.get("wings").getAsString() : "");
                }
            } catch (Exception ignored) {}
            if (config.apiUrl.isEmpty()) { useCacheOrGiveUp(client, uuidKey, cached); return; }
            String base = config.apiUrl.replaceAll("/+$", "");
            HttpRequest req = HttpRequest.newBuilder(URI.create(base + "/v1/cosmetics/" + uuidKey))
                    .timeout(Duration.ofSeconds(10)).header("Accept", "application/json")
                    .header("User-Agent", "chaos-client/" + ChaosClient.VERSION).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 404) { negative.add(uuidKey); pending.remove(uuidKey); return; }
            if (resp.statusCode() != 200) { useCacheOrGiveUp(client, uuidKey, cached); return; }
            JsonObject body = GSON.fromJson(resp.body(), JsonObject.class);
            if (body == null) { negative.add(uuidKey); pending.remove(uuidKey); return; }
            CosmeticsManager.get().markChaos(uuidKey);
            String vis = body.has("visibility") ? body.get("visibility").getAsString() : "everyone";
            if ("none".equals(vis)) { CosmeticsManager.get().setRemote(uuidKey, "", ""); negative.add(uuidKey); pending.remove(uuidKey); return; }
            String rHat = body.has("hat") && !body.get("hat").isJsonNull() ? body.get("hat").getAsString() : "";
            String rEffect = body.has("effect") && !body.get("effect").isJsonNull() ? body.get("effect").getAsString() : "";
            String rWings = body.has("wings") && !body.get("wings").isJsonNull() ? body.get("wings").getAsString() : "";
            CosmeticsManager.get().setRemote(uuidKey, rHat, rEffect, rWings);
            try {
                Files.createDirectories(cached.getParent());
                JsonObject m0 = Files.exists(meta) ? GSON.fromJson(Files.readString(meta), JsonObject.class) : new JsonObject();
                if (m0 == null) m0 = new JsonObject();
                m0.addProperty("hat", rHat);
                m0.addProperty("effect", rEffect);
                m0.addProperty("wings", rWings);
                Files.writeString(meta, GSON.toJson(m0));
            } catch (Exception ignored) {}
            if (!body.has("activeCape") || !body.get("activeCape").isJsonObject()) { negative.add(uuidKey); pending.remove(uuidKey); return; }
            JsonObject cape = body.getAsJsonObject("activeCape");
            String url = cape.has("url") ? cape.get("url").getAsString() : "";
            String sha1 = cape.has("sha1") ? cape.get("sha1").getAsString() : "";
            int rFps = DEFAULT_FPS;
            try { if (cape.has("fps") && !cape.get("fps").isJsonNull()) rFps = Math.max(1, Math.min(60, cape.get("fps").getAsInt())); } catch (Exception ignored) {}
            if (url.isEmpty() || !(url.startsWith("https://") || (config.allowHttp && url.startsWith("http://")))) { useCacheOrGiveUp(client, uuidKey, cached); return; }
            if (cachedSha1 != null && !sha1.isEmpty() && cachedSha1.equals(sha1) && Files.exists(cached)) {
                try { JsonObject mm = GSON.fromJson(Files.readString(meta), JsonObject.class); if (mm != null) { mm.addProperty("fps", rFps); Files.writeString(meta, GSON.toJson(mm)); } } catch (Exception ignored) {}
                registerBytes(client, uuidKey, Files.readAllBytes(cached), "cache", rFps);
                return;
            }
            HttpRequest dl = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(15)).header("User-Agent", "chaos-client/" + ChaosClient.VERSION).GET().build();
            HttpResponse<byte[]> png = http.send(dl, HttpResponse.BodyHandlers.ofByteArray());
            if (png.statusCode() != 200 || !isValidCapePng(png.body())) { useCacheOrGiveUp(client, uuidKey, cached); return; }
            Files.createDirectories(cached.getParent());
            Files.write(cached, png.body());
            JsonObject m = new JsonObject();
            m.addProperty("sha1", sha1);
            m.addProperty("hat", rHat);
            m.addProperty("effect", rEffect);
            m.addProperty("wings", rWings);
            m.addProperty("fps", rFps);
            m.addProperty("cachedAt", System.currentTimeMillis());
            Files.writeString(meta, GSON.toJson(m));
            registerBytes(client, uuidKey, png.body(), "api", rFps);
        } catch (Exception e) {
            ChaosClient.LOGGER.debug("[ChaosCosmetics] Remote-Cape {} fehlgeschlagen: {}", uuidKey, e.toString());
            negative.add(uuidKey);
            pending.remove(uuidKey);
        }
    }

    private void useCacheOrGiveUp(MinecraftClient client, String uuidKey, Path cached) throws IOException {
        if (Files.exists(cached)) registerBytes(client, uuidKey, Files.readAllBytes(cached), "cache");
        else { negative.add(uuidKey); pending.remove(uuidKey); }
    }

    /** Cache leeren (Fremd-Capes werden neu geladen). */
    public void clearCache() {
        negative.clear();
        pending.clear();
        capes.keySet().removeIf(k -> !k.equals(config.ownerUuid) && !config.players.containsKey(k));
    }
}
