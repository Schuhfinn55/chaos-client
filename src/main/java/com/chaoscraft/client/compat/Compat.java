package com.chaoscraft.client.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

/**
 * Versions-Verträglichkeit: Minecraft-Version lesen, Mindestversionen
 * prüfen, geladene Mods erkennen. Module nutzen {@code requires(...)},
 * damit nicht unterstützte Funktionen sauber deaktiviert werden statt
 * das Spiel abstürzen zu lassen.
 */
public final class Compat {

    private Compat() {}

    private static String cached;

    /** Minecraft-Version als String, z.B. "1.21.11". */
    public static String version() {
        if (cached == null) {
            cached = FabricLoader.getInstance().getModContainer("minecraft")
                    .map(c -> c.getMetadata().getVersion().getFriendlyString())
                    .orElse("unknown");
        }
        return cached;
    }

    /** true, wenn die laufende Version >= minVersion ist (numerischer Vergleich). */
    public static boolean atLeast(String minVersion) {
        int[] a = parse(version());
        int[] b = parse(minVersion);
        if (a.length == 0) return true; // unbekannt: nicht blockieren
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? a[i] : 0;
            int y = i < b.length ? b[i] : 0;
            if (x != y) return x > y;
        }
        return true;
    }

    public static boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    public static Optional<String> modVersion(String id) {
        return FabricLoader.getInstance().getModContainer(id).map(ModContainer::getMetadata).map(m -> m.getVersion().getFriendlyString());
    }

    /** Sodium/Iris etc. erkennen, um doppelte Optimierungen zu vermeiden. */
    public static boolean hasSodium() { return isModLoaded("sodium"); }
    public static boolean hasIris() { return isModLoaded("iris"); }
    public static boolean hasEntityCulling() { return isModLoaded("entityculling"); }

    private static int[] parse(String v) {
        try {
            String clean = v.split("[-+ ]")[0];
            String[] parts = clean.split("\\.");
            int[] out = new int[parts.length];
            for (int i = 0; i < parts.length; i++) out[i] = Integer.parseInt(parts[i].replaceAll("\\D", "0"));
            return out;
        } catch (Exception e) {
            return new int[0];
        }
    }
}
