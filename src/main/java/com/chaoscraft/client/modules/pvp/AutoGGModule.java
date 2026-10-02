package com.chaoscraft.client.modules.pvp;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.IntSetting;
import com.chaoscraft.client.settings.StringSetting;

/** AutoGG: nach Rundenende automatisch „GG“ senden (konfigurierbare Nachricht/Verzögerung). */
public class AutoGGModule extends Module {

    private final IntSetting delay = add(new IntSetting("Verzögerung", "Sekunden bis zum Senden.", 1, 0, 5).suffix(" s"));
    private final StringSetting message = add(new StringSetting("Nachricht", "Text, der gesendet wird.", "GG", 40));
    private final StringSetting triggers = add(new StringSetting("Auslöser", "Kommagetrennte Wörter, die das Rundenende markieren.", "game over,winner,has won,victory,gewonnen,hat gewonnen", 200));

    private long triggerTime = -1;
    private long lastSent;

    public AutoGGModule() {
        super("AutoGG", "Sendet automatisch GG, wenn eine Runde endet.", Category.PVP, "✌");
        tags("gg", "chat", "auto");
    }

    /** Vom ChatHudMixin aufgerufen. */
    public void onChatMessage(String raw) {
        if (!isEnabled()) return;
        String msg = raw.toLowerCase();
        for (String t : triggers.get().toLowerCase().split(",")) {
            String w = t.trim();
            if (!w.isEmpty() && msg.contains(w)) { triggerTime = System.currentTimeMillis() + delay.getInt() * 1000L; return; }
        }
    }

    @Override
    public void onTick() {
        if (triggerTime > 0 && System.currentTimeMillis() >= triggerTime) {
            triggerTime = -1;
            if (System.currentTimeMillis() - lastSent < 10_000) return;
            if (mc.player != null && mc.player.networkHandler != null && !message.get().isBlank()) {
                mc.player.networkHandler.sendChatMessage(message.get());
                lastSent = System.currentTimeMillis();
            }
        }
    }
}
