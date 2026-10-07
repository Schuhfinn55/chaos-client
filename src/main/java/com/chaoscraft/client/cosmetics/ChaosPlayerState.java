package com.chaoscraft.client.cosmetics;

import java.util.UUID;

/** Duck-Interface: hängt UUID und Bewegungszustand an den PlayerEntityRenderState (für Hüte/Wings/Cosmetics). */
public interface ChaosPlayerState {
    UUID chaos$uuid();
    void chaos$setUuid(UUID uuid);
    /** true, wenn der Spieler nicht am Boden ist (Sprung/Fall). */
    boolean chaos$airborne();
    /** Vertikale Geschwindigkeit (Blöcke/Tick, negativ = fällt). */
    float chaos$velY();
    void chaos$setMotion(boolean airborne, float velY);
}
