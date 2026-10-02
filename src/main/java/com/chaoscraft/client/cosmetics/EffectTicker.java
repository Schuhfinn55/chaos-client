package com.chaoscraft.client.cosmetics;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.cosmetics.CosmeticsModule;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.Random;

/**
 * Spawnt die Partikel der Chaos-Effekte um Spieler mit aktivem Effekt
 * (eigener Account + bekannte Chaos-Spieler). Rein clientseitig und
 * kosmetisch – keine Pakete, kein Einfluss auf Gameplay.
 */
public final class EffectTicker {
    private EffectTicker() {}

    private static final Random RNG = new Random();
    private static int tick;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(EffectTicker::onTick);
    }

    private static void onTick(MinecraftClient mc) {
        tick++;
        if (mc.world == null || mc.player == null || mc.isPaused()) return;
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        CosmeticsModule mod = cc.getModuleManager().get(CosmeticsModule.class);
        if (mod != null && !mod.categoryToggle("PARTICLES").isEnabled()) return;
        CosmeticsManager cm = CosmeticsManager.get();
        for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
            if (p.isInvisible() || p.isSpectator() || p.squaredDistanceTo(mc.player) > 48 * 48) continue;
            String id = cm.effectFor(p.getUuid());
            if (id == null) continue;
            EffectCatalog.Effect e = EffectCatalog.byId(id);
            if (e == null) continue;
            try {
                spawn(mc, p, e);
            } catch (Exception ex) {
                ChaosClient.LOGGER.warn("[ChaosCosmetics] Effekt {}: {}", id, ex.toString());
            }
        }
    }

    private static void spawn(MinecraftClient mc, AbstractClientPlayerEntity p, EffectCatalog.Effect e) {
        double x = p.getX(), y = p.getY(), z = p.getZ();
        double h = p.getHeight();
        switch (e.id()) {
            case "chaos-aura" -> {
                if (tick % 2 != 0) return;
                double a = (tick * 0.18) % (Math.PI * 2);
                for (int i = 0; i < 2; i++) {
                    double ang = a + i * Math.PI;
                    add(mc, new DustParticleEffect(e.color() & 0xFFFFFF, 1.1f), x + Math.cos(ang) * 0.9, y + 0.15 + ((tick % 40) / 40.0) * h, z + Math.sin(ang) * 0.9, 0, 0.01, 0);
                }
            }
            case "portal" -> {
                if (tick % 2 != 0) return;
                double a = RNG.nextDouble() * Math.PI * 2;
                add(mc, ParticleTypes.PORTAL, x + Math.cos(a) * 0.8, y + RNG.nextDouble() * h, z + Math.sin(a) * 0.8, -Math.cos(a) * 0.4, -0.2, -Math.sin(a) * 0.4);
            }
            case "flame-feet" -> {
                boolean moving = p.getVelocity().horizontalLengthSquared() > 0.003;
                if (tick % (moving ? 1 : 4) != 0) return;
                add(mc, ParticleTypes.FLAME, x + off(0.3), y + 0.05, z + off(0.3), 0, 0.02, 0);
                if (moving && tick % 3 == 0) add(mc, ParticleTypes.SMOKE, x + off(0.3), y + 0.1, z + off(0.3), 0, 0.03, 0);
            }
            case "soul-fire" -> { if (tick % 3 == 0) add(mc, ParticleTypes.SOUL_FIRE_FLAME, x + off(0.5), y + RNG.nextDouble() * h * 0.7, z + off(0.5), 0, 0.04, 0); }
            case "enchant-orbit" -> {
                if (tick % 2 != 0) return;
                double a = (tick * 0.25) % (Math.PI * 2);
                add(mc, ParticleTypes.ENCHANT, x + Math.cos(a) * 0.7, y + h + 0.2, z + Math.sin(a) * 0.7, -Math.cos(a) * 0.3, -0.4, -Math.sin(a) * 0.3);
            }
            case "hearts" -> { if (tick % 12 == 0) add(mc, ParticleTypes.HEART, x + off(0.5), y + h * 0.6 + RNG.nextDouble() * 0.6, z + off(0.5), 0, 0.05, 0); }
            case "notes" -> { if (tick % 8 == 0) add(mc, ParticleTypes.NOTE, x + off(0.6), y + h + 0.1, z + off(0.6), RNG.nextDouble(), 0, 0); }
            case "cherry" -> { if (tick % 3 == 0) add(mc, ParticleTypes.CHERRY_LEAVES, x + off(1.0), y + h + 0.8, z + off(1.0), 0, -0.02, 0); }
            case "snow" -> { if (tick % 2 == 0) add(mc, ParticleTypes.SNOWFLAKE, x + off(1.2), y + h + 1.0, z + off(1.2), 0, -0.03, 0); }
            case "end-rod" -> {
                if (tick % 2 != 0) return;
                double a = (tick * 0.3) % (Math.PI * 2);
                double yy = y + ((tick % 30) / 30.0) * h;
                add(mc, ParticleTypes.END_ROD, x + Math.cos(a) * 0.6, yy, z + Math.sin(a) * 0.6, 0, 0.005, 0);
            }
            case "sparks" -> { if (tick % 2 == 0) add(mc, ParticleTypes.ELECTRIC_SPARK, x + off(0.4), y + RNG.nextDouble() * h, z + off(0.4), off(0.08), off(0.08), off(0.08)); }
            case "glow" -> {
                if (tick % 3 != 0) return;
                double a = RNG.nextDouble() * Math.PI * 2;
                add(mc, ParticleTypes.GLOW, x + Math.cos(a) * 0.8, y + 0.3 + RNG.nextDouble() * h, z + Math.sin(a) * 0.8, off(0.01), 0.01, off(0.01));
            }
            case "smoke" -> { if (tick % 2 == 0) add(mc, ParticleTypes.SMOKE, x + off(0.5), y + RNG.nextDouble() * h * 0.8, z + off(0.5), 0, 0.03, 0); }
            case "totem" -> { if (tick % 2 == 0) add(mc, ParticleTypes.TOTEM_OF_UNDYING, x, y + h * 0.5, z, off(0.25), RNG.nextDouble() * 0.3, off(0.25)); }
            default -> {}
        }
    }

    private static double off(double r) { return (RNG.nextDouble() - 0.5) * 2 * r; }

    private static void add(MinecraftClient mc, ParticleEffect type, double x, double y, double z, double vx, double vy, double vz) {
        if (mc.world != null) mc.world.addParticleClient(type, x, y, z, vx, vy, vz);
    }
}
