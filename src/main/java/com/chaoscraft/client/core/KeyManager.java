package com.chaoscraft.client.core;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.ui.screens.ChaosMenuScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

/**
 * Zentrale Tastenverwaltung: pollt GLFW pro Tick mit Flankenerkennung.
 * Toggle-Module schalten bei Tastendruck, Hold-Module (Zoom, FreeLook)
 * bekommen onKeyDown/onKeyUp. Die Menü-Taste (Standard RIGHT SHIFT)
 * öffnet das Chaos-Menü. Tasten wirken nur ohne offenes GUI.
 */
public class KeyManager {

    private final ModuleManager modules;
    private final Set<Integer> held = new HashSet<>();
    private final Set<Module> holdActive = new HashSet<>();

    public KeyManager(ModuleManager modules) { this.modules = modules; }

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(this::poll);
    }

    /** Ist eine Taste (oder Maustaste 1000+) gerade gedrückt? */
    public static boolean isKeyDown(int key) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (key == KeybindSetting.NONE || mc.getWindow() == null) return false;
        long h = mc.getWindow().getHandle();
        if (key >= KeybindSetting.MOUSE_OFFSET) return GLFW.glfwGetMouseButton(h, key - KeybindSetting.MOUSE_OFFSET) == GLFW.GLFW_PRESS;
        return GLFW.glfwGetKey(h, key) == GLFW.GLFW_PRESS;
    }

    private void poll(MinecraftClient client) {
        boolean guiOpen = client.currentScreen != null;
        // Menü-Taste
        int menuKey = ChaosClient.get().getMenuKey();
        boolean menuDown = isKeyDown(menuKey);
        if (menuDown && !held.contains(menuKey) && !guiOpen && client.player != null) {
            client.setScreen(new ChaosMenuScreen());
        }
        if (menuDown) held.add(menuKey); else held.remove(menuKey);

        if (guiOpen) {
            // Hold-Module beim Öffnen eines GUIs loslassen
            for (Module m : holdActive) m.onKeyUp();
            holdActive.clear();
            return;
        }

        for (Module module : modules.getModules()) {
            int key = module.getKeyCode();
            if (key == KeybindSetting.NONE || !module.isSupported()) continue;
            boolean pressed = isKeyDown(key);
            boolean was = held.contains(key + 100_000 * (module.getName().hashCode() & 0xF));
            int token = key + 100_000 * (module.getName().hashCode() & 0xF);
            if (module.isHoldToUse()) {
                if (pressed && !holdActive.contains(module) && module.isEnabled()) { module.onKeyDown(); holdActive.add(module); }
                else if (!pressed && holdActive.contains(module)) { module.onKeyUp(); holdActive.remove(module); }
            } else if (pressed && !was) {
                module.toggle();
                ChaosClient.get().getNotifications().moduleToggled(module);
            }
            if (pressed) held.add(token); else held.remove(token);
        }
    }

    /** Alle Hold-Module zurücksetzen (z.B. bei Weltwechsel). */
    public void releaseAll() {
        for (Module m : holdActive) m.onKeyUp();
        holdActive.clear();
        held.clear();
    }
}
