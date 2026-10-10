package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.player.LowFireModule;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Low Fire: das Feuer-Overlay in der Ego-Perspektive nach unten verschieben. */
@Mixin(InGameOverlayRenderer.class)
public class FireOverlayMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"))
    private static void chaosclient$lowFireStart(MatrixStack matrices, VertexConsumerProvider consumers, Sprite sprite, CallbackInfo ci) {
        matrices.push();
        ChaosClient cc = ChaosClient.get();
        LowFireModule mod = cc == null ? null : cc.getModuleManager().get(LowFireModule.class);
        if (mod != null && mod.isEnabled()) matrices.translate(0f, -mod.offset(), 0f);
    }

    @Inject(method = "renderFireOverlay", at = @At("RETURN"))
    private static void chaosclient$lowFireEnd(MatrixStack matrices, VertexConsumerProvider consumers, Sprite sprite, CallbackInfo ci) {
        matrices.pop();
    }
}
