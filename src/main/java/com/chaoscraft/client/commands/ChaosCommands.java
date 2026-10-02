package com.chaoscraft.client.commands;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.emotes.EmoteManager;
import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.ui.screens.ChaosMenuScreen;
import com.chaoscraft.client.ui.screens.CosmeticsScreen;
import com.chaoscraft.client.ui.screens.HudEditorScreen;
import com.chaoscraft.client.ui.screens.KeybindManagerScreen;
import com.chaoscraft.client.ui.screens.MusicScreen;
import com.chaoscraft.client.ui.screens.PerformanceScreen;
import com.chaoscraft.client.ui.screens.ProfilesScreen;
import com.chaoscraft.client.ui.screens.ScreenshotsScreen;
import com.chaoscraft.client.ui.screens.ServerQuickScreen;
import com.chaoscraft.client.ui.screens.SocialScreen;
import com.chaoscraft.client.ui.screens.WaypointsScreen;
import com.chaoscraft.client.waypoints.WaypointManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * Chat-Befehle: /chaos, /chaos menu|hud|cosmetics|mods|settings|profile [name]|
 * waypoint add|remove|list|emote id|server|keybinds|music|perf|screenshots|social|help
 */
public final class ChaosCommands {
    private ChaosCommands() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            literal("chaos")
                .executes(ctx -> open(ChaosMenuScreen::new))
                .then(literal("menu").executes(ctx -> open(ChaosMenuScreen::new)))
                .then(literal("hud").executes(ctx -> open(() -> new HudEditorScreen(null))))
                .then(literal("cosmetics").executes(ctx -> open(() -> new CosmeticsScreen(null))))
                .then(literal("mods").executes(ctx -> open(() -> new ChaosMenuScreen(Category.GENERAL))))
                .then(literal("settings").executes(ctx -> open(() -> new ChaosMenuScreen(Category.GUI))))
                .then(literal("keybinds").executes(ctx -> open(() -> new KeybindManagerScreen(null))))
                .then(literal("server").executes(ctx -> open(() -> new ServerQuickScreen(null))))
                .then(literal("music").executes(ctx -> open(() -> new MusicScreen(null))))
                .then(literal("perf").executes(ctx -> open(() -> new PerformanceScreen(null))))
                .then(literal("screenshots").executes(ctx -> open(() -> new ScreenshotsScreen(null))))
                .then(literal("social").executes(ctx -> open(() -> new SocialScreen(null))))
                .then(literal("profile")
                    .executes(ctx -> open(() -> new ProfilesScreen(null)))
                    .then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "name").trim();
                        ChaosClient cc = ChaosClient.get();
                        if (!cc.getConfigManager().listProfiles().contains(name)) {
                            feedback(ctx, "§cProfil \"" + name + "\" existiert nicht. Vorhanden: " + String.join(", ", cc.getConfigManager().listProfiles()));
                            return 0;
                        }
                        cc.getConfigManager().switchProfile(name);
                        feedback(ctx, "§aProfil \"" + name + "\" geladen.");
                        return 1;
                    })))
                .then(literal("waypoint")
                    .executes(ctx -> open(() -> new WaypointsScreen(null)))
                    .then(literal("list").executes(ctx -> {
                        var all = ChaosClient.get().getWaypoints().all();
                        if (all.isEmpty()) { feedback(ctx, "§7Keine Waypoints."); return 1; }
                        for (WaypointManager.Waypoint w : all) feedback(ctx, "§c⚑ §f" + w.name + " §7(" + w.x + ", " + w.y + ", " + w.z + ")");
                        return 1;
                    }))
                    .then(literal("add").then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "name").trim();
                        WaypointManager.Waypoint w = ChaosClient.get().getWaypoints().add(name, 0xFFE11D2E);
                        if (w == null) { feedback(ctx, "§cWaypoint konnte nicht gesetzt werden."); return 0; }
                        feedback(ctx, "§aWaypoint \"" + name + "\" gesetzt bei " + w.x + ", " + w.y + ", " + w.z + ".");
                        return 1;
                    })))
                    .then(literal("remove").then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "name").trim();
                        boolean ok = ChaosClient.get().getWaypoints().remove(name);
                        feedback(ctx, ok ? "§aWaypoint \"" + name + "\" entfernt." : "§cKein Waypoint \"" + name + "\" gefunden.");
                        return ok ? 1 : 0;
                    }))))
                .then(literal("emote").then(argument("id", StringArgumentType.word()).executes(ctx -> {
                    String id = StringArgumentType.getString(ctx, "id");
                    EmoteManager em = ChaosClient.get().getEmotes();
                    EmoteManager.Emote e = em.byId(id);
                    if (e == null) {
                        StringBuilder sb = new StringBuilder();
                        for (EmoteManager.Emote x : em.all()) { if (sb.length() > 0) sb.append(", "); sb.append(x.id()); }
                        feedback(ctx, "§cUnbekanntes Emote. Verfügbar: " + sb);
                        return 0;
                    }
                    em.play(e);
                    feedback(ctx, "§aEmote \"" + e.name() + "\" gestartet.");
                    return 1;
                })))
                .then(literal("help").executes(ctx -> {
                    feedback(ctx, "§c✸ CHAOS CLIENT " + ChaosClient.VERSION);
                    feedback(ctx, "§7/chaos §f- Menü öffnen (oder Taste " + KeybindSetting.keyName(ChaosClient.get().getMenuKey()) + ")");
                    feedback(ctx, "§7/chaos hud §f- HUD Editor   §7/chaos cosmetics §f- Cosmetics");
                    feedback(ctx, "§7/chaos profile [Name] §f- Profile   §7/chaos waypoint add|remove|list Name");
                    feedback(ctx, "§7/chaos emote id §f- Emote   §7/chaos server|music|perf|keybinds|social|screenshots");
                    return 1;
                }))
        ));
    }

    private static int open(Supplier<Screen> screen) {
        ChaosClient.get().openScreenNextTick(screen.get());
        return 1;
    }

    private static void feedback(CommandContext<FabricClientCommandSource> ctx, String msg) {
        ctx.getSource().sendFeedback(Text.literal(msg));
    }
}
