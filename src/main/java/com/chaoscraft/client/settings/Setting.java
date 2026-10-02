package com.chaoscraft.client.settings;

import java.util.function.BooleanSupplier;

/**
 * Basis für jede konfigurierbare Einstellung eines Moduls.
 * Typen: BOOLEAN, SLIDER (Double/Int), COLOR, KEYBIND, DROPDOWN (Enum), TEXT.
 */
public abstract class Setting<T> {

    private final String name;
    private final String description;
    protected T value;
    private final T defaultValue;
    private BooleanSupplier visible = () -> true;

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public T get() { return value; }
    public T getDefault() { return defaultValue; }
    public void set(T value) { this.value = value; }
    public void reset() { this.value = defaultValue; }

    /** Nur anzeigen, wenn die Bedingung erfüllt ist (z.B. Unteroption). */
    public Setting<T> visibleWhen(BooleanSupplier condition) {
        this.visible = condition;
        return this;
    }
    public boolean isVisible() { return visible.getAsBoolean(); }

    /** Höhe in Pixeln im Konfigurationspanel. */
    public int getHeight() { return 22; }

    /** Serialisierbarer Wert für die Config. */
    public abstract Object toJson();
    /** Wert aus der Config übernehmen (tolerant gegenüber falschen Typen). */
    public abstract void fromJson(Object json);
}
