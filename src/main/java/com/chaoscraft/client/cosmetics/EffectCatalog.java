package com.chaoscraft.client.cosmetics;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Vorgefertigte Partikel-Effekte – identisch zu src/lib/builtinEffects.ts im Launcher. */
public final class EffectCatalog {
    private EffectCatalog() {}

    public enum Pattern { RING, RISE, ORBIT, RAIN, BURST, FEET }

    public record Effect(String id, String name, String description, String icon, int color, Pattern pattern) {}

    private static final Map<String, Effect> EFFECTS = new LinkedHashMap<>();

    static {
        add("chaos-aura", "Chaos-Aura", "Roter Partikelring, der um dich kreist.", "🔴", 0xe11d2e, Pattern.RING);
        add("flame-feet", "Flammenschritte", "Flammen an deinen Füßen.", "🔥", 0xff6a00, Pattern.FEET);
        add("soul-fire", "Seelenfeuer", "Blaue Seelenflammen um dich.", "💙", 0x38bdf8, Pattern.RISE);
        add("enchant-orbit", "Verzauberung", "Runen, die um deinen Kopf kreisen.", "✨", 0xc084fc, Pattern.ORBIT);
        add("hearts", "Herzen", "Herzen steigen auf.", "❤", 0xf43f5e, Pattern.RISE);
        add("notes", "Noten", "Bunte Musiknoten.", "🎵", 0x22c55e, Pattern.RISE);
        add("cherry", "Kirschblüten", "Blütenblätter regnen herab.", "🌸", 0xf9a8d4, Pattern.RAIN);
        add("end-rod", "End-Spirale", "Weiße Lichtspirale.", "⚪", 0xf8fafc, Pattern.ORBIT);
        add("sparks", "Funken", "Elektrische Funken.", "⚡", 0xfde047, Pattern.BURST);
        add("portal", "Portal", "Lila Portalwirbel.", "🌀", 0x7c3aed, Pattern.RING);
        add("snow", "Schnee", "Sanfter Schneefall.", "❄", 0xffffff, Pattern.RAIN);
        add("glow", "Glühwürmchen", "Gelbgrüne Lichtpunkte.", "🟢", 0xbef264, Pattern.ORBIT);
        add("smoke", "Schattenrauch", "Dunkler Rauch um dich.", "🌫", 0x3f3f46, Pattern.RISE);
        add("totem", "Totem", "Totem-Partikel in Gold und Grün.", "🟡", 0xfbbf24, Pattern.BURST);
    }

    private static void add(String id, String name, String desc, String icon, int color, Pattern p) {
        EFFECTS.put(id, new Effect(id, name, desc, icon, 0xFF000000 | color, p));
    }

    public static List<Effect> all() { return List.copyOf(EFFECTS.values()); }
    public static Effect byId(String id) { return id == null ? null : EFFECTS.get(id); }
}
