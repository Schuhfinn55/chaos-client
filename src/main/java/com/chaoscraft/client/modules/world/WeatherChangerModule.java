package com.chaoscraft.client.modules.world;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.mixin.WorldAccessor;
import com.chaoscraft.client.settings.EnumSetting;

/** Weather Changer: Wetter nur für dich ändern (Server bleibt unberührt). */
public class WeatherChangerModule extends Module {

    public enum Weather { KLAR, REGEN, GEWITTER }

    private final EnumSetting<Weather> weather = add(new EnumSetting<>("Wetter", "Angezeigtes Wetter.", Weather.KLAR));

    public WeatherChangerModule() {
        super("Weather Changer", "Regen und Gewitter clientseitig an- oder abschalten.", Category.WORLD, "☂");
        tags("wetter", "regen", "gewitter", "clear", "rain");
    }

    /** Jeden Frame aufrufen (HudRenderer), damit Vanilla den Wert nicht zurückinterpoliert. */
    public void apply() {
        if (mc.world == null) return;
        float rain = weather.get() == Weather.KLAR ? 0f : 1f;
        float thunder = weather.get() == Weather.GEWITTER ? 1f : 0f;
        mc.world.setRainGradient(rain);
        mc.world.setThunderGradient(thunder);
        ((WorldAccessor) mc.world).chaos$setLastRainGradient(rain);
        ((WorldAccessor) mc.world).chaos$setLastThunderGradient(thunder);
    }

    @Override public void onTick() { apply(); }
}
