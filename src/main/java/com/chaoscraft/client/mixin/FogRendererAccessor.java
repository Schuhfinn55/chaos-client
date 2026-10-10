package com.chaoscraft.client.mixin;

import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Schaltet den Nebel komplett ab (No Fog) – derselbe Schalter wie der Debug-Toggle. */
@Mixin(FogRenderer.class)
public interface FogRendererAccessor {
    @Accessor("fogEnabled")
    static void chaos$setFogEnabled(boolean enabled) { throw new AssertionError(); }
    @Accessor("fogEnabled")
    static boolean chaos$isFogEnabled() { throw new AssertionError(); }
}
