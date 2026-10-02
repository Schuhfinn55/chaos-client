package com.chaoscraft.client.modules.screen;

import com.chaoscraft.client.compat.Compat;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;

/**
 * Replay / Clips: Gameplay aufzeichnen, Kamera frei bewegen, Zeitlupe,
 * Export. Benötigt eine Replay-Engine (z.B. die Mod „Replay Mod“), die der
 * Chaos Client erkennt. Ohne sie ist die Funktion in dieser Version
 * als nicht unterstützt markiert statt abzustürzen.
 */
public class ReplayModule extends Module {

    public ReplayModule() {
        super("Replay / Clips", "Gameplay aufzeichnen und als Replay ansehen (Zeitlupe, freie Kamera, Export).", Category.SCREEN, "⏺");
        boolean replayMod = Compat.isModLoaded("replaymod");
        requires(replayMod, "Replay-Aufzeichnung benötigt die Mod „Replay Mod“ für Minecraft " + Compat.version() + ". Installiere sie im Launcher unter Mods – der Chaos Client bindet sie dann ein.");
        tags("replay", "clip", "aufnahme", "record", "video");
    }

    @Override
    public void onEnable() {
        com.chaoscraft.client.ChaosClient.get().getNotifications().info("Replay Mod aktiv – Aufnahme über deren Steuerung.");
    }
}
