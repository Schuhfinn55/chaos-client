package com.chaoscraft.client.cosmetics;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.cosmetics.CosmeticsModule;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;

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
        boolean particlesOn = mod == null || mod.categoryToggle("PARTICLES").isEnabled();
        CosmeticsManager cm = CosmeticsManager.get();
        for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
            if (p.isInvisible() || p.isSpectator() || p.squaredDistanceTo(mc.player) > 48 * 48) continue;
            boolean own = cm.isOwner(p.getUuid());
            // Wings-Partikel (Phönix-Funken, Feen-Glitzer …)
            String wid = cm.wingsFor(p.getUuid());
            if (wid != null && (mod == null || mod.categoryToggle("WINGS").isEnabled()) && !(mod != null && ((own && !mod.showOwn().isEnabled()) || (!own && !mod.showOthers().isEnabled())))) {
                WingsCatalog.Wings w = WingsCatalog.byId(wid);
                if (w != null && !w.particle().isEmpty() && tick % 3 == 0) {
                    try { spawnWingParticle(mc, p, w.particle()); } catch (Exception ignored) {}
                }
            }
            if (!particlesOn) continue;
            if (mod != null && ((own && !mod.showOwn().isEnabled()) || (!own && !mod.showOthers().isEnabled()))) continue;
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
            // ---- Premium
            case "chaos-storm" -> {
                double a = (tick * 0.22) % (Math.PI * 2);
                for (int i = 0; i < 2; i++) {
                    double ang = a + i * Math.PI;
                    double yy = y + ((tick * 1.7 + i * 20) % 44) / 44.0 * (h + 0.4);
                    double r = 0.75 + 0.25 * Math.sin(yy * 3);
                    add(mc, new DustParticleEffect(i == 0 ? 0xE11D2E : 0x111113, 1.3f), x + Math.cos(ang) * r, yy, z + Math.sin(ang) * r, 0, 0.02, 0);
                }
                if (tick % 9 == 0) add(mc, ParticleTypes.SMOKE, x + off(0.4), y + 0.1, z + off(0.4), 0, 0.05, 0);
            }
            case "lightning" -> {
                if (tick % 2 == 0) add(mc, ParticleTypes.ELECTRIC_SPARK, x + off(0.6), y + RNG.nextDouble() * h, z + off(0.6), off(0.1), off(0.1), off(0.1));
                if (tick % 24 == 0) {
                    double a = RNG.nextDouble() * Math.PI * 2, r = 0.9 + RNG.nextDouble() * 0.6;
                    for (int i = 0; i < 10; i++) add(mc, ParticleTypes.END_ROD, x + Math.cos(a) * r, y + i * 0.25, z + Math.sin(a) * r, 0, 0, 0);
                }
            }
            case "void-rift" -> {
                double a = (tick * 0.35) % (Math.PI * 2);
                add(mc, ParticleTypes.REVERSE_PORTAL, x + Math.cos(a) * 1.1, y + 0.05, z + Math.sin(a) * 1.1, -Math.cos(a) * 0.1, 0.25, -Math.sin(a) * 0.1);
                if (tick % 2 == 0) add(mc, new DustParticleEffect(0x4C1D95, 1.4f), x + Math.cos(a + Math.PI) * 1.0, y + 0.03, z + Math.sin(a + Math.PI) * 1.0, 0, 0.01, 0);
                if (tick % 6 == 0) add(mc, ParticleTypes.WITCH, x + off(0.5), y + RNG.nextDouble() * h, z + off(0.5), 0, 0.08, 0);
            }
            case "galaxy" -> {
                if (tick % 2 != 0) return;
                double a = (tick * 0.2) % (Math.PI * 2);
                double yy = y + ((tick % 50) / 50.0) * (h + 0.3);
                add(mc, ParticleTypes.END_ROD, x + Math.cos(a) * 0.8, yy, z + Math.sin(a) * 0.8, 0, 0.004, 0);
                add(mc, new DustParticleEffect(0x818CF8, 1.0f), x + Math.cos(a + Math.PI) * 0.8, y + h + 0.2 - (yy - y), z + Math.sin(a + Math.PI) * 0.8, 0, 0.004, 0);
                if (tick % 8 == 0) add(mc, ParticleTypes.ENCHANT, x + off(0.3), y + h + 0.6, z + off(0.3), off(0.4), -0.3, off(0.4));
            }
            case "blood-moon" -> {
                if (tick % 2 == 0) add(mc, new DustParticleEffect(0x8A0F1C, 1.6f), x + off(0.9), y + 0.05 + RNG.nextDouble() * 0.3, z + off(0.9), 0, 0.045, 0);
                if (tick % 5 == 0) add(mc, ParticleTypes.FLAME, x + off(0.6), y + RNG.nextDouble() * h * 0.6, z + off(0.6), 0, 0.03, 0);
                if (tick % 13 == 0) add(mc, ParticleTypes.LAVA, x + off(0.5), y + 0.1, z + off(0.5), 0, 0, 0);
            }
            case "wisps" -> {
                if (tick % 3 != 0) return;
                double a = (tick * 0.12) % (Math.PI * 2);
                for (int i = 0; i < 3; i++) {
                    double ang = a + i * (Math.PI * 2 / 3);
                    add(mc, ParticleTypes.SOUL, x + Math.cos(ang) * 0.75, y + h * 0.75 + Math.sin(ang * 2) * 0.2, z + Math.sin(ang) * 0.75, -Math.sin(ang) * 0.02, 0.005, Math.cos(ang) * 0.02);
                }
            }
            case "angel-ring" -> {
                double a = (tick * 0.3) % (Math.PI * 2);
                add(mc, ParticleTypes.END_ROD, x + Math.cos(a) * 0.4, y + h + 0.35, z + Math.sin(a) * 0.4, 0, 0, 0);
                add(mc, ParticleTypes.END_ROD, x + Math.cos(a + Math.PI) * 0.4, y + h + 0.35, z + Math.sin(a + Math.PI) * 0.4, 0, 0, 0);
                if (tick % 7 == 0) add(mc, ParticleTypes.GLOW, x + off(0.3), y + h + 0.4, z + off(0.3), 0, 0.01, 0);
            }
            case "firework-trail" -> {
                boolean moving = p.getVelocity().horizontalLengthSquared() > 0.003;
                if (!moving) { if (tick % 10 == 0) add(mc, ParticleTypes.TOTEM_OF_UNDYING, x + off(0.3), y + 0.3, z + off(0.3), off(0.05), 0.1, off(0.05)); return; }
                add(mc, ParticleTypes.FIREWORK, x + off(0.3), y + 0.2 + RNG.nextDouble() * 0.8, z + off(0.3), off(0.03), 0.02, off(0.03));
                if (tick % 2 == 0) add(mc, ParticleTypes.TOTEM_OF_UNDYING, x + off(0.3), y + 0.3, z + off(0.3), off(0.05), 0.05, off(0.05));
            }
            case "rainbow" -> {
                double a = (tick * 0.2) % (Math.PI * 2);
                for (int i = 0; i < 2; i++) {
                    double ang = a + i * Math.PI;
                    float hue = ((tick * 3 + i * 90) % 360) / 360f;
                    int rgb = MathHelper.hsvToRgb(hue, 0.9f, 1f) & 0xFFFFFF;
                    double yy = y + ((tick * 1.5 + i * 25) % 50) / 50.0 * h;
                    add(mc, new DustParticleEffect(rgb, 1.2f), x + Math.cos(ang) * 0.8, yy, z + Math.sin(ang) * 0.8, 0, 0.01, 0);
                }
            }
            case "frost-aura" -> {
                double a = (tick * 0.15) % (Math.PI * 2);
                if (tick % 2 == 0) add(mc, ParticleTypes.SNOWFLAKE, x + Math.cos(a) * 0.9, y + 0.3 + RNG.nextDouble() * h * 0.8, z + Math.sin(a) * 0.9, -Math.sin(a) * 0.03, -0.01, Math.cos(a) * 0.03);
                if (tick % 3 == 0) add(mc, new DustParticleEffect(0xBAE6FD, 0.9f), x + Math.cos(a + Math.PI) * 0.9, y + 0.3 + RNG.nextDouble() * h * 0.8, z + Math.sin(a + Math.PI) * 0.9, 0, 0.01, 0);
            }
            default -> {}
        }
    }

    private static double off(double r) { return (RNG.nextDouble() - 0.5) * 2 * r; }

    /** Partikel hinter dem Spieler auf Flügelhöhe, links und rechts. */
    private static void spawnWingParticle(MinecraftClient mc, AbstractClientPlayerEntity p, String kind) {
        double yaw = Math.toRadians(p.getBodyYaw());
        double bx = -Math.sin(yaw), bz = Math.cos(yaw); // Blickrichtung (xz)
        double sideX = Math.cos(yaw), sideZ = Math.sin(yaw);
        for (int s = -1; s <= 1; s += 2) {
            double spread = 0.55 + RNG.nextDouble() * 0.45;
            double x = p.getX() - bx * 0.35 + sideX * s * spread, z = p.getZ() - bz * 0.35 + sideZ * s * spread;
            double y = p.getY() + 1.0 + RNG.nextDouble() * 0.6;
            ParticleEffect pe = switch (kind) {
                case "flame" -> ParticleTypes.FLAME;
                case "end_rod" -> ParticleTypes.END_ROD;
                case "snowflake" -> ParticleTypes.SNOWFLAKE;
                case "smoke" -> ParticleTypes.SMOKE;
                case "dust_red" -> new DustParticleEffect(0xE11D2E, 0.8f);
                case "portal" -> ParticleTypes.REVERSE_PORTAL;
                case "soul" -> ParticleTypes.SOUL_FIRE_FLAME;
                case "glow" -> ParticleTypes.GLOW;
                case "dust_gold" -> new DustParticleEffect(0xF5C342, 0.8f);
                case "dust_cyan" -> new DustParticleEffect(0x00E5FF, 0.8f);
                // Legendär: Mischung aus mehreren Partikeln
                case "overlord" -> switch (RNG.nextInt(4)) { case 0 -> ParticleTypes.FLAME; case 1 -> ParticleTypes.ELECTRIC_SPARK; case 2 -> ParticleTypes.LAVA; default -> new DustParticleEffect(0xFF1F3D, 1.2f); };
                case "celestial" -> switch (RNG.nextInt(4)) { case 0 -> ParticleTypes.END_ROD; case 1 -> ParticleTypes.GLOW; case 2 -> ParticleTypes.TOTEM_OF_UNDYING; default -> new DustParticleEffect(MathHelper.hsvToRgb((System.currentTimeMillis() % 4000) / 4000f, 0.5f, 1f) & 0xFFFFFF, 1.0f); };
                default -> null;
            };
            if (pe != null) add(mc, pe, x, y, z, -bx * 0.02, 0.01, -bz * 0.02);
        }
    }

    private static void add(MinecraftClient mc, ParticleEffect type, double x, double y, double z, double vx, double vy, double vz) {
        if (mc.world != null) mc.world.addParticleClient(type, x, y, z, vx, vy, vz);
    }
}
