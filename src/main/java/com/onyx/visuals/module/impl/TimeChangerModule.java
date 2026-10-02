package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.Category;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.setting.EnumSetting;
import net.minecraft.client.world.ClientWorld;

/**
 * TimeChanger — overrides the client-side displayed time of day.
 * Sets ClientWorld.Properties.setTimeOfDay() every tick while enabled
 * (mapping-safe, no mixin). Server time is untouched.
 * Yarn mappings 1.21.11.
 */
public class TimeChangerModule extends Module {

    public enum TimePreset {
        DAY(1000),
        SUNSET(12000),
        NIGHT(13000),
        MIDNIGHT(18000);

        private final long time;
        TimePreset(long time) { this.time = time; }
        public long getTime() { return time; }
    }

    private final EnumSetting<TimePreset> preset =
            add(new EnumSetting<>("Time", "Client-side time of day.", TimePreset.DAY));

    public TimeChangerModule() {
        super("TimeChanger", "Change displayed time client-side.", Category.UTILITY);
    }

    public long getTime() { return preset.get().getTime(); }

    @Override
    public void onTick() {
        if (mc.world == null) return;
        ClientWorld.Properties props = (ClientWorld.Properties) mc.world.getLevelProperties();
        if (props != null) props.setTimeOfDay(getTime());
    }
}
