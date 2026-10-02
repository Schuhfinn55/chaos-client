package com.onyx.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.onyx.visuals.module.HudModule;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.setting.BooleanSetting;
import com.onyx.visuals.setting.DoubleSetting;
import com.onyx.visuals.setting.EnumSetting;
import com.onyx.visuals.setting.KeybindSetting;
import com.onyx.visuals.setting.Setting;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persists module state (enabled, settings, HUD positions) to
 * .minecraft/config/onyxvisuals.json. Loaded after ModuleManager.init(),
 * saved on ClickGUI close, HUD-editor close and game shutdown.
 */
public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final OnyxVisuals client;

    public ConfigManager(OnyxVisuals client) {
        this.client = client;
    }

    private Path file() {
        return MinecraftClient.getInstance().runDirectory.toPath()
                .resolve("config").resolve("chaosclient.json");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) return;
            for (Module m : client.getModuleManager().getModules()) {
                if (!root.has(m.getName())) continue;
                JsonObject mo = root.getAsJsonObject(m.getName());
                if (mo.has("enabled")) m.setEnabled(mo.get("enabled").getAsBoolean());
                if (m instanceof HudModule hud && mo.has("x") && mo.has("y")) {
                    hud.setPos(mo.get("x").getAsInt(), mo.get("y").getAsInt());
                }
                if (mo.has("settings")) {
                    JsonObject so = mo.getAsJsonObject("settings");
                    for (Setting<?> s : m.getSettings()) {
                        if (!so.has(s.getName())) continue;
                        try {
                            if (s instanceof BooleanSetting bs) bs.set(so.get(s.getName()).getAsBoolean());
                            else if (s instanceof DoubleSetting ds) ds.set(so.get(s.getName()).getAsDouble());
                            else if (s instanceof KeybindSetting ks) ks.setKey(so.get(s.getName()).getAsInt());
                            else if (s instanceof EnumSetting es) {
                                String want = so.get(s.getName()).getAsString();
                                for (Object o : es.getOptions()) {
                                    if (o.toString().equalsIgnoreCase(want)) { es.set(o); break; }
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
            OnyxVisuals.LOGGER.info("[ChaosClient] Config geladen.");
        } catch (Exception e) {
            OnyxVisuals.LOGGER.warn("[ChaosClient] Config laden fehlgeschlagen: {}", e.toString());
        }
    }

    public void save() {
        JsonObject root = new JsonObject();
        for (Module m : client.getModuleManager().getModules()) {
            JsonObject mo = new JsonObject();
            mo.addProperty("enabled", m.isEnabled());
            if (m instanceof HudModule hud) {
                mo.addProperty("x", hud.getX());
                mo.addProperty("y", hud.getY());
            }
            JsonObject so = new JsonObject();
            for (Setting<?> s : m.getSettings()) {
                if (s instanceof BooleanSetting bs) so.addProperty(s.getName(), bs.isEnabled());
                else if (s instanceof DoubleSetting ds) so.addProperty(s.getName(), ds.get());
                else if (s instanceof KeybindSetting ks) so.addProperty(s.getName(), ks.getKey());
                else if (s instanceof EnumSetting<?> es) so.addProperty(s.getName(), es.get().toString());
            }
            mo.add("settings", so);
            root.add(m.getName(), mo);
        }
        Path f = file();
        try {
            Files.createDirectories(f.getParent());
            try (Writer w = Files.newBufferedWriter(f)) {
                GSON.toJson(root, w);
            }
        } catch (IOException e) {
            OnyxVisuals.LOGGER.warn("[ChaosClient] Config speichern fehlgeschlagen: {}", e.toString());
        }
    }
}
