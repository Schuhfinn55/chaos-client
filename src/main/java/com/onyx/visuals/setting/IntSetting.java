package com.onyx.visuals.setting;

/** Integer shortcut — a slider constrained to whole numbers. */
public class IntSetting extends DoubleSetting {

    public IntSetting(String name, String description, int defaultValue, int min, int max) {
        super(name, description, (double) defaultValue, min, max, 1.0);
    }

    @Override
    public int getInt() { return value.intValue(); }
}
