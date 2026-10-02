package com.chaoscraft.client.modules.general;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.EnumSetting;
import com.chaoscraft.client.settings.IntSetting;
import com.chaoscraft.client.waypoints.WaypointManager;
import net.minecraft.client.option.SimpleOption;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Fullbright: volle Helligkeit. Modus GAMMA (Gamma hoch, Shader-kompatibel)
 * oder LIGHTMAP (über FullbrightMixin erzwungen). Optional automatisch pro
 * Welt/Server merken.
 */
public class FullbrightModule extends Module {

    public enum Mode { LIGHTMAP, GAMMA }

    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Modus", "LIGHTMAP: maximale Helligkeit. GAMMA: nur Gamma erhöhen (Shader-kompatibel).", Mode.LIGHTMAP));
    private final IntSetting gamma = add(new IntSetting("Gamma", "Gamma-Faktor im GAMMA-Modus.", 10, 2, 16));
    private final BooleanSetting perWorld = add(new BooleanSetting("Pro Welt merken", "Aktivierung automatisch pro Welt/Server speichern.", false));

    private double previousGamma = 0.5;
    private Field valueField;
    private final Set<String> autoWorlds = new HashSet<>();
    private String lastWorld = "";

    public FullbrightModule() {
        super("Fullbright", "Helligkeit auf Maximum – nachts und in Höhlen alles sehen.", Category.GENERAL, "☀");
        tags("brightness", "gamma", "hell", "licht", "nacht");
    }

    public boolean lightmapMode() { return isEnabled() && mode.get() == Mode.LIGHTMAP; }

    @Override public void onEnable() { if (mc.options != null) previousGamma = mc.options.getGamma().getValue(); }
    @Override public void onDisable() { if (mc.options != null) forceGamma(previousGamma); }

    @Override
    public void onTick() {
        if (mc.options == null) return;
        if (mode.get() == Mode.GAMMA) forceGamma(gamma.getInt()); else forceGamma(Math.max(1.0, previousGamma));
        if (perWorld.isEnabled()) {
            String w = WaypointManager.currentServer() + "/" + WaypointManager.currentDimension();
            if (!w.equals(lastWorld)) { lastWorld = w; autoWorlds.add(w); }
        }
    }

    private void forceGamma(double value) {
        SimpleOption<Double> g = mc.options.getGamma();
        try {
            if (valueField == null) {
                for (Field f : SimpleOption.class.getDeclaredFields()) {
                    if (f.getType() == Object.class) { f.setAccessible(true); valueField = f; break; }
                }
            }
            if (valueField != null) valueField.set(g, value); else g.setValue(Math.min(1.0, value));
        } catch (Exception e) {
            g.setValue(Math.min(1.0, value));
        }
    }
}
