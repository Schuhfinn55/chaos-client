package com.chaoscraft.client.mixin;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import com.chaoscraft.client.cosmetics.ChaosPlayerState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Merkt sich die UUID des gerenderten Spielers im Render-State (für den Hut-Renderer). */
@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("TAIL"))
    private void chaosclient$storeUuid(PlayerLikeEntity entity, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (state instanceof ChaosPlayerState cps) cps.chaos$setUuid(entity.getUuid());
        // Chaos-Badge vor dem Namen über dem Kopf
        try { state.displayName = com.chaoscraft.client.cosmetics.BadgeManager.decorate(entity.getUuid(), state.displayName); } catch (Exception ignored) {}
    }
}
