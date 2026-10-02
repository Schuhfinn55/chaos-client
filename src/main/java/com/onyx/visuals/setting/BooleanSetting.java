package com.onyx.visuals.setting;

/** A simple on/off toggle. */
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public boolean isEnabled() { return value; }
    public void toggle() { value = !value; }

    @Override
    public int getHeight() { return 14; } // Original size
}