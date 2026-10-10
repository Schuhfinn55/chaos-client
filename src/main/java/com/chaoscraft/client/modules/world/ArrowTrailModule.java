package com.chaoscraft.client.modules.world;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.EnumSetting;
import com.chaoscraft.client.settings.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

/** Arrow Trail: Partikelspur hinter fliegenden Pfeilen und Dreizacken. */
public class ArrowTrailModule extends Module {

    public enum Trail { END_ROD, FLAME, SOUL_FIRE, CRIT, SPARK, HERZEN }

    private final EnumSetting<Trail> trail = add(new EnumSetting<>("Partikel", "Art der Spur.", Trail.END_ROD));
    private final IntSetting density = add(new IntSetting("Dichte", "Partikel pro Tick und Pfeil.", 2, 1, 6));
    private final BooleanSetting ownOnly = add(new BooleanSetting("Nur eigene", "Nur Pfeile, die du geschossen hast.", false));

    public ArrowTrailModule() {
        super("Arrow Trail", "Leuchtende Partikelspur hinter Pfeilen.", Category.WORLD, "➶");
        tags("pfeil", "arrow", "trail", "partikel", "bogen");
    }

    private ParticleEffect effect() {
        return switch (trail.get()) {
            case FLAME -> ParticleTypes.FLAME;
            case SOUL_FIRE -> ParticleTypes.SOUL_FIRE_FLAME;
            case CRIT -> ParticleTypes.CRIT;
            case SPARK -> ParticleTypes.ELECTRIC_SPARK;
            case HERZEN -> ParticleTypes.HEART;
            default -> ParticleTypes.END_ROD;
        };
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;
        ParticleEffect fx = effect();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PersistentProjectileEntity p) || p.isOnGround() || p.getVelocity().lengthSquared() < 0.01) continue;
            if (ownOnly.isEnabled() && p.getOwner() != mc.player) continue;
            if (p.squaredDistanceTo(mc.player) > 64 * 64) continue;
            var v = p.getVelocity();
            for (int i = 0; i < density.getInt(); i++) {
                double f = i / (double) density.getInt();
                mc.world.addParticleClient(fx, p.getX() - v.x * f, p.getY() - v.y * f, p.getZ() - v.z * f, 0, 0, 0);
            }
        }
    }
}
