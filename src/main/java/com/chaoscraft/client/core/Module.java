package com.chaoscraft.client.core;

import com.chaoscraft.client.compat.Compat;
import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.settings.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Basisklasse aller Module. Jedes Modul hat Name, Beschreibung, Kategorie,
 * Aktiv-Zustand, Keybind, Einstellungen, optionales Icon und eine
 * Versions-Verträglichkeit. Module sind unabhängig voneinander.
 */
public abstract class Module {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private final String icon;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();
    private final KeybindSetting keybind;
    /** Taste wirkt nur solange gehalten (Zoom, FreeLook) statt zu toggeln. */
    private boolean holdToUse;
    /** Modul kann nicht deaktiviert werden (reine Konfigurationsmodule). */
    private boolean alwaysOn;
    /** Gründe für fehlende Unterstützung in dieser MC-Version (null = unterstützt). */
    private String unsupportedReason;
    private final List<String> searchTags = new ArrayList<>();

    protected Module(String name, String description, Category category) {
        this(name, description, category, "");
    }

    protected Module(String name, String description, Category category, String icon) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.icon = icon;
        this.keybind = new KeybindSetting("Keybind", "Taste zum Umschalten dieses Moduls.");
        settings.add(keybind);
    }

    protected <T extends Setting<?>> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    protected void holdToUse() { this.holdToUse = true; }
    protected void alwaysOn() { this.alwaysOn = true; this.enabled = true; }
    protected void tags(String... t) { searchTags.addAll(List.of(t)); }

    /** Markiert das Modul als nicht unterstützt, wenn die Bedingung fehlschlägt. */
    protected void requires(boolean supported, String reason) {
        if (!supported) unsupportedReason = reason;
    }
    protected void requiresMinecraft(String minVersion) {
        if (!Compat.atLeast(minVersion)) {
            unsupportedReason = "Diese Funktion wird in Minecraft " + Compat.version() + " nicht unterstützt (benötigt " + minVersion + "+).";
        }
    }

    /* ---------- Lebenszyklus ---------- */
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    /** Taste gedrückt (bei holdToUse) / losgelassen. */
    public void onKeyDown() {}
    public void onKeyUp() {}

    public final void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean enabled) {
        if (alwaysOn) { this.enabled = true; return; }
        if (!isSupported()) { this.enabled = false; return; }
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        try {
            if (enabled) onEnable(); else onDisable();
        } catch (Exception e) {
            com.chaoscraft.client.ChaosClient.LOGGER.warn("[ChaosClient] Modul {}: {}", name, e.toString());
        }
    }

    /* ---------- Getter ---------- */
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public String getIcon() { return icon; }
    public boolean isEnabled() { return enabled; }
    public List<Setting<?>> getSettings() { return settings; }
    public boolean hasSettings() { return settings.size() > 1; }
    public KeybindSetting getKeybindSetting() { return keybind; }
    public int getKeyCode() { return keybind.getKey(); }
    public boolean isHoldToUse() { return holdToUse; }
    public boolean isAlwaysOn() { return alwaysOn; }
    public boolean isSupported() { return unsupportedReason == null; }
    public String getUnsupportedReason() { return unsupportedReason; }

    public Setting<?> getSetting(String settingName) {
        for (Setting<?> s : settings) if (s.getName().equalsIgnoreCase(settingName)) return s;
        return null;
    }

    /** Suche über Name, Beschreibung, Kategorie und Tags. */
    public boolean matches(String query) {
        String q = query.toLowerCase();
        if (name.toLowerCase().contains(q) || description.toLowerCase().contains(q)) return true;
        if (category.getLabel().toLowerCase().contains(q) || category.name().toLowerCase().contains(q)) return true;
        for (String t : searchTags) if (t.toLowerCase().contains(q)) return true;
        for (Setting<?> s : settings) if (s.getName().toLowerCase().contains(q)) return true;
        return false;
    }

    /** Ist die Taste des Moduls gerade physisch gedrückt? */
    public boolean isKeyHeld() {
        return com.chaoscraft.client.core.KeyManager.isKeyDown(getKeyCode());
    }
}
