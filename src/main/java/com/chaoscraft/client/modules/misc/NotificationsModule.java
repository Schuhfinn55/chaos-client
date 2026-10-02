package com.chaoscraft.client.modules.misc;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.IntSetting;

/** Chaos-Notifications: Popups oben rechts – Dauer, Sound, Modul-Toggles. */
public class NotificationsModule extends Module {

    private final IntSetting duration = add(new IntSetting("Dauer", "Anzeigedauer in Sekunden.", 4, 1, 15).suffix(" s"));
    private final BooleanSetting sound = add(new BooleanSetting("Sound", "Ton bei Notifications.", true));
    private final BooleanSetting toggles = add(new BooleanSetting("Modul-Toggles", "Beim Ein-/Ausschalten von Modulen benachrichtigen.", true));

    public NotificationsModule() {
        super("Notifications", "Chaos-Popups: [CHAOS] Dein Cape wurde aktiviert.", Category.MISC, "✉");
        setEnabled(true);
        tags("notification", "popup", "hinweis", "toast");
    }

    public long durationMs() { return duration.getInt() * 1000L; }
    public boolean sound() { return sound.isEnabled(); }
    public boolean showToggles() { return toggles.isEnabled(); }
}
