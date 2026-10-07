package com.chaoscraft.client;

import com.chaoscraft.client.commands.ChaosCommands;
import com.chaoscraft.client.config.ConfigManager;
import com.chaoscraft.client.config.SharedData;
import com.chaoscraft.client.core.KeyManager;
import com.chaoscraft.client.core.ModuleManager;
import com.chaoscraft.client.cosmetics.CapeManager;
import com.chaoscraft.client.emotes.EmoteManager;
import com.chaoscraft.client.hud.HudRenderer;
import com.chaoscraft.client.modules.audio.AudioModule;
import com.chaoscraft.client.modules.general.MenuModule;
import com.chaoscraft.client.modules.gui.EscMenuModule;
import com.chaoscraft.client.music.MusicPlayer;
import com.chaoscraft.client.notifications.NotificationManager;
import com.chaoscraft.client.screenshot.ScreenshotManager;
import com.chaoscraft.client.server.ServerManager;
import com.chaoscraft.client.social.SocialManager;
import com.chaoscraft.client.waypoints.WaypointManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chaos Client – Einstiegspunkt. Verbindet Module, Konfiguration (Profile),
 * Tasten, HUD, Cosmetics, Notifications, Server, Waypoints, Musik, Emotes,
 * Social, Screenshots und Chat-Befehle.
 */
public class ChaosClient implements ClientModInitializer {

    public static final String MOD_ID = "chaosclient";
    public static final String NAME = "Chaos Client";
    public static final String VERSION = "2.4.2";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ChaosClient instance;

    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private KeyManager keyManager;
    private NotificationManager notifications;
    private WaypointManager waypoints;
    private ServerManager servers;
    private ScreenshotManager screenshots;
    private MusicPlayer music;
    private EmoteManager emotes;
    private SocialManager social;
    private Screen pendingScreen;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("[{}] v{} startet.", NAME, VERSION);

        SharedData.reload();
        notifications = new NotificationManager();
        moduleManager = new ModuleManager();
        moduleManager.init();

        configManager = new ConfigManager(this);
        configManager.load();

        waypoints = new WaypointManager();
        waypoints.load();
        servers = new ServerManager();
        servers.register();
        screenshots = new ScreenshotManager();
        music = new MusicPlayer();
        emotes = new EmoteManager();
        emotes.register();
        social = new SocialManager();

        keyManager = new KeyManager(moduleManager);
        keyManager.register();
        HudRenderer.init();
        ChaosCommands.register();
        CapeManager.get().init(MinecraftClient.getInstance());
        com.chaoscraft.client.cosmetics.EffectTicker.register();
        net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (renderer instanceof net.minecraft.client.render.entity.PlayerEntityRenderer<?> player) {
                helper.register(new com.chaoscraft.client.cosmetics.HatFeatureRenderer(player));
                helper.register(new com.chaoscraft.client.cosmetics.WingsFeatureRenderer(player));
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            moduleManager.tickAll();
            if (pendingScreen != null) {
                Screen s = pendingScreen;
                pendingScreen = null;
                client.setScreen(s);
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            configManager.save();
            waypoints.save();
            music.stop();
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            SharedData.reload();
            client.execute(() -> {
                EscMenuModule.resetHint();
                if (SharedData.get().present) {
                    notifications.push("CHAOS", "Chaos Client " + VERSION + " aktiv · " + KeyManagerHint(), NotificationManager.Kind.INFO);
                }
            });
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> keyManager.releaseAll());
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> EscMenuModule.onScreenInit(screen));

        LOGGER.info("[{}] bereit – {} Module, Profil '{}'.", NAME, moduleManager.getModules().size(), configManager.getActiveProfile());
    }

    private String KeyManagerHint() {
        return com.chaoscraft.client.settings.KeybindSetting.keyName(getMenuKey()) + " öffnet das Menü";
    }

    /** Taste des Chaos-Menüs: Modul-Einstellung, sonst Launcher-Vorgabe, sonst RIGHT SHIFT. */
    public int getMenuKey() {
        MenuModule m = moduleManager.get(MenuModule.class);
        if (m != null && m.getKeyCode() != -1) return m.getKeyCode();
        if (SharedData.get().menuKey > 0) return SharedData.get().menuKey;
        return GLFW.GLFW_KEY_RIGHT_SHIFT;
    }

    /** Öffnet einen Screen im nächsten Tick (für Chat-Befehle). */
    public void openScreenNextTick(Screen s) { pendingScreen = s; }

    public static ChaosClient get() { return instance; }
    public ModuleManager getModuleManager() { return moduleManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public KeyManager getKeyManager() { return keyManager; }
    public NotificationManager getNotifications() { return notifications; }
    public WaypointManager getWaypoints() { return waypoints; }
    public ServerManager getServers() { return servers; }
    public ScreenshotManager getScreenshots() { return screenshots; }
    public MusicPlayer getMusic() { return music; }
    public EmoteManager getEmotes() { return emotes; }
    public SocialManager getSocial() { return social; }
    public AudioModule getAudio() {
        AudioModule a = moduleManager.get(AudioModule.class);
        return a != null ? a : new AudioModule();
    }
}
