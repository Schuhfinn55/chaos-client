package com.chaoscraft.client.modules.screen;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import org.lwjgl.glfw.GLFW;

/** Screenshots: Taste (Standard F12) nimmt auf, Historie mit Vorschau im Menü. Kein Auto-Upload ohne Zustimmung. */
public class ScreenshotModule extends Module {

    private final BooleanSetting notify = add(new BooleanSetting("Benachrichtigung", "Hinweis nach dem Speichern.", true));
    private final BooleanSetting autoUpload = add(new BooleanSetting("Auto-Upload (Chaos)", "Screenshots automatisch über das Chaos-System teilen. Standard aus – benötigt die Chaos-API.", false));

    public ScreenshotModule() {
        super("Screenshots", "Screenshot aufnehmen, Vorschau, Historie, Ordner öffnen.", Category.SCREEN, "📷");
        getKeybindSetting().set(GLFW.GLFW_KEY_F12);
        alwaysOn();
        tags("screenshot", "bild", "foto", "f2");
    }

    public boolean autoUpload() { return autoUpload.isEnabled(); }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled && isEnabled() && mc.currentScreen == null) { ChaosClient.get().getScreenshots().take(); return; }
        super.setEnabled(true);
    }
}
