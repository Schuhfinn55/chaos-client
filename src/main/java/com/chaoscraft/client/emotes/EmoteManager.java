package com.chaoscraft.client.emotes;

import com.chaoscraft.client.ChaosClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;

/**
 * Emote-System (erweiterbar): jedes Emote ist eine {@link Emote}-Implementierung
 * mit Name, Dauer und Tick-Logik. Die mitgelieferten Emotes nutzen nur
 * clientseitig sichtbare Aktionen (Handschwung, Drehung). Server-synchronisierte
 * Animationen können später über die Cosmetics-API ergänzt werden.
 */
public final class EmoteManager {

    /** Basisschnittstelle für Emotes. */
    public interface Emote {
        String id();
        String name();
        String description();
        /** Dauer in Ticks. */
        int duration();
        /** Wird pro Tick mit dem Fortschritt (0..duration-1) aufgerufen. */
        void tick(MinecraftClient mc, int tick);
        default void onEnd(MinecraftClient mc) {}
    }

    private final List<Emote> emotes = new ArrayList<>();
    private Emote active;
    private int tick;
    private float startYaw;

    public EmoteManager() {
        emotes.add(new Simple("wave", "Winken", "Winkt mit der Haupthand.", 40, (mc, t) -> { if (t % 6 == 0) mc.player.swingHand(Hand.MAIN_HAND); }));
        emotes.add(new Simple("point", "Zeigen", "Zeigt mit der Nebenhand.", 30, (mc, t) -> { if (t % 10 == 0) mc.player.swingHand(Hand.OFF_HAND); }));
        emotes.add(new Simple("cheer", "Jubeln", "Springt und schwingt beide Hände.", 40, (mc, t) -> {
            if (t % 8 == 0) mc.player.swingHand(t % 16 == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND);
            if (t % 20 == 0 && mc.player.isOnGround()) mc.player.jump();
        }));
        emotes.add(new Simple("spin", "Drehung", "Eine volle Drehung.", 24, (mc, t) -> mc.player.setYaw(mc.player.getYaw() + 15f)));
        emotes.add(new Simple("bow", "Verbeugen", "Kurze Verbeugung (Blick nach unten).", 24, (mc, t) -> mc.player.setPitch(t < 12 ? Math.min(60f, mc.player.getPitch() + 5f) : Math.max(0f, mc.player.getPitch() - 5f))));
    }

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (active == null || mc.player == null) return;
            try {
                active.tick(mc, tick);
            } catch (Exception e) {
                ChaosClient.LOGGER.warn("[ChaosClient] Emote {}: {}", active.id(), e.toString());
                active = null;
                return;
            }
            tick++;
            if (tick >= active.duration()) {
                active.onEnd(mc);
                active = null;
            }
        });
    }

    public List<Emote> all() { return emotes; }
    public Emote active() { return active; }
    public void add(Emote e) { emotes.add(e); }

    public Emote byId(String id) {
        for (Emote e : emotes) if (e.id().equalsIgnoreCase(id) || e.name().equalsIgnoreCase(id)) return e;
        return null;
    }

    public void play(Emote e) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || e == null) return;
        active = e;
        tick = 0;
        startYaw = mc.player.getYaw();
        ChaosClient.get().getNotifications().info("Emote: " + e.name());
    }

    public void stop() { active = null; }

    /** Einfaches Emote aus Lambda. */
    public static final class Simple implements Emote {
        public interface Ticker { void tick(MinecraftClient mc, int t); }
        private final String id, name, description;
        private final int duration;
        private final Ticker ticker;
        public Simple(String id, String name, String description, int duration, Ticker ticker) {
            this.id = id; this.name = name; this.description = description; this.duration = duration; this.ticker = ticker;
        }
        @Override public String id() { return id; }
        @Override public String name() { return name; }
        @Override public String description() { return description; }
        @Override public int duration() { return duration; }
        @Override public void tick(MinecraftClient mc, int t) { ticker.tick(mc, t); }
    }
}
