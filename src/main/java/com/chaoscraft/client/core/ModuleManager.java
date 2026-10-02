package com.chaoscraft.client.core;

import com.chaoscraft.client.modules.audio.AudioModule;
import com.chaoscraft.client.modules.audio.MusicPlayerModule;
import com.chaoscraft.client.modules.chaos.ClientInfoModule;
import com.chaoscraft.client.modules.chaos.KeybindManagerModule;
import com.chaoscraft.client.modules.chaos.ProfilesModule;
import com.chaoscraft.client.modules.chat.ChatModule;
import com.chaoscraft.client.modules.cosmetics.CosmeticsModule;
import com.chaoscraft.client.modules.cosmetics.EmotesModule;
import com.chaoscraft.client.modules.general.FullbrightModule;
import com.chaoscraft.client.modules.general.MenuModule;
import com.chaoscraft.client.modules.general.ZoomModule;
import com.chaoscraft.client.modules.gui.EscMenuModule;
import com.chaoscraft.client.modules.gui.ChaosThemeModule;
import com.chaoscraft.client.modules.hud.ArmorHud;
import com.chaoscraft.client.modules.hud.ClockHud;
import com.chaoscraft.client.modules.hud.CoordinatesHud;
import com.chaoscraft.client.modules.hud.CpsHud;
import com.chaoscraft.client.modules.hud.FpsHud;
import com.chaoscraft.client.modules.hud.HealthHud;
import com.chaoscraft.client.modules.hud.HeldItemHud;
import com.chaoscraft.client.modules.hud.KeystrokesHud;
import com.chaoscraft.client.modules.hud.MovementHud;
import com.chaoscraft.client.modules.hud.PotionHud;
import com.chaoscraft.client.modules.hud.ScoreboardModule;
import com.chaoscraft.client.modules.hud.ServerInfoHud;
import com.chaoscraft.client.modules.hud.TablistModule;
import com.chaoscraft.client.modules.misc.NotificationsModule;
import com.chaoscraft.client.modules.performance.PerformanceModule;
import com.chaoscraft.client.modules.player.FreeLookModule;
import com.chaoscraft.client.modules.player.PerspectiveModule;
import com.chaoscraft.client.modules.player.ToggleSneakModule;
import com.chaoscraft.client.modules.player.ToggleSprintModule;
import com.chaoscraft.client.modules.pvp.AutoGGModule;
import com.chaoscraft.client.modules.pvp.CrosshairModule;
import com.chaoscraft.client.modules.pvp.DamageIndicatorHud;
import com.chaoscraft.client.modules.pvp.PlayerInfoHud;
import com.chaoscraft.client.modules.screen.FullscreenModule;
import com.chaoscraft.client.modules.screen.ReplayModule;
import com.chaoscraft.client.modules.screen.ScreenshotModule;
import com.chaoscraft.client.modules.server.ServerQuickMenuModule;
import com.chaoscraft.client.modules.social.SocialModule;
import com.chaoscraft.client.modules.world.BlockOverlayModule;
import com.chaoscraft.client.modules.world.TimeChangerModule;
import com.chaoscraft.client.modules.world.WaypointsModule;
import com.chaoscraft.client.settings.KeybindSetting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Registry aller Module. Neue Module werden hier mit einer Zeile
 * registriert; Kategorie, Suche, Keybind-Konflikte und HUD-Liste leiten
 * sich automatisch ab.
 */
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        // ---- Allgemein ----
        register(new MenuModule());
        register(new FullbrightModule());
        register(new ZoomModule());
        // ---- HUD ----
        register(new FpsHud());
        register(new CpsHud());
        register(new KeystrokesHud());
        register(new CoordinatesHud());
        register(new ServerInfoHud());
        register(new ArmorHud());
        register(new PotionHud());
        register(new HeldItemHud());
        register(new HealthHud());
        register(new MovementHud());
        register(new ClockHud());
        register(new ScoreboardModule());
        register(new TablistModule());
        // ---- GUI ----
        register(new ChaosThemeModule());
        register(new EscMenuModule());
        // ---- Spieler ----
        register(new ToggleSprintModule());
        register(new ToggleSneakModule());
        register(new PerspectiveModule());
        register(new FreeLookModule());
        // ---- World ----
        register(new WaypointsModule());
        register(new BlockOverlayModule());
        register(new TimeChangerModule());
        // ---- PvP ----
        register(new CrosshairModule());
        register(new DamageIndicatorHud());
        register(new PlayerInfoHud());
        register(new AutoGGModule());
        // ---- Performance ----
        register(new PerformanceModule());
        // ---- Cosmetics ----
        register(new CosmeticsModule());
        register(new EmotesModule());
        // ---- Social ----
        register(new SocialModule());
        // ---- Chat ----
        register(new ChatModule());
        // ---- Server ----
        register(new ServerQuickMenuModule());
        // ---- Screen ----
        register(new FullscreenModule());
        register(new ScreenshotModule());
        register(new ReplayModule());
        // ---- Audio ----
        register(new AudioModule());
        register(new MusicPlayerModule());
        // ---- Misc ----
        register(new NotificationsModule());
        // ---- Chaos ----
        register(new ClientInfoModule());
        register(new ProfilesModule());
        register(new KeybindManagerModule());
    }

    public void register(Module module) { modules.add(module); }

    public List<Module> getModules() { return modules; }

    public List<Module> getByCategory(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).collect(Collectors.toList());
    }

    public List<HudModule> getHudModules() {
        return modules.stream().filter(m -> m instanceof HudModule).map(m -> (HudModule) m).collect(Collectors.toList());
    }

    public Module getByName(String name) {
        for (Module m : modules) if (m.getName().equalsIgnoreCase(name)) return m;
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> type) {
        for (Module m : modules) if (type.isInstance(m)) return (T) m;
        return null;
    }

    /** Sofort reagierende Suche über alle Module. */
    public List<Module> search(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return new ArrayList<>(modules);
        List<Module> out = new ArrayList<>();
        for (Module m : modules) if (m.matches(q)) out.add(m);
        out.sort(Comparator.comparing((Module m) -> !m.getName().toLowerCase(Locale.ROOT).startsWith(q)).thenComparing(Module::getName));
        return out;
    }

    public void tickAll() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    com.chaoscraft.client.ChaosClient.LOGGER.warn("[ChaosClient] Tick-Fehler in {}: {}", module.getName(), e.toString());
                }
            }
        }
    }

    /** Keybind-Konflikte: Taste → Module (nur Tasten mit >1 Modul). */
    public Map<Integer, List<Module>> keyConflicts() {
        Map<Integer, List<Module>> byKey = new TreeMap<>();
        for (Module m : modules) {
            int k = m.getKeyCode();
            if (k == KeybindSetting.NONE) continue;
            byKey.computeIfAbsent(k, x -> new ArrayList<>()).add(m);
        }
        byKey.values().removeIf(l -> l.size() < 2);
        return byKey;
    }

    public boolean hasConflict(Module m) {
        int k = m.getKeyCode();
        if (k == KeybindSetting.NONE) return false;
        int n = 0;
        for (Module o : modules) if (o.getKeyCode() == k) n++;
        return n > 1;
    }
}
