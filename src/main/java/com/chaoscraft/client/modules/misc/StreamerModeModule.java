package com.chaoscraft.client.modules.misc;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;

/** Streamer Mode: Server-IP und Koordinaten in den HUDs verstecken. */
public class StreamerModeModule extends Module {

    private final BooleanSetting hideIp = add(new BooleanSetting("IP verstecken", "Server-Adresse in HUDs maskieren.", true));
    private final BooleanSetting hideCoords = add(new BooleanSetting("Koordinaten verstecken", "Koordinaten-HUD ausblenden.", true));

    public StreamerModeModule() {
        super("Streamer Mode", "Versteckt Server-IP und Koordinaten für Stream und Aufnahme.", Category.MISC, "▶");
        tags("stream", "streamer", "ip", "koordinaten", "privat");
    }

    private static StreamerModeModule get() {
        ChaosClient cc = ChaosClient.get();
        if (cc == null || cc.getModuleManager() == null) return null;
        StreamerModeModule m = cc.getModuleManager().get(StreamerModeModule.class);
        return m != null && m.isEnabled() ? m : null;
    }

    public static boolean hidesIp() { StreamerModeModule m = get(); return m != null && m.hideIp.isEnabled(); }
    public static boolean hidesCoords() { StreamerModeModule m = get(); return m != null && m.hideCoords.isEnabled(); }
}
