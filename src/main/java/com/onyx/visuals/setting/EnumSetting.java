package com.onyx.visuals.setting;

import java.util.Arrays;
import java.util.List;

/** A multi-choice setting rendered as a clickable cycle in the GUI. */
public class EnumSetting<T extends Enum<T>> extends Setting<T> {

    private final List<T> options;

    public EnumSetting(String name, String description, T defaultValue) {
        super(name, description, defaultValue);
        this.options = Arrays.asList(defaultValue.getDeclaringClass().getEnumConstants());
    }

    public List<T> getOptions() { return options; }

    public void cycle(boolean forward) {
        int idx = options.indexOf(value);
        if (forward) idx = (idx + 1) % options.size();
        else idx = (idx - 1 + options.size()) % options.size();
        value = options.get(idx);
    }

    @Override
    public int getHeight() { return 14; } // Original size
}