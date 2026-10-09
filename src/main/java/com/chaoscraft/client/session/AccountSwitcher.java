package com.chaoscraft.client.session;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.config.SharedData;
import com.chaoscraft.client.cosmetics.CapeManager;
import com.chaoscraft.client.cosmetics.CosmeticsManager;
import com.chaoscraft.client.ui.screens.AccountSwitcherScreen;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Account-Wechsel im Spiel über die Launcher-Bridge: Accounts des Chaos Launchers und in
 * anderen Launchern gefundene Accounts anzeigen, Sitzung ohne Neustart tauschen,
 * Microsoft-Login (Device-Code) direkt aus dem Spiel starten.
 */
public final class AccountSwitcher {
    private AccountSwitcher() {}

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    /** Button oben links im Titelbildschirm und in der Serverliste. */
    public static void onScreenInit(Screen screen) {
        if (!(screen instanceof TitleScreen) && !(screen instanceof MultiplayerScreen)) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        String name = mc.getSession().getUsername();
        Text label = Text.literal("§c✸ §f" + name + " §7▾");
        int w = Math.max(96, mc.textRenderer.getWidth(label) + 18);
        ButtonWidget b = ButtonWidget.builder(label, btn -> mc.setScreen(new AccountSwitcherScreen(screen))).dimensions(4, 4, w, 20).build();
        b.setTooltip(Tooltip.of(Text.literal("Account wechseln – Chaos Launcher")));
        Screens.getButtons(screen).add(b);
    }

    public static boolean available() {
        SharedData sd = SharedData.get();
        return sd.bridgePort > 0 && !sd.bridgeSecret.isEmpty();
    }

    private static String base() {
        SharedData sd = SharedData.get();
        return "http://127.0.0.1:" + sd.bridgePort + "/";
    }

    private static String enc(String s) { return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8); }

    /** GET an die Bridge (Hintergrund-Thread); Ergebnis als JsonObject oder Fehler ("error"-Feld). */
    public static void call(String path, String params, Consumer<JsonObject> onResult) {
        Thread t = new Thread(() -> {
            JsonObject out;
            try {
                if (!available()) throw new IllegalStateException("Chaos Launcher nicht erreichbar – Spiel bitte über den Chaos Launcher starten.");
                String url = base() + path + "?secret=" + SharedData.get().bridgeSecret + (params.isEmpty() ? "" : "&" + params);
                HttpResponse<String> resp = HTTP.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30)).GET().build(), HttpResponse.BodyHandlers.ofString());
                out = GSON.fromJson(resp.body(), JsonObject.class);
                if (out == null) out = new JsonObject();
                if (resp.statusCode() != 200 && !out.has("error")) out.addProperty("error", "HTTP " + resp.statusCode());
            } catch (Exception e) {
                out = new JsonObject();
                out.addProperty("error", e.getMessage() == null ? e.toString() : e.getMessage());
            }
            JsonObject result = out;
            MinecraftClient.getInstance().execute(() -> onResult.accept(result));
        }, "ChaosAccountBridge");
        t.setDaemon(true);
        t.start();
    }

    public static void listAccounts(Consumer<JsonObject> cb) { call("accounts", "", cb); }
    public static void switchTo(String uuid, Consumer<JsonObject> cb) { call("switch", "uuid=" + enc(uuid), cb); }
    public static void useForeign(String source, String uuid, Consumer<JsonObject> cb) { call("foreign/use", "source=" + enc(source) + "&uuid=" + enc(uuid), cb); }
    public static void loginStart(Consumer<JsonObject> cb) { call("login/start", "", cb); }
    public static void loginStatus(String deviceCode, Consumer<JsonObject> cb) { call("login/status", "code=" + enc(deviceCode), cb); }

    /** Sitzung aus einer Bridge-Antwort übernehmen (uuid, name, accessToken). */
    public static boolean applySession(JsonObject o) {
        if (o == null || !o.has("accessToken") || o.get("accessToken").getAsString().isEmpty()) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        String token = o.get("accessToken").getAsString();
        String name = o.has("name") ? o.get("name").getAsString() : mc.getSession().getUsername();
        UUID uuid = null;
        try { uuid = toUuid(o.get("uuid").getAsString()); } catch (Exception ignored) {}
        if (!SessionHealer.applySession(mc, token, name, uuid)) return false;
        // Launcher hat shared.json + Cosmetics für den neuen Account neu geschrieben → neu laden
        try { SharedData.reload(); } catch (Exception ignored) {}
        try { CosmeticsManager.get().reload(); } catch (Exception ignored) {}
        try { CapeManager.get().reload(mc); } catch (Exception ignored) {}
        ChaosClient.get().getNotifications().success("Angemeldet als " + name);
        return true;
    }

    public static UUID toUuid(String s) {
        String h = s.replace("-", "");
        if (h.length() != 32) return UUID.fromString(s);
        return UUID.fromString(h.substring(0, 8) + "-" + h.substring(8, 12) + "-" + h.substring(12, 16) + "-" + h.substring(16, 20) + "-" + h.substring(20));
    }
}
