package com.chaoscraft.client.settings;

/** Slider zwischen min und max mit Schrittweite. */
public class DoubleSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double step;
    private final int decimals;
    private String suffix = "";

    public DoubleSetting(String name, String description, double defaultValue, double min, double max, double step) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.decimals = step >= 1 ? 0 : step >= 0.1 ? 1 : step >= 0.01 ? 2 : 3;
    }

    public DoubleSetting suffix(String s) { this.suffix = s; return this; }
    public String getSuffix() { return suffix; }
    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }
    public int getDecimals() { return decimals; }
    public float getFloat() { return value.floatValue(); }
    public int getInt() { return (int) Math.round(value); }

    @Override
    public void set(Double v) {
        double snapped = Math.round((v - min) / step) * step + min;
        this.value = Math.max(min, Math.min(max, snapped));
    }

    public double getProgress() { return max == min ? 0 : (value - min) / (max - min); }
    public void setFromProgress(double p) { set(min + Math.max(0, Math.min(1, p)) * (max - min)); }

    public String format() {
        return (decimals == 0 ? String.valueOf(getInt()) : String.format(java.util.Locale.ROOT, "%." + decimals + "f", value)) + suffix;
    }

    @Override public int getHeight() { return 30; }
    @Override public Object toJson() { return value; }
    @Override public void fromJson(Object json) {
        if (json instanceof Number n) set(n.doubleValue());
        else if (json instanceof String s) { try { set(Double.parseDouble(s)); } catch (NumberFormatException ignored) {} }
    }
}
