package com.chaoscraft.client.mixin;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Zugriff auf die interpolierten Wetterwerte (Weather Changer). */
@Mixin(World.class)
public interface WorldAccessor {
    @Accessor("lastRainGradient") void chaos$setLastRainGradient(float v);
    @Accessor("lastThunderGradient") void chaos$setLastThunderGradient(float v);
}
