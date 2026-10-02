package com.chaoscraft.client.modules.general;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.DoubleSetting;
import com.chaoscraft.client.ui.Anim;
import org.lwjgl.glfw.GLFW;

/** Zoom (Standard C): Stärke, Smooth Zoom, Scrollrad-Feinzoom, FOV-Rückgabe. */
public class ZoomModule extends Module {

    private final DoubleSetting factor = add(new DoubleSetting("Zoom-Stärke", "Vergrößerungsfaktor.", 4.0, 1.5, 12.0, 0.5).suffix("x"));
    private final BooleanSetting smooth = add(new BooleanSetting("Smooth Zoom", "Weiches Ein-/Auszoomen.", true));
    private final DoubleSetting speed = add(new DoubleSetting("Geschwindigkeit", "Animationsgeschwindigkeit.", 12, 4, 30, 1));
    private final BooleanSetting scroll = add(new BooleanSetting("Scrollrad-Zoom", "Während des Haltens mit dem Scrollrad feinzoomen.", true));

    private final Anim anim = new Anim(1f, 12f);
    private double extra = 1.0;

    public ZoomModule() {
        super("Zoom", "Taste halten, um heranzuzoomen (wie OptiFine).", Category.GENERAL, "⌕");
        getKeybindSetting().set(GLFW.GLFW_KEY_C);
        holdToUse();
        tags("optifine", "fov", "fernglas");
    }

    public boolean isZoomKeyHeld() { return isEnabled() && isKeyHeld(); }

    /** Aktueller FOV-Divisor (animiert). */
    public float currentFactor() {
        float target = isZoomKeyHeld() ? (float) (factor.get() * extra) : 1f;
        if (!smooth.isEnabled()) { anim.snap(target); return target; }
        anim.setTarget(target);
        return Math.max(1f, anim.get());
    }

    public boolean handleScroll(double amount) {
        if (!isZoomKeyHeld() || !scroll.isEnabled()) return false;
        extra = Math.max(0.5, Math.min(4.0, extra + (amount > 0 ? 0.25 : -0.25)));
        return true;
    }

    @Override public void onKeyUp() { extra = 1.0; }
    @Override public void onEnable() { anim.snap(1f); }
}
