package com.chaoscraft.client.modules.world;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.EnumSetting;
import net.minecraft.client.world.ClientWorld;

/** Time Changer: nur die angezeigte Tageszeit (clientseitig) ändern. */
public class TimeChangerModule extends Module {

    public enum TimePreset {
        DAY(1000), NOON(6000), SUNSET(12000), NIGHT(13000), MIDNIGHT(18000);
        private final long time;
        TimePreset(long t) { time = t; }
        public long getTime() { return time; }
    }

    private final EnumSetting<TimePreset> preset = add(new EnumSetting<>("Zeit", "Angezeigte Tageszeit.", TimePreset.DAY));

    public TimeChangerModule() {
        super("Time Changer", "Angezeigte Tageszeit clientseitig ändern (Server bleibt unberührt).", Category.WORLD, "☾");
        tags("time", "zeit", "tag", "nacht");
    }

    @Override
    public void onTick() {
        if (mc.world == null) return;
        if (mc.world.getLevelProperties() instanceof ClientWorld.Properties props) props.setTimeOfDay(preset.get().getTime());
    }
}
