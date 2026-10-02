package com.chaoscraft.client.settings;

/** Ganzzahl-Slider. */
public class IntSetting extends DoubleSetting {
    public IntSetting(String name, String description, int defaultValue, int min, int max) {
        super(name, description, defaultValue, min, max, 1.0);
    }
    @Override public IntSetting suffix(String s) { super.suffix(s); return this; }
    @Override public int getInt() { return (int) Math.round(value); }
    public void setInt(int v) { set((double) v); }
}
