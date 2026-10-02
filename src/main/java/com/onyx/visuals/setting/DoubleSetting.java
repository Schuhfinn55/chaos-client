package com.onyx.visuals.setting;

/** A numeric slider between min and max. Renders as a draggable bar in the GUI. */
public class DoubleSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double step;
    private final int decimals;

    public DoubleSetting(String name, String description, double defaultValue,
                         double min, double max, double step) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.decimals = decimalsFor(step);
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }
    public int getDecimals() { return decimals; }

    public float getFloat() { return value.floatValue(); }
    public int getInt() { return value.intValue(); }

    @Override
    public void set(Double value) {
        double snapped = Math.round((value - min) / step) * step + min;
        this.value = Math.max(min, Math.min(max, snapped));
    }

    /** 0.0 (min) .. 1.0 (max) — used by the slider rendering. */
    public double getProgress() {
        if (max == min) return 0.0;
        return (value - min) / (max - min);
    }

    public void setFromProgress(double progress) {
        set(min + progress * (max - min));
    }

    private static int decimalsFor(double step) {
        if (step >= 1) return 0;
        if (step >= 0.1) return 1;
        if (step >= 0.01) return 2;
        return 3;
    }

    @Override
    public int getHeight() { return 18; } // Original size
}