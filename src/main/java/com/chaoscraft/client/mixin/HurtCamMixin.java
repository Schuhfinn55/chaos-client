package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.player.NoHurtCamModule;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** No Hurt Cam: das Kamerawackeln bei Schaden unterdrücken. */
@Mixin(GameRenderer.class)
public class HurtCamMixin {
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void chaosclient$noHurtCam(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        NoHurtCamModule mod = cc.getModuleManager().get(NoHurtCamModule.class);
        if (mod != null && mod.isEnabled()) ci.cancel();
    }
}
