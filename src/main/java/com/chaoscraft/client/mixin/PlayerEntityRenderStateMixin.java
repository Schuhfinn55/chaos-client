package com.chaoscraft.client.mixin;

import com.chaoscraft.client.cosmetics.ChaosPlayerState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(PlayerEntityRenderState.class)
public class PlayerEntityRenderStateMixin implements ChaosPlayerState {
    @Unique private UUID chaos$uuid;
    @Unique private boolean chaos$airborne;
    @Unique private float chaos$velY;

    @Override public UUID chaos$uuid() { return chaos$uuid; }
    @Override public void chaos$setUuid(UUID uuid) { this.chaos$uuid = uuid; }
    @Override public boolean chaos$airborne() { return chaos$airborne; }
    @Override public float chaos$velY() { return chaos$velY; }
    @Override public void chaos$setMotion(boolean airborne, float velY) { this.chaos$airborne = airborne; this.chaos$velY = velY; }
}
