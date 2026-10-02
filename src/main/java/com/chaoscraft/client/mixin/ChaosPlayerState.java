package com.chaoscraft.client.mixin;

import java.util.UUID;

/** Duck-Interface: hängt die Spieler-UUID an den PlayerEntityRenderState (für Hüte/Cosmetics). */
public interface ChaosPlayerState {
    UUID chaos$uuid();
    void chaos$setUuid(UUID uuid);
}
