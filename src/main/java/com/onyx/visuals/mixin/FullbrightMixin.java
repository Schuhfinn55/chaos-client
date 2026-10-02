package com.onyx.visuals.mixin;

import com.onyx.visuals.OnyxVisuals;
import com.onyx.visuals.module.Module;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fullbright implementation: forces maximum brightness in the lightmap.
 * Target: LightmapTextureManager.getBrightness(DimensionType, int) = method_23284.
 * The method is static in 1.21.11, so the callback must be static too.
 * Yarn mappings 1.21.11.
 */
@Mixin(LightmapTextureManager.class)
public class FullbrightMixin {

    @Inject(method = "getBrightness(Lnet/minecraft/world/dimension/DimensionType;I)F",
            at = @At("RETURN"), cancellable = true)
    private static void onyxvisuals$fullbright(DimensionType dimensionType, int light,
                                                CallbackInfoReturnable<Float> cir) {
        OnyxVisuals ov = OnyxVisuals.getInstance();
        if (ov == null || ov.getModuleManager() == null) return;
        Module mod = ov.getModuleManager().getByName("Fullbright");
        if (mod == null || !mod.isEnabled()) return;

        cir.setReturnValue(1.0f);
    }
}
