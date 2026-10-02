package com.chaoscraft.client.cosmetics;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vorgefertigte Hüte – identisch zur Definition im Chaos Launcher
 * (src/lib/builtinHats.ts). Koordinaten im Kopf-Modellraum: Kopf =
 * x −4..4, y −8..0 (oben negativ), z −4..4; Einheit 1/16 Block.
 */
public final class HatCatalog {
    private HatCatalog() {}

    public record Box(float x, float y, float z, float w, float h, float d, int color) {}
    public record Hat(String id, String name, String description, String icon, List<Box> boxes) {}

    private static Box b(double x, double y, double z, double w, double h, double d, int color) {
        return new Box((float) x, (float) y, (float) z, (float) w, (float) h, (float) d, 0xFF000000 | color);
    }

    private static final Map<String, Hat> HATS = new LinkedHashMap<>();

    static {
        add(new Hat("top-hat", "Zylinder", "Klassischer schwarzer Zylinder mit rotem Band.", "🎩", List.of(
            b(-6, -9, -6, 12, 1, 12, 0x111113), b(-4, -16, -4, 8, 7, 8, 0x1a1a1e), b(-4.3, -11, -4.3, 8.6, 2, 8.6, 0xe11d2e))));
        add(new Hat("chaos-crown", "Chaos-Krone", "Schwarze Krone mit roten Zacken.", "👑", List.of(
            b(-4.5, -11, -4.5, 9, 3, 9, 0x1a0a0d), b(-4.5, -13, -4.5, 2, 2, 2, 0xe11d2e), b(2.5, -13, -4.5, 2, 2, 2, 0xe11d2e),
            b(-4.5, -13, 2.5, 2, 2, 2, 0xe11d2e), b(2.5, -13, 2.5, 2, 2, 2, 0xe11d2e), b(-1, -14, -4.5, 2, 3, 1, 0xff4d5e))));
        add(new Hat("gold-crown", "Goldkrone", "Königliche Krone aus Gold.", "👑", List.of(
            b(-4.5, -11, -4.5, 9, 3, 9, 0xf5c342), b(-4.5, -13, -4.5, 2, 2, 2, 0xf5c342), b(2.5, -13, -4.5, 2, 2, 2, 0xf5c342),
            b(-4.5, -13, 2.5, 2, 2, 2, 0xf5c342), b(2.5, -13, 2.5, 2, 2, 2, 0xf5c342), b(-1, -13, -4.6, 2, 2, 1, 0xe11d2e))));
        add(new Hat("halo", "Heiligenschein", "Leuchtender Ring über dem Kopf.", "😇", List.of(
            b(-4, -11, -4, 8, 0.6, 1, 0xfff1b8), b(-4, -11, 3, 8, 0.6, 1, 0xfff1b8), b(-4, -11, -3, 1, 0.6, 6, 0xfff1b8), b(3, -11, -3, 1, 0.6, 6, 0xfff1b8))));
        add(new Hat("horns", "Teufelshörner", "Zwei rote Hörner.", "😈", List.of(
            b(-4.5, -10, -1, 2, 2, 2, 0x8f1b22), b(-4, -12, -0.5, 1.4, 2, 1.4, 0xe11d2e), b(-3.6, -13.5, -0.2, 0.8, 1.6, 0.8, 0xff6b6b),
            b(2.5, -10, -1, 2, 2, 2, 0x8f1b22), b(2.6, -12, -0.5, 1.4, 2, 1.4, 0xe11d2e), b(2.8, -13.5, -0.2, 0.8, 1.6, 0.8, 0xff6b6b))));
        add(new Hat("wizard", "Zaubererhut", "Spitzhut in Mitternachtsrot.", "🧙", List.of(
            b(-6.5, -9, -6.5, 13, 1, 13, 0x2a0b0f), b(-4, -13, -4, 8, 4, 8, 0x3a0d12), b(-3, -16, -3, 6, 3, 6, 0x3a0d12),
            b(-2, -18.5, -2, 4, 2.5, 4, 0x3a0d12), b(-1, -20.5, -1, 2, 2, 2, 0xe11d2e))));
        add(new Hat("cap", "Chaos-Cap", "Rote Baseballcap mit schwarzem Schild.", "🧢", List.of(
            b(-4.5, -9.5, -4.5, 9, 2.5, 9, 0xe11d2e), b(-4, -7, -8, 8, 0.8, 4, 0x111113), b(-0.6, -10.2, -0.6, 1.2, 0.8, 1.2, 0x111113))));
        add(new Hat("headphones", "Kopfhörer", "Gaming-Headset mit rotem Licht.", "🎧", List.of(
            b(-4.5, -9.5, -1, 9, 1, 2, 0x111113), b(-5.6, -6, -1.6, 1.6, 4, 3.2, 0x1a1a1e), b(4, -6, -1.6, 1.6, 4, 3.2, 0x1a1a1e),
            b(-5.9, -5, -0.6, 0.4, 2, 1.2, 0xe11d2e), b(5.5, -5, -0.6, 0.4, 2, 1.2, 0xe11d2e))));
        add(new Hat("bunny", "Hasenohren", "Zwei lange Ohren.", "🐰", List.of(
            b(-3.5, -15, -0.6, 2, 7, 1.2, 0xf4f1f2), b(-3, -14, -0.3, 1, 5, 0.6, 0xf9a8b8), b(1.5, -15, -0.6, 2, 7, 1.2, 0xf4f1f2), b(2, -14, -0.3, 1, 5, 0.6, 0xf9a8b8))));
        add(new Hat("viking", "Wikingerhelm", "Eisenhelm mit Hörnern.", "⛑", List.of(
            b(-4.5, -9.5, -4.5, 9, 3, 9, 0x9ca3af), b(-4.5, -7, -4.5, 9, 0.6, 9, 0x4b5563), b(-7, -11, -1, 2.5, 2, 2, 0xf4f1f2),
            b(4.5, -11, -1, 2.5, 2, 2, 0xf4f1f2), b(-6, -13, -0.6, 1.4, 2.2, 1.2, 0xf4f1f2), b(4.6, -13, -0.6, 1.4, 2.2, 1.2, 0xf4f1f2))));
        add(new Hat("propeller", "Propellerkappe", "Bunte Kappe mit Propeller.", "🚁", List.of(
            b(-4.5, -9.5, -4.5, 9, 2.5, 9, 0xe11d2e), b(-4.5, -9.5, -4.5, 4.5, 2.5, 4.5, 0xfbbf24), b(0, -9.5, 0, 4.5, 2.5, 4.5, 0x3b82f6),
            b(-0.4, -11.5, -0.4, 0.8, 2, 0.8, 0x111113), b(-4, -12, -0.4, 8, 0.5, 0.8, 0xf4f1f2))));
        add(new Hat("santa", "Weihnachtsmütze", "Rote Mütze mit weißem Bommel.", "🎅", List.of(
            b(-4.6, -9.5, -4.6, 9.2, 2, 9.2, 0xf4f1f2), b(-4, -13, -4, 8, 3.5, 8, 0xe11d2e), b(-2.5, -15.5, -2.5, 5, 2.5, 5, 0xe11d2e),
            b(-0.5, -17, 1, 3, 2, 3, 0xe11d2e), b(1, -18.5, 2.5, 2.5, 2.5, 2.5, 0xf4f1f2))));
    }

    private static void add(Hat h) { HATS.put(h.id(), h); }

    public static List<Hat> all() { return List.copyOf(HATS.values()); }
    public static Hat byId(String id) { return id == null ? null : HATS.get(id); }
}
