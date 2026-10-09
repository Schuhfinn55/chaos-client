package com.chaoscraft.client.modules.cosmetics;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.KeyManager;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.emotes.EmoteManager;
import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.ui.screens.CosmeticsScreen;
import com.chaoscraft.client.ui.screens.EmoteWheelScreen;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * Emotes: Modul-Taste öffnet das Emote-Menü (Standard B); jedes Emote hat
 * zusätzlich einen eigenen Keybind zum Direktabspielen.
 */
public class EmotesModule extends Module {

    private final Map<String, KeybindSetting> keys = new HashMap<>();
    private final Map<String, Boolean> held = new HashMap<>();

    public EmotesModule() {
        super("Emotes", "Emote-Rad (Taste) und Direkttasten – Ganzkörper-Animationen, für Chaos-Spieler in der Nähe sichtbar.", Category.COSMETICS, "☺");
        getKeybindSetting().set(GLFW.GLFW_KEY_B);
        alwaysOn();
        tags("emote", "winken", "tanzen", "animation");
        // Eingebaute Emotes vorab registrieren, damit gespeicherte Keybinds beim Config-Laden greifen;
        // später hinzugefügte Emotes bekommen ihren Keybind lazy in onTick.
        for (String id : EmoteManager.BUILTIN_IDS) keyFor(id);
    }

    public KeybindSetting keyFor(String emoteId) {
        return keys.computeIfAbsent(emoteId, id -> {
            KeybindSetting k = new KeybindSetting("Emote: " + id, "Direkttaste für dieses Emote.");
            add(k);
            return k;
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Taste → Emote-Menü öffnen (Aktion statt Toggle)
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
