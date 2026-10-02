package com.chaoscraft.client.settings;

/** Farbe als ARGB-Int. Im Panel: Presets + RGB/Alpha-Slider. */
public class ColorSetting extends Setting<Integer> {

    private final boolean allowAlpha;

    public ColorSetting(String name, String description, int defaultArgb) { this(name, description, defaultArgb, true); }
    public ColorSetting(String name, String description, int defaultArgb, boolean allowAlpha) {
        super(name, description, defaultArgb);
        this.allowAlpha = allowAlpha;
    }

    public boolean allowsAlpha() { return allowAlpha; }
    public int argb() { return value; }
    public int rgb() { return value & 0xFFFFFF; }
    public int alpha() { return (value >>> 24) & 0xFF; }
    public int red() { return (value >> 16) & 0xFF; }
    public int green() { return (value >> 8) & 0xFF; }
    public int blue() { return value & 0xFF; }

    public void setRgba(int r, int g, int b, int a) {
        value = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
    public void setChannel(int channel, int v) {
        v = Math.max(0, Math.min(255, v));
        switch (channel) {
            case 0 -> setRgba(v, green(), blue(), alpha());
            case 1 -> setRgba(red(), v, blue(), alpha());
            case 2 -> setRgba(red(), green(), v, alpha());
            default -> setRgba(red(), green(), blue(), v);
        }
    }

    /** Farbe mit anderer Deckkraft (0..1). */
    public int withAlpha(double a) {
        int al = (int) Math.round(Math.max(0, Math.min(1, a)) * 255);
        return (al << 24) | rgb();
    }

    public String hex() { return String.format("#%06X", rgb()); }

    @Override public int getHeight() { return allowAlpha ? 70 : 60; }
    @Override public Object toJson() { return value; }
    @Override public void fromJson(Object json) {
        if (json instanceof Number n) value = n.intValue();
        else if (json instanceof String s) {
            try { value = (int) Long.parseLong(s.replace("#", ""), 16); if ((value >>> 24) == 0) value |= 0xFF000000; } catch (NumberFormatException ignored) {}
        }
    }
}
