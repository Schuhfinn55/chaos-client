package com.onyx.visuals.event;

import com.onyx.visuals.gui.ClickGuiScreen;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.module.ModuleManager;
import com.onyx.visuals.setting.KeybindSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

/**
 * Polls GLFW keys every tick with edge-detection. R-Shift opens the ClickGUI.
 * Yarn mappings 1.21.11.
 */
public class KeyInputHandler {

    private static final int GUI_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;

    private final ModuleManager moduleManager;
    private final Set<Integer> heldKeys = new HashSet<>();
    private boolean guiKeyDown;

    public KeyInputHandler(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            pollModuleKeys(client);
            handleGuiKey(client);
        });
    }

    private void pollModuleKeys(MinecraftClient client) {
        long handle = client.getWindow().getHandle();

        for (Module module : moduleManager.getModules()) {
            KeybindSetting kb = module.getKeybindSetting();
            if (kb == null || !kb.isBound()) continue;

            int key = kb.getKey();
            boolean pressed = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;

            if (pressed && !heldKeys.contains(key)) {
                if (client.currentScreen instanceof ClickGuiScreen gui && gui.isCapturingKeybind()) continue;
                module.toggle();
            }

            if (pressed) heldKeys.add(key);
            else heldKeys.remove(key);
        }
    }

    private void handleGuiKey(MinecraftClient client) {
        boolean pressed = GLFW.glfwGetKey(client.getWindow().getHandle(), GUI_KEY) == GLFW.GLFW_PRESS;
        if (pressed && !guiKeyDown && client.currentScreen == null) {
            client.setScreen(new ClickGuiScreen(moduleManager));
        }
        guiKeyDown = pressed;
    }
}
