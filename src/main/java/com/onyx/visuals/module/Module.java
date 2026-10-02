package com.onyx.visuals.module;

import com.onyx.visuals.setting.KeybindSetting;
import com.onyx.visuals.setting.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;

    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        // every module gets a keybind as the first setting (set in the GUI)
        add(new KeybindSetting("Keybind", "Toggle key for this module."));
    }

    protected <T extends Setting<?>> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public Setting<?> getSetting(String name) {
        return settings.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public KeybindSetting getKeybindSetting() {
        return (KeybindSetting) settings.stream()
                .filter(s -> s instanceof KeybindSetting)
                .findFirst()
                .orElse(null);
    }

    public int getKeyCode() {
        KeybindSetting kb = getKeybindSetting();
        return kb != null ? kb.getKey() : -1;
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}

    public final void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public List<Setting<?>> getSettings() { return settings; }
    public boolean hasSettings() { return !settings.isEmpty(); }
}
