package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.Category;
import com.onyx.visuals.module.Module;
import net.minecraft.client.option.SimpleOption;

import java.lang.reflect.Field;

/**
 * Fullbright — forces gamma to max every tick via direct field reflection
 * (bypasses the SimpleOption clamp). Restores the previous gamma on disable.
 * ALSO uses FullbrightMixin to force lightmap brightness — belt and braces.
 * Yarn mappings 1.21.11.
 */
public class FullbrightModule extends Module {

    private double previousGamma = 0.5;
    private Field valueField;

    public FullbrightModule() {
        super("Fullbright", "Light up the whole world.", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options == null) return;
        previousGamma = mc.options.getGamma().getValue();
    }

    @Override
    public void onDisable() {
        if (mc.options == null) return;
        forceGamma(previousGamma);
    }

    @Override
    public void onTick() {
        if (mc.options == null) return;
        forceGamma(16.0);
    }

    private void forceGamma(double value) {
        SimpleOption<Double> gamma = mc.options.getGamma();
        try {
            if (valueField == null) {
                for (Field f : SimpleOption.class.getDeclaredFields()) {
                    if (f.getType() == Object.class) {
                        f.setAccessible(true);
                        valueField = f;
                        break;
                    }
                }
            }
            if (valueField != null) valueField.set(gamma, value);
        } catch (Exception e) {
            gamma.setValue(Math.max(1.0, value));
        }
    }
}
