package com.chaoscraft.client.modules.player;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.DoubleSetting;
import org.lwjgl.glfw.GLFW;

/**
 * Free Look: Taste halten → Kamera frei bewegen, Blick-/Laufrichtung des
 * Spielers bleibt unverändert; nach Loslassen normale Ansicht.
 * Umgesetzt über CameraMixin (Rotation) und EntityLookMixin (Mausbewegung).
 */
public class FreeLookModule extends Module {

    private final DoubleSetting sensitivity = add(new DoubleSetting("Empfindlichkeit", "Kamerageschwindigkeit.", 1.0, 0.2, 3.0, 0.1));
    private final BooleanSetting invertY = add(new BooleanSetting("Y invertieren", "Vertikale Achse umkehren.", false));
    private final BooleanSetting thirdPerson = add(new BooleanSetting("Third Person", "Während Free Look automatisch in die 3rd-Person-Ansicht wechseln.", false));

    private boolean active;
    private float yaw, pitch;
    private net.minecraft.client.option.Perspective previous;

    public FreeLookModule() {
        super("Free Look", "Taste halten und die Kamera frei bewegen, ohne die Laufrichtung zu ändern.", Category.PLAYER, "⟲");
        getKeybindSetting().set(GLFW.GLFW_KEY_LEFT_ALT);
        holdToUse();
        tags("freelook", "kamera", "umschauen");
    }

    public boolean isActive() { return isEnabled() && active && mc.player != null; }
    public float yaw() { return yaw; }
    public float pitch() { return pitch; }

    @Override
    public void onKeyDown() {
        if (mc.player == null) return;
        active = true;
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
        if (thirdPerson.isEnabled()) { previous = mc.options.getPerspective(); mc.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK); }
    }

    @Override
    public void onKeyUp() {
        active = false;
        if (previous != null) { mc.options.setPerspective(previous); previous = null; }
    }

    @Override public void onDisable() { onKeyUp(); }

    /** Vom EntityLookMixin: Mausbewegung auf die freie Kamera anwenden. */
    public void applyMouse(double dx, double dy) {
        float s = (float) (0.15 * sensitivity.get());
        yaw += (float) (dx * s);
        pitch += (float) (dy * s) * (invertY.isEnabled() ? -1 : 1);
        pitch = Math.max(-90f, Math.min(90f, pitch));
    }
}
