package com.chaoscraft.client.emotes;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.config.SharedData;
import com.chaoscraft.client.cosmetics.CosmeticsManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.Vec3d;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Emote-System: Ganzkörper-Animationen ({@link EmoteAnimations}), gespielt über das
 * Emote-Rad (Taste B), Direkttasten oder /chaos emote. Das eigene Emote wird über die
 * Launcher-Bridge an die Cosmetics-API gemeldet; Emotes anderer Chaos-Spieler in der Nähe
 * werden alle 2 s abgefragt und auf deren Modell abgespielt. Bewegung bricht das Emote ab.
 */
public final class EmoteManager {

    /** Basisschnittstelle für Emotes. */
    public interface Emote {
        String id();
        String name();
        String description();
        /** Dauer in Ticks. */
        int duration();
        void tick(MinecraftClient mc, int tick);
        default void onEnd(MinecraftClient mc) {}
    }

    /** IDs der eingebauten Emotes – für die vorab registrierten Keybinds im EmotesModule. */
    public static final List<String> BUILTIN_IDS = EmoteAnimations.ALL.stream().map(EmoteAnimations.Animated::id).toList();

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private static final float FADE = 0.18f;

    private record Playing(EmoteAnimations.Animated emote, long startMs, long key) {}

    private final List<Emote> emotes = new ArrayList<>(EmoteAnimations.ALL);
    private final Map<UUID, Playing> playing = new ConcurrentHashMap<>();
    private final Map<UUID, Long> seenKeys = new ConcurrentHashMap<>();
    private Perspective restorePerspective;
    private Vec3d startPos;
    private long lastPoll;
    private volatile boolean polling;

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) { playing.clear(); return; }
        long now = System.currentTimeMillis();
        // Eigenes Emote: Ende / Abbruch durch Bewegung
        Playing own = playing.get(mc.player.getUuid());
        if (own != null) {
            boolean moved = startPos != null && mc.player.getEntityPos().squaredDistanceTo(startPos) > 0.02 || mc.player.input != null && (mc.player.input.getMovementInput().lengthSquared() > 0.01 || mc.player.input.playerInput.jump());
            float t = (now - own.startMs) / 1000f;
            if (moved || (!own.emote.loop() && t > own.emote.seconds() + FADE)) stopOwn(mc);
        }
        // Fremde Emotes auslaufen lassen
        playing.entrySet().removeIf(e -> {
            if (e.getKey().equals(mc.player.getUuid())) return false;
            float t = (now - e.getValue().startMs) / 1000f;
            return e.getValue().emote.loop() ? t > 12f : t > e.getValue().emote.seconds() + FADE;
        });
        // Fremde Emotes abfragen
        if (now - lastPoll > 2000 && !polling) {
            lastPoll = now;
            List<String> near = new ArrayList<>();
            for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
                if (p == mc.player || p.squaredDistanceTo(mc.player) > 48 * 48) continue;
                if (CosmeticsManager.get().isChaosPlayer(p.getUuid())) near.add(p.getUuid().toString().replace("-", ""));
            }
            if (!near.isEmpty()) pollRemote(mc, near);
        }
    }

    /** Pose eines Spielers für den aktuellen Frame oder null (keine Animation). */
    public EmotePose poseFor(UUID uuid) {
        Playing p = playing.get(uuid);
        if (p == null) return null;
        float t = (System.currentTimeMillis() - p.startMs) / 1000f;
        float dur = p.emote.seconds();
        float tt = p.emote.loop() ? t % dur : Math.min(t, dur);
        EmotePose pose = p.emote.poseAt(tt);
        // Ein-/Ausblenden gegen die Nullpose
        float w = 1f;
        if (t < FADE) w = t / FADE;
        else if (!p.emote.loop() && t > dur - FADE) w = Math.max(0, (dur + FADE - t) / (2 * FADE));
        if (w < 1f) pose = EmotePose.mix(new EmotePose(), pose, Math.max(0, Math.min(1, w)));
        return pose;
    }

    public List<Emote> all() { return emotes; }
    public Emote active() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return null;
        Playing p = playing.get(mc.player.getUuid());
        return p == null ? null : p.emote;
    }
    public boolean isPlaying(UUID uuid) { return playing.containsKey(uuid); }
    public void add(Emote e) { emotes.add(e); }

    public Emote byId(String id) {
        for (Emote e : emotes) if (e.id().equalsIgnoreCase(id) || e.name().equalsIgnoreCase(id)) return e;
        return null;
    }

    public void play(Emote e) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !(e instanceof EmoteAnimations.Animated a)) return;
        long now = System.currentTimeMillis();
        playing.put(mc.player.getUuid(), new Playing(a, now, now));
        startPos = mc.player.getEntityPos();
        // Automatisch in die dritte Person, damit man sich selbst sieht
        if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            restorePerspective = Perspective.FIRST_PERSON;
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        }
        ChaosClient.get().getNotifications().info(a.icon() + " " + a.name());
        sendOwn(a.id());
    }

    public void stop() { stopOwn(MinecraftClient.getInstance()); }

    private void stopOwn(MinecraftClient mc) {
        if (mc.player == null) return;
        if (playing.remove(mc.player.getUuid()) != null && restorePerspective != null) {
            mc.options.setPerspective(restorePerspective);
        }
        restorePerspective = null;
        startPos = null;
    }

    /* ---------------- Sync über Launcher-Bridge / Cosmetics-API ---------------- */

    private void sendOwn(String id) {
        SharedData sd = SharedData.get();
        if (sd.bridgePort <= 0 || sd.bridgeSecret.isEmpty()) return;
        Thread t = new Thread(() -> {
            try {
                String url = "http://127.0.0.1:" + sd.bridgePort + "/emote?secret=" + sd.bridgeSecret + "&id=" + id;
                HTTP.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(6)).GET().build(), HttpResponse.BodyHandlers.discarding());
            } catch (Exception ignored) {}
        }, "ChaosEmoteSend");
        t.setDaemon(true);
        t.start();
    }

    private void pollRemote(MinecraftClient mc, List<String> uuids) {
        String api = com.chaoscraft.client.cosmetics.CapeManager.get().config().apiUrl;
        if (api == null || api.isEmpty()) return;
        polling = true;
        Thread t = new Thread(() -> {
            try {
                String body = "{\"uuids\":[" + String.join(",", uuids.stream().map(u -> "\"" + u + "\"").toList()) + "]}";
                HttpRequest req = HttpRequest.newBuilder(URI.create(api.replaceAll("/+$", "") + "/v1/emotes/bulk"))
                    .timeout(Duration.ofSeconds(5)).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
                HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() != 200) return;
                JsonObject o = GSON.fromJson(resp.body(), JsonObject.class);
                if (o == null) return;
                for (String key : o.keySet()) {
                    JsonObject e = o.getAsJsonObject(key);
                    if (e == null || !e.has("id")) continue;
                    long at = e.has("at") ? e.get("at").getAsLong() : 0;
                    UUID uuid = toUuid(key);
                    if (uuid == null || uuid.equals(mc.player.getUuid())) continue;
                    if (seenKeys.getOrDefault(uuid, 0L) >= at) continue;
                    Emote em = byId(e.get("id").getAsString());
                    if (em instanceof EmoteAnimations.Animated a) {
                        seenKeys.put(uuid, at);
                        playing.put(uuid, new Playing(a, System.currentTimeMillis(), at));
                    }
                }
            } catch (Exception ignored) {
            } finally {
                polling = false;
            }
        }, "ChaosEmotePoll");
        t.setDaemon(true);
        t.start();
    }

    private static UUID toUuid(String s) {
        try {
            String h = s.replace("-", "").toLowerCase(Locale.ROOT);
            if (h.length() != 32) return null;
            return UUID.fromString(h.substring(0, 8) + "-" + h.substring(8, 12) + "-" + h.substring(12, 16) + "-" + h.substring(16, 20) + "-" + h.substring(20));
        } catch (Exception e) {
            return null;
        }
    }

    /** Einfaches Emote aus Lambda (Kompatibilität). */
    public static final class Simple implements Emote {
        public interface Ticker { void tick(MinecraftClient mc, int t); }
        private final String id, name, description;
        private final int duration;
        private final Ticker ticker;
        public Simple(String id, String name, String description, int duration, Ticker ticker) {
            this.id = id; this.name = name; this.description = description; this.duration = duration; this.ticker = ticker;
        }
        @Override public String id() { return id; }
        @Override public String name() { return name; }
        @Override public String description() { return description; }
        @Override public int duration() { return duration; }
        @Override public void tick(MinecraftClient mc, int t) { ticker.tick(mc, t); }
    }
}
