package com.chaoscraft.client.modules.cosmetics;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.cosmetics.CapeManager;
import com.chaoscraft.client.settings.BooleanSetting;

import java.util.Map;

/**
 * Cosmetics: eigenes Cape, fremde Capes, Hüte, Wings, Back Items, Partikel,
 * Emotes ein-/ausschalten. Verbunden mit dem Cape-System des Launchers.
 */
public class CosmeticsModule extends Module {

    private final BooleanSetting showOwn = add(new BooleanSetting("Eigenes Cape", "Dein aktives Cape ingame rendern.", true));
    private final BooleanSetting showOthers = add(new BooleanSetting("Andere Capes", "Capes anderer Chaos-Spieler anzeigen.", true));
    private final BooleanSetting nameBadge = add(new BooleanSetting("Namens-Badge", "Chaos-Icon vor dem Namen (Nametag + Tab-Liste) bei allen Chaos-Client-Spielern.", true));
    private final BooleanSetting hats = add(new BooleanSetting("Hüte", "Hut-Cosmetics anzeigen (sobald verfügbar).", true));
    private final BooleanSetting wings = add(new BooleanSetting("Wings", "Flügel anzeigen (sobald verfügbar).", true));
    private final BooleanSetting back = add(new BooleanSetting("Back Items", "Rücken-Items anzeigen (sobald verfügbar).", true));
    private final BooleanSetting particles = add(new BooleanSetting("Partikel", "Partikel-Cosmetics anzeigen (sobald verfügbar).", true));
    private final BooleanSetting emotes = add(new BooleanSetting("Emotes", "Emotes anderer Spieler anzeigen.", true));

    public CosmeticsModule() {
        super("Cosmetics", "Capes, Hüte, Wings, Back Items, Partikel, Emotes – mit Vorschau im Cosmetics-Menü.", Category.COSMETICS, "✦");
        alwaysOn();
        tags("cape", "capes", "umhang", "kosmetik", "skin", "emote");
    }

    public BooleanSetting showOwn() { return showOwn; }
    public BooleanSetting showOthers() { return showOthers; }
    public BooleanSetting nameBadge() { return nameBadge; }

    public BooleanSetting categoryToggle(String tab) {
        return switch (tab) { case "HATS" -> hats; case "WINGS" -> wings; case "BACK" -> back; case "PARTICLES" -> particles; case "EMOTES" -> emotes; default -> null; };
    }

    public Map<String, Boolean> state() {
        return Map.of("own", showOwn.isEnabled(), "others", showOthers.isEnabled(), "hats", hats.isEnabled(), "wings", wings.isEnabled(), "back", back.isEnabled(), "particles", particles.isEnabled(), "emotes", emotes.isEnabled());
    }

    @Override
    public void onTick() {
        CapeManager.get().setShowOwn(showOwn.isEnabled());
        CapeManager.get().setShowOthers(showOthers.isEnabled());
    }
}
