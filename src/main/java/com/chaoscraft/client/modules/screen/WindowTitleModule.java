package com.chaoscraft.client.modules.screen;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.StringSetting;

/** Window Title: eigener Fenstertitel mit Platzhaltern. */
public class WindowTitleModule extends Module {

    private final StringSetting title = add(new StringSetting("Titel", "Platzhalter: {name} {version} {mc} {server}", "Chaos Client {version} · {name}", 120));
    private String last = "";

    public WindowTitleModule() {
        super("Window Title", "Eigener Fenstertitel, z.B. „Chaos Client · Name“.", Category.SCREEN, "▣");
        tags("fenster", "titel", "window", "title");
    }

    private String build() {
        String server = mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : mc.isInSingleplayer() ? "Einzelspieler" : "Menü";
        return title.get()
            .replace("{name}", mc.getSession().getUsername())
            .replace("{version}", ChaosClient.VERSION)
            .replace("{mc}", net.minecraft.SharedConstants.getGameVersion().name())
            .replace("{server}", server);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.player.age % 20 != 0) return;
        String t = build();
        if (!t.equals(last)) { last = t; mc.getWindow().setTitle(t); }
    }

    @Override public void onDisable() { last = ""; try { mc.updateWindowTitle(); } catch (Throwable ignored) {} }
}
