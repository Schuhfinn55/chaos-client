package com.onyx.visuals.setting;

/** Base for every configurable setting on a module. */
public abstract class Setting<T> {

    private final String name;
    private final String description;
    protected T value;

    public Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public T get() { return value; }
    public void set(T value) { this.value = value; }

    /** Height in pixels this setting occupies when rendered in the GUI. */
    public abstract int getHeight();
}
