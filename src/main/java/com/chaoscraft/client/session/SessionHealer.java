package com.chaoscraft.client.session;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.config.SharedData;
import com.chaoscraft.client.mixin.MinecraftClientAccessor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Selbstheilung bei „Ungültige Sitzung“: holt vom laufenden Chaos Launcher
 * (lokale Bridge, Port/Geheimnis aus shared.json) ein frisches Minecraft-Token,
 * tauscht die Spielsitzung aus und verbindet automatisch neu – ohne Neustart.
 */
public final class SessionHealer {
    private SessionHealer() {}

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private static final AtomicBoolean BUSY = new AtomicBoolean(false);
    private static long lastHeal;

    /** Letzter Verbindungsversuch (von ConnectScreenMixin gesetzt). */
    public static volatile ServerAddress lastAddress;
    public static volatile ServerInfo lastInfo;

    public static void rememberConnect(ServerAddress address, ServerInfo info) {
        lastAddress = address;
        lastInfo = info;
    }

    /** Erkennt die Vanilla-Meldung „Ungültige Sitzung“ in jeder Sprache (Übersetzungsschlüssel) oder im Text. */
    public static boolean isInvalidSession(Text reason) {
        if (reason == null) return false;
        if (hasKey(reason, "disconnect.loginFailedInfo.invalidSession")) return true;
        String s = reason.getString().toLowerCase();
        return s.contains("invalid session") || s.contains("ungültige sitzung") || s.contains("invalidsession");
    }

    private static boolean hasKey(Text t, String key) {
        if (t.getContent() instanceof TranslatableTextContent tc) {
            if (key.equals(tc.getKey())) return true;
            for (Object a : tc.getArgs()) if (a instanceof Text at && hasKey(at, key)) return true;
        }
        for (Text s : t.getSiblings()) if (hasKey(s, key)) return true;
        return false;
    }

    /** Wird vom DisconnectedScreen-Mixin aufgerufen. */
    public static void onDisconnect(Text reason) {
        if (!isInvalidSession(reason)) return;
        SharedData sd = SharedData.get();
        if (sd.bridgePort <= 0 || sd.bridgeSecret.isEmpty()) {
            ChaosClient.get().getNotifications().warn("Sitzung abgelaufen – bitte den Chaos Launcher starten und das Spiel neu starten.");
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastHeal < 30_000 || !BUSY.compareAndSet(false, true)) return;
        lastHeal = now;
        ChaosClient.get().getNotifications().info("Sitzung abgelaufen – hole neues Token vom Launcher …");
        Thread t = new Thread(() -> {
            try {
                MinecraftClient mc = MinecraftClient.getInstance();
                String uuid = mc.getSession().getUuidOrNull() != null ? mc.getSession().getUuidOrNull().toString().replace("-", "") : "";
                String url = "http://127.0.0.1:" + sd.bridgePort + "/session?secret=" + sd.bridgeSecret + "&uuid=" + uuid + "&force=1";
                HttpResponse<String> resp = HTTP.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(25)).GET().build(), HttpResponse.BodyHandlers.ofString());
                JsonObject o = GSON.fromJson(resp.body(), JsonObject.class);
                if (resp.statusCode() != 200 || o == null || !o.has("accessToken") || o.get("accessToken").getAsString().isEmpty()) {
                    String err = o != null && o.has("error") ? o.get("error").getAsString() : "HTTP " + resp.statusCode();
                    mc.execute(() -> ChaosClient.get().getNotifications().error("Token-Erneuerung fehlgeschlagen: " + err));
                    return;
                }
                String token = o.get("accessToken").getAsString();
                String name = o.has("name") ? o.get("name").getAsString() : mc.getSession().getUsername();
                mc.execute(() -> {
                    if (applySession(mc, token, name)) {
                        ChaosClient.get().getNotifications().success("Sitzung erneuert – verbinde neu …");
                        reconnect(mc);
                    }
                });
            } catch (Exception e) {
                ChaosClient.LOGGER.warn("[ChaosClient] Session-Heilung fehlgeschlagen: {}", e.toString());
                MinecraftClient.getInstance().execute(() -> ChaosClient.get().getNotifications().error("Launcher nicht erreichbar – Spiel bitte über den Chaos Launcher neu starten."));
            } finally {
                BUSY.set(false);
            }
        }, "ChaosSessionHeal");
        t.setDaemon(true);
        t.start();
    }

    /** Tauscht Session, UserApiService und Profil-Schlüssel (Chat-Signierung) gegen das neue Token. */
    public static boolean applySession(MinecraftClient mc, String token, String name) {
        try {
            Session old = mc.getSession();
            UUID uuid = old.getUuidOrNull();
            Session fresh = new Session(name, uuid, token, old.getClientId(), old.getXuid());
            MinecraftClientAccessor acc = (MinecraftClientAccessor) mc;
            acc.chaos$setSession(fresh);
            UserApiService api;
            try {
                api = new YggdrasilAuthenticationService(mc.getNetworkProxy()).createUserApiService(token);
            } catch (Exception e) {
                api = UserApiService.OFFLINE;
            }
            acc.chaos$setUserApiService(api);
            try {
                acc.chaos$setProfileKeys(ProfileKeys.create(api, fresh, mc.runDirectory.toPath()));
            } catch (Exception e) {
                acc.chaos$setProfileKeys(ProfileKeys.MISSING);
            }
            ChaosClient.LOGGER.info("[ChaosClient] Spielsitzung erneuert für {}.", name);
            return true;
        } catch (Throwable e) {
            ChaosClient.LOGGER.warn("[ChaosClient] Sitzung konnte nicht getauscht werden: {}", e.toString());
            ChaosClient.get().getNotifications().error("Sitzung konnte nicht getauscht werden – bitte Spiel neu starten.");
            return false;
        }
    }

    private static void reconnect(MinecraftClient mc) {
        ServerAddress addr = lastAddress;
        ServerInfo info = lastInfo;
        if (addr == null && info != null) addr = ServerAddress.parse(info.address);
        if (addr == null) {
            mc.setScreen(new MultiplayerScreen(new TitleScreen()));
            return;
        }
        if (info == null) info = new ServerInfo(addr.getAddress(), addr.getAddress() + ":" + addr.getPort(), ServerInfo.ServerType.OTHER);
        ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), mc, addr, info, false, null);
    }

    /** Optional: Token vorsorglich vor dem Verbinden erneuern lassen (Launcher prüft Ablauf selbst). */
    public static Optional<String> describe() {
        SharedData sd = SharedData.get();
        return sd.bridgePort > 0 ? Optional.of("Launcher-Bridge 127.0.0.1:" + sd.bridgePort) : Optional.empty();
    }
}
