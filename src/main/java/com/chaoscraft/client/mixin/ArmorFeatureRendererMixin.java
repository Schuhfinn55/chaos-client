package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.player.HideArmorModule;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hide Armor: Rüstung von Spielern nicht rendern (eigene/andere). */
@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void chaosclient$hideArmor(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, BipedEntityRenderState state, float limbAngle, float limbDistance, CallbackInfo ci) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        HideArmorModule mod = cc.getModuleManager().get(HideArmorModule.class);
        if (mod != null && mod.isEnabled() && mod.shouldHide(state)) ci.cancel();
    }
}
