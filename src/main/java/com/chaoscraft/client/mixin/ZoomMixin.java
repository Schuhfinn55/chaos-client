package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.general.ZoomModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Zoom: FOV durch den (animierten) Zoomfaktor teilen. */
@Mixin(GameRenderer.class)
public class ZoomMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void chaosclient$zoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        ZoomModule zoom = cc.getModuleManager().get(ZoomModule.class);
        if (zoom == null || !zoom.isEnabled()) return;
        float f = zoom.currentFactor();
        if (f > 1.001f) cir.setReturnValue(cir.getReturnValue() / f);
    }
}
