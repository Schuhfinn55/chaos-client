package com.onyx.visuals.mixin;

import com.onyx.visuals.OnyxVisuals;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.module.impl.ZoomModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Zoom implementation: divides the rendered FOV while the zoom key is held.
 * Target: GameRenderer.getFov(Camera, float, boolean) = method_3196.
 * Yarn mappings 1.21.11.
 */
@Mixin(GameRenderer.class)
public class ZoomMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onyxvisuals$zoom(Camera camera, float tickDelta, boolean changingFov,
                                   CallbackInfoReturnable<Float> cir) {
        OnyxVisuals ov = OnyxVisuals.getInstance();
        if (ov == null || ov.getModuleManager() == null) return;
        Module mod = ov.getModuleManager().getByName("Zoom");
        if (mod == null || !mod.isEnabled()) return;

        ZoomModule zoom = (ZoomModule) mod;
        if (zoom.isZoomKeyHeld()) {
            float fov = cir.getReturnValue();
            cir.setReturnValue(fov / zoom.getFactor());
        }
    }
}
