package com.onyx.visuals;

import com.onyx.visuals.event.KeyInputHandler;
import com.onyx.visuals.module.ModuleManager;
import com.onyx.visuals.render.RenderManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnyxVisuals implements ClientModInitializer {

    public static final String NAME = "Chaos Client";
    public static final String VERSION = "1.1.0";
    public static final Logger LOGGER = LoggerFactory.getLogger("chaosclient");

    private static OnyxVisuals instance;

    private ModuleManager moduleManager;
    private ConfigManager configManager;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("[{}] v{} booting.", NAME, VERSION);

        moduleManager = new ModuleManager();
        moduleManager.init();

        configManager = new ConfigManager(this);
        configManager.load();

        new KeyInputHandler(moduleManager).register();
        RenderManager.init();
        // Chaos-Cosmetics: Capes aus dem Launcher-Export laden
        com.onyx.visuals.cosmetics.CapeManager.get().init(net.minecraft.client.MinecraftClient.getInstance());

        ClientTickEvents.END_CLIENT_TICK.register(client -> moduleManager.tickAll());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> configManager.save());

        LOGGER.info("[{}] ready. {} modules loaded.", NAME, moduleManager.getModules().size());
    }

    public static OnyxVisuals getInstance() { return instance; }
    public ModuleManager getModuleManager() { return moduleManager; }
    public ConfigManager getConfigManager() { return configManager; }
}
