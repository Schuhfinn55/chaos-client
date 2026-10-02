package com.chaoscraft.client.settings;

import java.util.Arrays;
import java.util.List;

/** Dropdown-Auswahl über Enum-Werte. */
public class EnumSetting<T extends Enum<T>> extends Setting<T> {

    private final List<T> options;

    public EnumSetting(String name, String description, T defaultValue) {
        super(name, description, defaultValue);
        this.options = Arrays.asList(defaultValue.getDeclaringClass().getEnumConstants());
    }

    public List<T> getOptions() { return options; }

    public void cycle(boolean forward) {
        int idx = options.indexOf(value);
        idx = forward ? (idx + 1) % options.size() : (idx - 1 + options.size()) % options.size();
        value = options.get(idx);
    }

    /** Lesbarer Name: "THIRD_PERSON" → "Third Person". */
    public static String label(Enum<?> e) {
        String[] parts = e.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }
    public String label() { return label(value); }

    @Override public Object toJson() { return value.name(); }
    @Override public void fromJson(Object json) {
        if (json instanceof String s) {
            for (T o : options) if (o.name().equalsIgnoreCase(s) || o.toString().equalsIgnoreCase(s)) { value = o; return; }
        }
    }
}
