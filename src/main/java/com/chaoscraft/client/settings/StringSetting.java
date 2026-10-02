package com.chaoscraft.client.settings;

/** Freitext (z.B. Filterwörter, Formatvorlagen). */
public class StringSetting extends Setting<String> {

    private final int maxLength;
    private String placeholder = "";

    public StringSetting(String name, String description, String defaultValue) { this(name, description, defaultValue, 120); }
    public StringSetting(String name, String description, String defaultValue, int maxLength) {
        super(name, description, defaultValue);
        this.maxLength = maxLength;
    }

    public StringSetting placeholder(String p) { this.placeholder = p; return this; }
    public String getPlaceholder() { return placeholder; }
    public int getMaxLength() { return maxLength; }

    @Override public void set(String v) { value = v == null ? "" : (v.length() > maxLength ? v.substring(0, maxLength) : v); }
    @Override public int getHeight() { return 30; }
    @Override public Object toJson() { return value; }
    @Override public void fromJson(Object json) { if (json != null) set(String.valueOf(json)); }
}
