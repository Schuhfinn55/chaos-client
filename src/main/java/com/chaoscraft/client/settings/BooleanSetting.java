package com.chaoscraft.client.settings;

/** Toggle-Schalter. */
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public boolean isEnabled() { return value; }
    public void toggle() { value = !value; }

    @Override public Object toJson() { return value; }
    @Override public void fromJson(Object json) {
        if (json instanceof Boolean b) value = b;
        else if (json instanceof String s) value = Boolean.parseBoolean(s);
    }
}
