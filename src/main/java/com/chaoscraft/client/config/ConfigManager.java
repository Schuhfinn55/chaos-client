package com.chaoscraft.client.config;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Persistenz der Client-Konfiguration mit Profilen.
 *
 *   config/chaosclient/profiles.json           { "active": "Default" }
 *   config/chaosclient/profiles/<Name>.json    Module, Settings, Keybinds, HUD-Positionen
 *   config/chaosclient/exports/<Name>.json     Export/Import
 *
 * Jedes Profil speichert HUD, Module, Keybinds, Cosmetics, Crosshair,
 * Performance, Chat und GUI – schnell wechselbar. Die alte Datei
 * config/chaosclient.json (v1) wird beim ersten Start übernommen.
 */
public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String DEFAULT_PROFILE = "Default";

    private final ChaosClient client;
    private String activeProfile = DEFAULT_PROFILE;

    public ConfigManager(ChaosClient client) { this.client = client; }

    /* ---------- Pfade ---------- */
    public Path baseDir() {
        Path p = MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve("chaosclient");
        try { Files.createDirectories(p); } catch (IOException ignored) {}
        return p;
    }
    public Path profilesDir() {
        Path p = baseDir().resolve("profiles");
        try { Files.createDirectories(p); } catch (IOException ignored) {}
        return p;
    }
    public Path exportsDir() {
        Path p = baseDir().resolve("exports");
        try { Files.createDirectories(p); } catch (IOException ignored) {}
        return p;
    }
    private Path profileFile(String name) { return profilesDir().resolve(safe(name) + ".json"); }
    private Path indexFile() { return baseDir().resolve("profiles.json"); }
    private Path legacyFile() { return MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve("chaosclient.json"); }

    public static String safe(String name) {
        String s = name.replaceAll("[^A-Za-z0-9_\\- ]", "").trim();
        return s.isEmpty() ? DEFAULT_PROFILE : s;
    }

    public String getActiveProfile() { return activeProfile; }

    /* ---------- Profile ---------- */
    public List<String> listProfiles() {
        List<String> out = new ArrayList<>();
        try (Stream<Path> s = Files.list(profilesDir())) {
            s.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                String n = p.getFileName().toString();
                out.add(n.substring(0, n.length() - 5));
            });
        } catch (IOException ignored) {}
        if (!out.contains(activeProfile)) out.add(activeProfile);
        out.sort(String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    /** Wechselt das Profil: aktuelles speichern, neues laden (oder aus aktuellem anlegen). */
    public void switchProfile(String name) {
        save();
        String target = safe(name);
        activeProfile = target;
        if (Files.exists(profileFile(target))) {
            loadProfile(target);
        } else {
            save(); // neues Profil mit aktuellen Werten
        }
        saveIndex();
        ChaosClient.LOGGER.info("[ChaosClient] Profil aktiv: {}", target);
    }

    public void createProfile(String name, boolean copyCurrent) {
        String target = safe(name);
        if (!copyCurrent) {
            // Standardwerte: alle Module zurücksetzen, dann speichern
            save();
            activeProfile = target;
            resetAll();
            save();
            saveIndex();
            return;
        }
        switchProfile(target);
    }

    public void deleteProfile(String name) {
        String target = safe(name);
        try { Files.deleteIfExists(profileFile(target)); } catch (IOException ignored) {}
        if (target.equals(activeProfile)) {
            List<String> rest = listProfiles();
            rest.remove(target);
            activeProfile = rest.isEmpty() ? DEFAULT_PROFILE : rest.get(0);
            if (Files.exists(profileFile(activeProfile))) loadProfile(activeProfile); else save();
            saveIndex();
        }
    }

    public void renameProfile(String from, String to) {
        String a = safe(from), b = safe(to);
        if (a.equals(b)) return;
        try {
            Files.move(profileFile(a), profileFile(b));
            if (a.equals(activeProfile)) { activeProfile = b; saveIndex(); }
        } catch (IOException e) {
            ChaosClient.LOGGER.warn("[ChaosClient] Profil umbenennen: {}", e.toString());
        }
    }

    private void resetAll() {
        for (Module m : client.getModuleManager().getModules()) {
            for (Setting<?> s : m.getSettings()) s.reset();
            if (!m.isAlwaysOn()) m.setEnabled(false);
        }
    }

    /* ---------- Laden / Speichern ---------- */
    public void load() {
        // Index
        if (Files.exists(indexFile())) {
            try (Reader r = Files.newBufferedReader(indexFile())) {
                JsonObject o = GSON.fromJson(r, JsonObject.class);
                if (o != null && o.has("active")) activeProfile = safe(o.get("active").getAsString());
            } catch (Exception ignored) {}
        }
        if (Files.exists(profileFile(activeProfile))) {
            loadProfile(activeProfile);
        } else if (Files.exists(legacyFile())) {
            // v1-Migration
            loadFrom(legacyFile());
            save();
            ChaosClient.LOGGER.info("[ChaosClient] Alte Konfiguration übernommen.");
        }
        saveIndex();
    }

    private void loadProfile(String name) { loadFrom(profileFile(name)); }

    private void loadFrom(Path file) {
        try (Reader r = Files.newBufferedReader(file)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) return;
            JsonObject modules = root.has("modules") && root.get("modules").isJsonObject() ? root.getAsJsonObject("modules") : root;
            for (Module m : client.getModuleManager().getModules()) {
                if (!modules.has(m.getName())) continue;
                JsonObject mo = modules.getAsJsonObject(m.getName());
                if (m instanceof HudModule hud && mo.has("x") && mo.has("y")) hud.setPos(mo.get("x").getAsInt(), mo.get("y").getAsInt());
                if (mo.has("settings") && mo.get("settings").isJsonObject()) {
                    JsonObject so = mo.getAsJsonObject("settings");
                    for (Setting<?> s : m.getSettings()) {
                        if (!so.has(s.getName())) continue;
                        try { s.fromJson(toJava(so.get(s.getName()))); } catch (Exception ignored) {}
                    }
                }
                if (mo.has("enabled")) m.setEnabled(mo.get("enabled").getAsBoolean());
            }
        } catch (Exception e) {
            ChaosClient.LOGGER.warn("[ChaosClient] Config laden fehlgeschlagen ({}): {}", file.getFileName(), e.toString());
        }
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("format", 2);
        root.addProperty("client", ChaosClient.VERSION);
        root.addProperty("profile", activeProfile);
        JsonObject modules = new JsonObject();
        for (Module m : client.getModuleManager().getModules()) {
            JsonObject mo = new JsonObject();
            mo.addProperty("enabled", m.isEnabled());
            if (m instanceof HudModule hud) {
                mo.addProperty("x", hud.getX());
                mo.addProperty("y", hud.getY());
            }
            JsonObject so = new JsonObject();
            for (Setting<?> s : m.getSettings()) {
                Object v = s.toJson();
                if (v instanceof Boolean b) so.addProperty(s.getName(), b);
                else if (v instanceof Number n) so.addProperty(s.getName(), n);
                else if (v != null) so.addProperty(s.getName(), String.valueOf(v));
            }
            mo.add("settings", so);
            modules.add(m.getName(), mo);
        }
        root.add("modules", modules);
        return root;
    }

    public void save() {
        writeJson(profileFile(activeProfile), toJson());
        saveIndex();
    }

    private void saveIndex() {
        JsonObject o = new JsonObject();
        o.addProperty("active", activeProfile);
        writeJson(indexFile(), o);
    }

    private void writeJson(Path file, JsonObject o) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer w = Files.newBufferedWriter(file)) { GSON.toJson(o, w); }
        } catch (IOException e) {
            ChaosClient.LOGGER.warn("[ChaosClient] Config speichern fehlgeschlagen: {}", e.toString());
        }
    }

    /* ---------- Import / Export ---------- */
    public Path exportProfile(String fileName) {
        String n = safe(fileName.isEmpty() ? activeProfile : fileName);
        Path f = exportsDir().resolve(n + ".json");
        writeJson(f, toJson());
        return f;
    }

    public List<Path> listExports() {
        List<Path> out = new ArrayList<>();
        try (Stream<Path> s = Files.list(exportsDir())) {
            s.filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".json")).forEach(out::add);
        } catch (IOException ignored) {}
        out.sort((a, b) -> a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString()));
        return out;
    }

    public boolean importProfile(Path file) {
        if (!Files.exists(file)) return false;
        loadFrom(file);
        save();
        return true;
    }

    /* ---------- Hilfen ---------- */
    private static Object toJava(JsonElement e) {
        if (e == null || e.isJsonNull()) return null;
        if (e.isJsonPrimitive()) {
            JsonPrimitive p = e.getAsJsonPrimitive();
            if (p.isBoolean()) return p.getAsBoolean();
            if (p.isNumber()) return p.getAsNumber();
            return p.getAsString();
        }
        return e.toString();
    }

    /** Werte als flache Map (für Anzeige/Debug). */
    public Map<String, JsonElement> moduleJson(Module m) {
        return toJson().getAsJsonObject("modules").getAsJsonObject(m.getName()).asMap();
    }
}
