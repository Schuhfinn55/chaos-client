package com.chaoscraft.client.cosmetics;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vorgefertigte Hüte – GENERIERT von scripts/gen_hats.py (Chaos Launcher), identisch zu
 * src/lib/builtinHats.ts. Koordinaten im Kopf-Modellraum: Kopf = x −4..4, y −8..0
 * (oben negativ), z −4..4; Einheit 1/16 Block. glow = emissiv, spin = Grad/Tick,
 * bob = Schwebe-Amplitude in Einheiten.
 */
public final class HatCatalog {
    private HatCatalog() {}

    public record Box(float x, float y, float z, float w, float h, float d, int color) {}
    public record Hat(String id, String name, String description, String icon, boolean glow, float spin, float bob, List<Box> boxes) {}

    private static Box b(double x, double y, double z, double w, double h, double d, int color) {
        return new Box((float) x, (float) y, (float) z, (float) w, (float) h, (float) d, 0xFF000000 | color);
    }

    private static final Map<String, Hat> HATS = new LinkedHashMap<>();

    static {
        add(new Hat("top-hat", "Zylinder", "Klassischer schwarzer Zylinder mit rotem Band.", "🎩", false, 0f, 0f, List.of(b(-6, -9, -6, 12, 1, 12, 0x111113), b(-4, -16, -4, 8, 7, 8, 0x1a1a1e), b(-4.3, -11, -4.3, 8.6, 2, 8.6, 0xe11d2e))));
        add(new Hat("chaos-crown", "Chaos-Krone", "Schwarze Krone mit glühenden roten Zacken.", "👑", false, 0f, 0f, List.of(b(-4.5, -11, -4.5, 9, 3, 9, 0x1a0a0d), b(-4.5, -13, -4.5, 2, 2, 2, 0xe11d2e), b(2.5, -13, -4.5, 2, 2, 2, 0xe11d2e), b(-4.5, -13, 2.5, 2, 2, 2, 0xe11d2e), b(2.5, -13, 2.5, 2, 2, 2, 0xe11d2e), b(-1, -14, -4.5, 2, 3, 1, 0xff4d5e))));
        add(new Hat("gold-crown", "Goldkrone", "Königliche Krone aus Gold mit rotem Rubin.", "👑", false, 0f, 0f, List.of(b(-4.5, -11, -4.5, 9, 3, 9, 0xf5c342), b(-4.5, -13, -4.5, 2, 2, 2, 0xf5c342), b(2.5, -13, -4.5, 2, 2, 2, 0xf5c342), b(-4.5, -13, 2.5, 2, 2, 2, 0xf5c342), b(2.5, -13, 2.5, 2, 2, 2, 0xf5c342), b(-1, -13, -4.6, 2, 2, 1, 0xe11d2e))));
        add(new Hat("halo", "Heiligenschein", "Leuchtender Ring, der langsam über dem Kopf schwebt und rotiert.", "😇", true, 1.2f, 0.35f, List.of(b(-4, -11, -4, 8, 0.6, 1, 0xfff1b8), b(-4, -11, 3, 8, 0.6, 1, 0xfff1b8), b(-4, -11, -3, 1, 0.6, 6, 0xfff1b8), b(3, -11, -3, 1, 0.6, 6, 0xfff1b8))));
        add(new Hat("horns", "Teufelshörner", "Zwei rote Hörner.", "😈", false, 0f, 0f, List.of(b(-4.5, -10, -1, 2, 2, 2, 0x8f1b22), b(-4, -12, -0.5, 1.4, 2, 1.4, 0xe11d2e), b(-3.6, -13.5, -0.2, 0.8, 1.6, 0.8, 0xff6b6b), b(2.5, -10, -1, 2, 2, 2, 0x8f1b22), b(2.6, -12, -0.5, 1.4, 2, 1.4, 0xe11d2e), b(2.8, -13.5, -0.2, 0.8, 1.6, 0.8, 0xff6b6b))));
        add(new Hat("wizard", "Zaubererhut", "Spitzhut in Mitternachtsrot.", "🧙", false, 0f, 0f, List.of(b(-6.5, -9, -6.5, 13, 1, 13, 0x2a0b0f), b(-4, -13, -4, 8, 4, 8, 0x3a0d12), b(-3, -16, -3, 6, 3, 6, 0x3a0d12), b(-2, -18.5, -2, 4, 2.5, 4, 0x3a0d12), b(-1, -20.5, -1, 2, 2, 2, 0xe11d2e))));
        add(new Hat("cap", "Chaos-Cap", "Rote Baseballcap mit schwarzem Schild.", "🧢", false, 0f, 0f, List.of(b(-4.5, -9.5, -4.5, 9, 2.5, 9, 0xe11d2e), b(-4, -7, -8, 8, 0.8, 4, 0x111113), b(-0.6, -10.2, -0.6, 1.2, 0.8, 1.2, 0x111113))));
        add(new Hat("headphones", "Kopfhörer", "Gaming-Headset mit rotem Licht.", "🎧", false, 0f, 0f, List.of(b(-4.5, -9.5, -1, 9, 1, 2, 0x111113), b(-5.6, -6, -1.6, 1.6, 4, 3.2, 0x1a1a1e), b(4, -6, -1.6, 1.6, 4, 3.2, 0x1a1a1e), b(-5.9, -5, -0.6, 0.4, 2, 1.2, 0xe11d2e), b(5.5, -5, -0.6, 0.4, 2, 1.2, 0xe11d2e))));
        add(new Hat("bunny", "Hasenohren", "Zwei lange Ohren.", "🐰", false, 0f, 0f, List.of(b(-3.5, -15, -0.6, 2, 7, 1.2, 0xf4f1f2), b(-3, -14, -0.3, 1, 5, 0.6, 0xf9a8b8), b(1.5, -15, -0.6, 2, 7, 1.2, 0xf4f1f2), b(2, -14, -0.3, 1, 5, 0.6, 0xf9a8b8))));
        add(new Hat("viking", "Wikingerhelm", "Eisenhelm mit Hörnern.", "⛑", false, 0f, 0f, List.of(b(-4.5, -9.5, -4.5, 9, 3, 9, 0x9ca3af), b(-4.5, -7, -4.5, 9, 0.6, 9, 0x4b5563), b(-7, -11, -1, 2.5, 2, 2, 0xf4f1f2), b(4.5, -11, -1, 2.5, 2, 2, 0xf4f1f2), b(-6, -13, -0.6, 1.4, 2.2, 1.2, 0xf4f1f2), b(4.6, -13, -0.6, 1.4, 2.2, 1.2, 0xf4f1f2))));
        add(new Hat("propeller", "Propellerkappe", "Bunte Kappe mit Propeller.", "🚁", false, 0f, 0f, List.of(b(-4.5, -9.5, -4.5, 9, 2.5, 9, 0xe11d2e), b(-4.5, -9.5, -4.5, 4.5, 2.5, 4.5, 0xfbbf24), b(0, -9.5, 0, 4.5, 2.5, 4.5, 0x3b82f6), b(-0.4, -11.5, -0.4, 0.8, 2, 0.8, 0x111113), b(-4, -12, -0.4, 8, 0.5, 0.8, 0xf4f1f2))));
        add(new Hat("santa", "Weihnachtsmütze", "Rote Mütze mit weißem Bommel.", "🎅", false, 0f, 0f, List.of(b(-4.6, -9.5, -4.6, 9.2, 2, 9.2, 0xf4f1f2), b(-4, -13, -4, 8, 3.5, 8, 0xe11d2e), b(-2.5, -15.5, -2.5, 5, 2.5, 5, 0xe11d2e), b(-0.5, -17, 1, 3, 2, 3, 0xe11d2e), b(1, -18.5, 2.5, 2.5, 2.5, 2.5, 0xf4f1f2))));
        add(new Hat("dragon-helm", "Drachenhelm", "Dunkler Schuppenhelm mit Stachelkamm, geschwungenen Hörnern und glühenden Augen.", "🐲", false, 0f, 0f, List.of(b(-4.6, -9.2, -4.6, 9.2, 4, 9.2, 0x1f2937), b(-4.8, -6.2, -5, 9.6, 1, 1, 0x111827), b(-0.6, -11, -3, 1.2, 2, 1.2, 0x334155), b(-0.6, -11.5, -0.5, 1.2, 2.5, 1.2, 0x334155), b(-0.6, -11, 2, 1.2, 2, 1.2, 0x334155), b(-3, -6.5, -5.1, 2, 1, 0.5, 0xff2d44), b(1, -6.5, -5.1, 2, 1, 0.5, 0xff2d44), b(-6.2, -9.5, -1, 2, 2, 2, 0x0f172a), b(-6.8, -11.5, -0.8, 1.6, 2.2, 1.6, 0x1e293b), b(-7.6, -13.3, -0.6, 1.2, 2, 1.2, 0x334155), b(4.2, -9.5, -1, 2, 2, 2, 0x0f172a), b(5.2, -11.5, -0.8, 1.6, 2.2, 1.6, 0x1e293b), b(6.4, -13.3, -0.6, 1.2, 2, 1.2, 0x334155))));
        add(new Hat("chaos-visor", "Chaos-Visor", "Cyber-Visor mit grell leuchtendem Streifen in Chaos-Rot.", "🕶", true, 0f, 0f, List.of(b(-4.8, -5, -4.9, 9.6, 2.4, 1.2, 0x0b0b10), b(-4.9, -5, -4.5, 0.8, 2.4, 6, 0x0b0b10), b(4.1, -5, -4.5, 0.8, 2.4, 6, 0x0b0b10), b(-4.4, -4.3, -5.2, 8.8, 0.8, 0.4, 0xff1f3d), b(-4.6, -4.6, 3.6, 9.2, 1.4, 1, 0x1a1a1e))));
        add(new Hat("cat-ears", "Katzenohren", "Schwarze Katzenohren mit rosa Innenseite.", "🐱", false, 0f, 0f, List.of(b(-4.2, -10.5, -1, 2.8, 3, 1.6, 0x111113), b(-3.6, -12.5, -0.7, 1.6, 2.2, 1, 0x111113), b(-3.4, -10.2, -1.3, 1.2, 2, 0.4, 0xf472b6), b(1.4, -10.5, -1, 2.8, 3, 1.6, 0x111113), b(2, -12.5, -0.7, 1.6, 2.2, 1, 0x111113), b(2.2, -10.2, -1.3, 1.2, 2, 0.4, 0xf472b6))));
        add(new Hat("wolf-ears", "Wolfsohren", "Graue Wolfsohren mit hellem Fell.", "🐺", false, 0f, 0f, List.of(b(-4.4, -10.8, -1.2, 2.6, 3.2, 2, 0x4b5563), b(-3.8, -12.6, -0.9, 1.4, 2, 1.4, 0x374151), b(-3.6, -10.5, -1.5, 1.2, 2.2, 0.4, 0xd1d5db), b(1.8, -10.8, -1.2, 2.6, 3.2, 2, 0x4b5563), b(2.4, -12.6, -0.9, 1.4, 2, 1.4, 0x374151), b(2.4, -10.5, -1.5, 1.2, 2.2, 0.4, 0xd1d5db))));
        add(new Hat("pirate", "Piratenhut", "Schwarzer Dreispitz mit Totenkopf und Goldborte.", "🏴‍☠️", false, 0f, 0f, List.of(b(-6.5, -9, -6.5, 13, 1, 13, 0x111113), b(-4.2, -12.5, -4.2, 8.4, 3.5, 8.4, 0x1a1a1e), b(-7, -11.5, -2, 1.2, 3.5, 6, 0x1a1a1e), b(5.8, -11.5, -2, 1.2, 3.5, 6, 0x1a1a1e), b(-5, -11.8, -5.2, 10, 3.2, 1.2, 0x1a1a1e), b(-1.2, -11.2, -5.6, 2.4, 2, 0.5, 0xf4f1f2), b(-0.9, -10.8, -5.8, 0.6, 0.6, 0.3, 0x111113), b(0.3, -10.8, -5.8, 0.6, 0.6, 0.3, 0x111113), b(-5, -8.8, -5.3, 10, 0.4, 1.3, 0xf5c342))));
        add(new Hat("antlers", "Geweih", "Braunes Hirschgeweih mit verzweigten Enden.", "🦌", false, 0f, 0f, List.of(b(-3.8, -13, -0.6, 1.2, 5, 1.2, 0x6b4423), b(-6, -12, -0.5, 2.4, 1, 1, 0x7c4f2a), b(-6.2, -14.5, -0.5, 1, 3, 1, 0x7c4f2a), b(-3.2, -15.5, -0.5, 1, 3, 1, 0x8b5a2b), b(-1.6, -13.5, -0.5, 1.6, 1, 1, 0x7c4f2a), b(2.6, -13, -0.6, 1.2, 5, 1.2, 0x6b4423), b(3.6, -12, -0.5, 2.4, 1, 1, 0x7c4f2a), b(5.2, -14.5, -0.5, 1, 3, 1, 0x7c4f2a), b(2.2, -15.5, -0.5, 1, 3, 1, 0x8b5a2b), b(-0, -13.5, -0.5, 1.6, 1, 1, 0x7c4f2a))));
        add(new Hat("flame-crown", "Flammenkrone", "Lodernde Krone aus glühenden Flammen – schwebt leicht.", "🔥", true, 0f, 0.3f, List.of(b(-4.5, -10.5, -4.5, 9, 2, 9, 0x7a0f17), b(-3.5, -13.5, -4.6, 1.4, 3, 1, 0xff6a00), b(-0.7, -15, -4.6, 1.4, 4.5, 1, 0xffd166), b(2.1, -13.5, -4.6, 1.4, 3, 1, 0xff6a00), b(-4.6, -13, -0.7, 1, 2.5, 1.4, 0xff6a00), b(3.6, -13, -0.7, 1, 2.5, 1.4, 0xff6a00), b(-0.7, -13.5, 3.6, 1.4, 3, 1, 0xff8a1f), b(-4.6, -12.5, -4.6, 1, 2, 1, 0xe11d2e), b(3.6, -12.5, -4.6, 1, 2, 1, 0xe11d2e), b(-4.6, -12.5, 3.6, 1, 2, 1, 0xe11d2e), b(3.6, -12.5, 3.6, 1, 2, 1, 0xe11d2e))));
        add(new Hat("astronaut", "Astronautenhelm", "Weißer Raumhelm mit dunklem Visier und roter Antenne.", "👩‍🚀", false, 0f, 0f, List.of(b(-5.5, -9.5, -5.5, 11, 10.5, 11, 0xf4f1f2), b(-4, -7, -6, 8, 5, 0.8, 0x1e293b), b(-3, -6, -6.2, 2, 1, 0.3, 0x93c5fd), b(-5.8, 0.8, -5.8, 11.6, 1, 11.6, 0x9ca3af), b(5.4, -11, -0.4, 0.6, 3, 0.8, 0x9ca3af), b(5.2, -11.6, -0.6, 1, 0.8, 1.2, 0xe11d2e))));
        add(new Hat("mushroom", "Pilzhut", "Roter Fliegenpilz mit weißen Punkten.", "🍄", false, 0f, 0f, List.of(b(-6.5, -11.5, -6.5, 13, 3.5, 13, 0xe11d2e), b(-4.5, -13.5, -4.5, 9, 2, 9, 0xe11d2e), b(-4.5, -8.2, -4.5, 9, 0.4, 9, 0xf5e0c3), b(-5, -11.6, -6.7, 2, 2, 0.4, 0xf4f1f2), b(2, -12.6, -6.7, 2.4, 2.4, 0.4, 0xf4f1f2), b(-2, -13.7, -2, 3, 0.4, 3, 0xf4f1f2), b(-6.7, -11, 0, 0.4, 2, 2.4, 0xf4f1f2), b(6.3, -12, -3, 0.4, 2, 2, 0xf4f1f2), b(-1, -11.5, 6.3, 2, 2, 0.4, 0xf4f1f2))));
        add(new Hat("knight-helm", "Ritterhelm", "Stahlhelm mit Visierschlitz und rotem Federbusch.", "🛡", false, 0f, 0f, List.of(b(-4.6, -9.5, -4.6, 9.2, 5, 9.2, 0x9ca3af), b(-4.7, -4.6, -5, 9.4, 3.6, 1, 0x6b7280), b(-3.5, -3.4, -5.2, 7, 0.6, 0.3, 0x111113), b(-0.4, -10.5, -4.6, 0.8, 1, 9.2, 0xd1d5db), b(-4.8, -4.6, -2, 0.6, 4, 6, 0x6b7280), b(4.2, -4.6, -2, 0.6, 4, 6, 0x6b7280), b(-0.6, -14.5, -1, 1.2, 5, 2, 0xe11d2e), b(-0.6, -14, 1, 1.2, 3, 3, 0xb91c1c), b(-0.6, -13, 4, 1.2, 2, 2, 0xe11d2e))));
        add(new Hat("flower-crown", "Blumenkranz", "Grüner Kranz mit rosa, gelben und weißen Blüten.", "🌸", false, 0f, 0f, List.of(b(-4.5, -9, -4.8, 9, 1, 1, 0x2e7d32), b(-4.5, -9, 3.8, 9, 1, 1, 0x2e7d32), b(-4.8, -9, -3.8, 1, 1, 7.6, 0x2e7d32), b(3.8, -9, -3.8, 1, 1, 7.6, 0x2e7d32), b(-3.5, -10, -5.2, 1.6, 1.6, 1.4, 0xf472b6), b(0.8, -10.2, -5.2, 1.6, 1.8, 1.4, 0xfbbf24), b(-1.4, -9.8, -5.3, 1.2, 1.2, 1.4, 0xf4f1f2), b(-5.4, -10, -1, 1.4, 1.6, 1.6, 0xf472b6), b(4, -10, 0.5, 1.4, 1.6, 1.6, 0xfbbf24), b(3.6, -9.8, -3, 1.2, 1.2, 1.2, 0xf4f1f2), b(-1, -9.9, 4, 1.6, 1.4, 1.4, 0xf472b6), b(-4, -9.3, -5.1, 1, 0.5, 1, 0x4ade80), b(2.6, -9.4, -5.1, 1, 0.5, 1, 0x4ade80))));
        add(new Hat("neon-halo", "Neon-Ring", "Rotierender Chaos-roter Neonring – leuchtet im Dunkeln.", "⭕", true, 2.5f, 0.4f, List.of(b(-5, -11.5, -5, 10, 0.5, 1, 0xff1f3d), b(-5, -11.5, 4, 10, 0.5, 1, 0xff1f3d), b(-5, -11.5, -4, 1, 0.5, 8, 0xff1f3d), b(4, -11.5, -4, 1, 0.5, 8, 0xff1f3d), b(-1, -12.2, -5.3, 2, 0.4, 0.6, 0xffffff))));
        add(new Hat("ice-crown", "Eiskrone", "Kristallkrone aus leuchtendem Eis.", "❄", true, 0f, 0f, List.of(b(-4.5, -10.5, -4.5, 9, 2, 9, 0xbae6fd), b(-4.4, -13, -4.4, 1.4, 2.5, 1.4, 0xe0f2fe), b(3, -13, -4.4, 1.4, 2.5, 1.4, 0xe0f2fe), b(-4.4, -13, 3, 1.4, 2.5, 1.4, 0xe0f2fe), b(3, -13, 3, 1.4, 2.5, 1.4, 0xe0f2fe), b(-0.8, -15, -4.4, 1.6, 4.5, 1.2, 0xf0f9ff), b(-0.5, -16.5, -4.2, 1, 1.5, 0.8, 0xffffff))));
    }

    private static void add(Hat h) { HATS.put(h.id(), h); }

    public static List<Hat> all() { return List.copyOf(HATS.values()); }
    public static Hat byId(String id) { return id == null ? null : HATS.get(id); }
}
