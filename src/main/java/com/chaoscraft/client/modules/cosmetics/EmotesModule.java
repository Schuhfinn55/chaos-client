package com.chaoscraft.client.modules.cosmetics;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.KeyManager;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.emotes.EmoteAnimations;
import com.chaoscraft.client.emotes.EmoteManager;
import com.chaoscraft.client.settings.DoubleSetting;
import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.settings.StringSetting;
import com.chaoscraft.client.ui.screens.EmoteWheelScreen;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Emotes: Modul-Taste öffnet das Emote-Rad (Standard B) mit 8 frei belegbaren Slots;
 * jedes Emote hat zusätzlich einen eigenen Keybind zum Direktabspielen. Die Rad-Belegung
 * wird im Client-Profil gespeichert, nach chaos-client/emote-wheel.json geschrieben (der
 * Launcher übernimmt sie) und beim Start mit der Belegung aus dem Launcher abgeglichen –
 * der neuere Stand gewinnt.
 */
public class EmotesModule extends Module {

    public static final int SLOTS = 8;
    public static final List<String> DEFAULT_WHEEL = List.of("wave", "dab", "dance", "floss", "flex", "backflip", "sit", "cheer");

    private final Map<String, KeybindSetting> keys = new HashMap<>();
    private final Map<String, Boolean> held = new HashMap<>();
    private final StringSetting wheel = add(new StringSetting("Rad-Belegung", "Interne Belegung des Emote-Rads.", String.join(",", DEFAULT_WHEEL), 400));
    private final DoubleSetting wheelUpdatedAt = add(new DoubleSetting("Rad geändert", "Zeitstempel der letzten Rad-Änderung.", 0, 0, 1e15, 1));

    public EmotesModule() {
        super("Emotes", "Emote-Rad (Taste, 8 Slots) und Direkttasten – Ganzkörper-Animationen, für Chaos-Spieler in der Nähe sichtbar.", Category.COSMETICS, "☺");
        getKeybindSetting().set(GLFW.GLFW_KEY_B);
        alwaysOn();
        wheel.visibleWhen(() -> false);
        wheelUpdatedAt.visibleWhen(() -> false);
        tags("emote", "winken", "tanzen", "animation", "rad");
        // Eingebaute Emotes vorab registrieren, damit gespeicherte Keybinds beim Config-Laden greifen
        for (String id : EmoteManager.BUILTIN_IDS) keyFor(id);
    }

    public KeybindSetting keyFor(String emoteId) {
        return keys.computeIfAbsent(emoteId, id -> {
            EmoteAnimations.Animated a = EmoteAnimations.byId(id);
            KeybindSetting k = new KeybindSetting("Emote: " + (a != null ? a.name() : id), "Direkttaste für dieses Emote.");
            add(k);
            return k;
        });
    }

    /* ---------------- Emote-Rad ---------------- */

    /** 8 Slot-IDs ("" = leer). */
    public List<String> wheel() {
        List<String> out = new ArrayList<>(Arrays.asList(wheel.get().split(",", -1)));
        while (out.size() < SLOTS) out.add("");
        if (out.size() > SLOTS) out = new ArrayList<>(out.subList(0, SLOTS));
        for (int i = 0; i < SLOTS; i++) if (!out.get(i).isEmpty() && EmoteAnimations.byId(out.get(i)) == null) out.set(i, "");
        return out;
    }

    public long wheelUpdatedAt() { return (long) wheelUpdatedAt.get().doubleValue(); }

    /** Slot belegen (Emote darf nur einmal im Rad liegen) und speichern. */
    public void setSlot(int slot, String id) {
        List<String> w = wheel();
        if (id != null && !id.isEmpty()) for (int i = 0; i < SLOTS; i++) if (w.get(i).equals(id)) w.set(i, "");
        w.set(slot, id == null ? "" : id);
        applyWheel(w, System.currentTimeMillis(), true);
    }

    /** Belegung übernehmen; `persist` schreibt Config + emote-wheel.json. */
    public void applyWheel(List<String> ids, long updatedAt, boolean persist) {
        List<String> w = new ArrayList<>(ids);
        while (w.size() < SLOTS) w.add("");
        wheel.set(String.join(",", w.subList(0, SLOTS)));
        wheelUpdatedAt.set((double) updatedAt);
        if (!persist) return;
        try { ChaosClient.get().getConfigManager().save(); } catch (Exception ignored) {}
        try {
            Path dir = MinecraftClient.getInstance().runDirectory.toPath().resolve("chaos-client");
            Files.createDirectories(dir);
            JsonObject o = new JsonObject();
            o.addProperty("uuid", MinecraftClient.getInstance().getSession().getUuidOrNull() != null ? MinecraftClient.getInstance().getSession().getUuidOrNull().toString().replace("-", "") : "");
            var arr = new com.google.gson.JsonArray();
            for (String s : w.subList(0, SLOTS)) arr.add(s);
            o.add("ids", arr);
            o.addProperty("updatedAt", updatedAt);
            Files.writeString(dir.resolve("emote-wheel.json"), new Gson().toJson(o));
        } catch (Exception e) {
            ChaosClient.LOGGER.warn("[ChaosClient] emote-wheel.json: {}", e.toString());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Taste → Emote-Rad öffnen (Aktion statt Toggle)
        if (!enabled && isEnabled() && mc.currentScreen == null && mc.player != null) {
            mc.setScreen(new EmoteWheelScreen(null));
            return;
        }
        super.setEnabled(true);
    }

    @Override
    public void onTick() {
        EmoteManager em = ChaosClient.get().getEmotes();
        if (em == null) return;
        for (EmoteManager.Emote e : em.all()) {
            KeybindSetting k = keyFor(e.id());
            boolean down = mc.currentScreen == null && KeyManager.isKeyDown(k.getKey());
            boolean was = held.getOrDefault(e.id(), false);
            if (down && !was) em.play(e);
            held.put(e.id(), down);
        }
    }
}
