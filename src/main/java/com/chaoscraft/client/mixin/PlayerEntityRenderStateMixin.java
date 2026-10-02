package com.chaoscraft.client.mixin;

import com.chaoscraft.client.cosmetics.ChaosPlayerState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(PlayerEntityRenderState.class)
public class PlayerEntityRenderStateMixin implements ChaosPlayerState {
    @Unique private UUID chaos$uuid;

    @Override public UUID chaos$uuid() { return chaos$uuid; }
    @Override public void chaos$setUuid(UUID uuid) { this.chaos$uuid = uuid; }
}
