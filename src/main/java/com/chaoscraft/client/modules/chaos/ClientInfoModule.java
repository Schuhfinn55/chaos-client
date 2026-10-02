package com.chaoscraft.client.modules.chaos;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.compat.Compat;
import com.chaoscraft.client.config.SharedData;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.StringSetting;

/** Chaos Client: Version, Launcher-Verbindung, Minecraft-Version, Lizenzhinweise. */
public class ClientInfoModule extends Module {

    private final StringSetting version = add(new StringSetting("Client", "", "Chaos Client " + ChaosClient.VERSION));
    private final StringSetting minecraft = add(new StringSetting("Minecraft", "", Compat.version()));
    private final StringSetting launcher = add(new StringSetting("Launcher", "", "—"));
    private final StringSetting licenses = add(new StringSetting("Open Source", "", "Fabric API & Loader (Apache-2.0), Mixin (MIT), Gson (Apache-2.0)"));

    public ClientInfoModule() {
        super("Chaos Client", "Version, Launcher-Verbindung und Lizenzhinweise.", Category.CHAOS, "✸");
        alwaysOn();
        tags("version", "info", "about", "lizenz", "license", "update");
    }

    @Override
    public void onTick() {
        SharedData sd = SharedData.get();
        String l = sd.present ? "Chaos Launcher " + sd.launcherVersion + (sd.profileName.isEmpty() ? "" : " · Profil " + sd.profileName) + (sd.accountName.isEmpty() ? "" : " · " + sd.accountName) : "Nicht über den Chaos Launcher gestartet";
        if (!launcher.get().equals(l)) launcher.set(l);
        if (!minecraft.get().equals(Compat.version())) minecraft.set(Compat.version());
    }
}
