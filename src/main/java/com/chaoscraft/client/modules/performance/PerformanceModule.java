package com.chaoscraft.client.modules.performance;

import com.chaoscraft.client.compat.Compat;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.EnumSetting;
import com.chaoscraft.client.settings.IntSetting;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.particle.ParticlesMode;

/**
 * Performance Center: Performance Mode (LOW/BALANCED/HIGH/CUSTOM) und
 * einzelne Grafikoptionen. Alle Optionen sind reine Render-Einstellungen;
 * nichts davon beeinflusst Gameplay oder Serverregeln. Der Nutzer sieht im
 * Menü jederzeit, was aktiv ist.
 */
public class PerformanceModule extends Module {

    public enum Mode {
        LOW("Maximale FPS: Renderdistanz 6, Partikel minimal, keine Wolken/Schatten, VSync aus."),
        BALANCED("Ausgewogen: Renderdistanz 10, reduzierte Partikel, Wolken schnell."),
        HIGH("Beste Optik: Renderdistanz 16, alle Partikel, Wolken fancy, Schatten."),
        CUSTOM("Eigene Werte (unten einstellen).");
        private final String desc;
        Mode(String d) { desc = d; }
        public String description() { return desc; }
    }

    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Performance Mode", "Vorlage für die Grafikoptionen.", Mode.CUSTOM));
    private final IntSetting renderDistance = add(new IntSetting("Render-Distanz", "Sichtweite in Chunks.", 10, 2, 32));
    private final IntSetting simulationDistance = add(new IntSetting("Simulation-Distanz", "Simulierte Chunks.", 8, 5, 32));
    private final EnumSetting<ParticlesMode> particles = add(new EnumSetting<>("Partikel", "Partikelmenge.", ParticlesMode.ALL));
    private final BooleanSetting vsync = add(new BooleanSetting("VSync", "Vertikale Synchronisation.", true));
    private final BooleanSetting clouds = add(new BooleanSetting("Wolken", "Wolken rendern.", true));
    private final BooleanSetting entityShadows = add(new BooleanSetting("Entity-Schatten", "Schatten unter Entities.", true));
    private final IntSetting maxFps = add(new IntSetting("Max. FPS", "FPS-Limit (260 = unbegrenzt).", 260, 10, 260));
    private final BooleanSetting reduceAnimations = add(new BooleanSetting("Animationen reduzieren", "Chaos-Menü-Animationen aus (spart Leistung).", false));
    private final BooleanSetting entityCullingHint = add(new BooleanSetting("Entity Culling (Mod)", "Hinweis: Entity Culling wird über die Mod „EntityCulling“ bereitgestellt – im Launcher-Profil installieren.", true));
    private final BooleanSetting applyOnJoin = add(new BooleanSetting("Beim Beitreten anwenden", "Werte beim Start jeder Welt setzen.", false));

    private Mode lastApplied = Mode.CUSTOM;
    private boolean dirty;

    public PerformanceModule() {
        super("Performance", "Performance Center: FPS, Speicher, Chunks, Partikel und Performance Mode.", Category.PERFORMANCE, "⚡");
        alwaysOn();
        tags("fps", "lag", "optimierung", "render", "distance", "partikel", "vsync");
    }

    public Mode mode() { return mode.get(); }

    public void applyMode(Mode m) {
        mode.set(m);
        switch (m) {
            case LOW -> set(6, 5, ParticlesMode.MINIMAL, false, false, false, 260);
            case BALANCED -> set(10, 8, ParticlesMode.DECREASED, true, true, false, 260);
            case HIGH -> set(16, 12, ParticlesMode.ALL, true, true, true, 260);
            default -> {}
        }
        dirty = true;
        lastApplied = m;
    }

    private void set(int rd, int sd, ParticlesMode p, boolean vs, boolean cl, boolean sh, int fps) {
        renderDistance.setInt(rd); simulationDistance.setInt(sd); particles.set(p); vsync.set(vs); clouds.set(cl); entityShadows.set(sh); maxFps.setInt(fps);
    }

    /** Werte in die Minecraft-Optionen übertragen. */
    public void applyToGame() {
        GameOptions o = mc.options;
        if (o == null) return;
        o.getViewDistance().setValue(renderDistance.getInt());
        o.getSimulationDistance().setValue(simulationDistance.getInt());
        o.getParticles().setValue(particles.get());
        o.getEnableVsync().setValue(vsync.isEnabled());
        o.getCloudRenderMode().setValue(clouds.isEnabled() ? CloudRenderMode.FANCY : CloudRenderMode.OFF);
        o.getEntityShadows().setValue(entityShadows.isEnabled());
        o.getMaxFps().setValue(maxFps.getInt());
        o.write();
        if (reduceAnimations.isEnabled()) {
            var theme = getModuleManagerSafe();
            if (theme != null) { var s = theme.getSetting("Animationen"); if (s instanceof BooleanSetting b) b.set(false); }
        }
    }

    private Module getModuleManagerSafe() {
        var cc = com.chaoscraft.client.ChaosClient.get();
        return cc == null ? null : cc.getModuleManager().getByName("Chaos Theme");
    }

    @Override
    public void onTick() {
        if (mode.get() != lastApplied && mode.get() != Mode.CUSTOM) applyMode(mode.get());
        if (dirty) { applyToGame(); dirty = false; }
    }

    /** Beschreibung der aktiven Optimierungen (für Anzeige). */
    public String summary() {
        return "RD " + renderDistance.getInt() + " · SD " + simulationDistance.getInt() + " · Partikel " + particles.label() + " · VSync " + (vsync.isEnabled() ? "an" : "aus") + (Compat.hasSodium() ? " · Sodium" : "") + (Compat.hasEntityCulling() ? " · EntityCulling" : "");
    }
}
