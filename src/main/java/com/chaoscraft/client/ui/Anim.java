package com.chaoscraft.client.ui;

import com.chaoscraft.client.config.ChaosTheme;

/**
 * Kleine Animationshilfe: zeitbasierte Interpolation (framerate-unabhängig).
 * Bei deaktivierten Animationen springt der Wert sofort.
 */
public final class Anim {

    private float value;
    private float target;
    private long last = System.nanoTime();
    private final float speed;

    public Anim(float initial, float speed) {
        this.value = initial;
        this.target = initial;
        this.speed = speed;
    }

    public Anim(float initial) { this(initial, 12f); }

    public void setTarget(float t) { this.target = t; }
    public void snap(float v) { this.value = v; this.target = v; }
    public float target() { return target; }

    /** Aktualisiert und liefert den aktuellen Wert. */
    public float get() {
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - last) / 1_000_000_000f);
        last = now;
        if (!ChaosTheme.get().animations()) { value = target; return value; }
        float k = 1f - (float) Math.exp(-speed * dt);
        value += (target - value) * k;
        if (Math.abs(target - value) < 0.001f) value = target;
        return value;
    }

    public boolean isSettled() { return Math.abs(target - value) < 0.002f; }

    /* ---------- Easing ---------- */
    public static float easeOutCubic(float t) { t = clamp(t); return 1 - (float) Math.pow(1 - t, 3); }
    public static float easeInOut(float t) { t = clamp(t); return t < 0.5f ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2; }
    public static float clamp(float t) { return Math.max(0, Math.min(1, t)); }
    public static float lerp(float a, float b, float t) { return a + (b - a) * clamp(t); }
}
