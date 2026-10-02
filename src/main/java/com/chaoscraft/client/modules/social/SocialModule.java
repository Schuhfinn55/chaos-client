package com.chaoscraft.client.modules.social;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.social.SocialManager;
import com.chaoscraft.client.ui.screens.SocialScreen;

import java.util.HashSet;
import java.util.Set;

/** Social: Freundesliste, Online-Benachrichtigungen, Party (Server-Integration vorbereitet). */
public class SocialModule extends Module {

    private final BooleanSetting notifyOnline = add(new BooleanSetting("Online-Benachrichtigung", "„[CHAOS] Friend online.“ wenn ein Freund den Server betritt.", true));
    private final BooleanSetting partyEnabled = add(new BooleanSetting("Party-System", "Party-Funktionen aktivieren (benötigt Chaoscraft-Server).", true));

    private final Set<String> onlineBefore = new HashSet<>();
    private int tick;

    public SocialModule() {
        super("Social", "Freunde, Online-Status, Party.", Category.SOCIAL, "☻");
        alwaysOn();
        tags("friends", "freunde", "party", "online");
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled && isEnabled() && mc.currentScreen == null && mc.player != null) { mc.setScreen(new SocialScreen(null)); return; }
        super.setEnabled(true);
    }

    @Override
    public void onTick() {
        if (++tick % 40 != 0 || mc.getNetworkHandler() == null) return;
        Set<String> now = new HashSet<>();
        for (SocialManager.Friend f : ChaosClient.get().getSocial().friends()) if (f.online()) now.add(f.name());
        if (notifyOnline.isEnabled() && !onlineBefore.isEmpty()) {
            for (String n : now) if (!onlineBefore.contains(n)) ChaosClient.get().getNotifications().info("Friend online: " + n);
        }
        onlineBefore.clear();
        onlineBefore.addAll(now);
    }
}
