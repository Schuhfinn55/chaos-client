package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.general.FullbrightModule;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fullbright (LIGHTMAP-Modus): Lightmap-Helligkeit auf Maximum. */
@Mixin(LightmapTextureManager.class)
public class FullbrightMixin {
    @Inject(method = "getBrightness(Lnet/minecraft/world/dimension/DimensionType;I)F", at = @At("RETURN"), cancellable = true)
    private static void chaosclient$fullbright(DimensionType dimensionType, int light, CallbackInfoReturnable<Float> cir) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        FullbrightModule fb = cc.getModuleManager().get(FullbrightModule.class);
        if (fb != null && fb.lightmapMode()) cir.setReturnValue(1.0f);
    }
}
