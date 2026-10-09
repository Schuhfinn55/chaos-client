package com.chaoscraft.client.cosmetics;

import com.chaoscraft.client.emotes.EmotePose;

import java.util.UUID;

/** Duck-Interface: hängt UUID, Bewegungszustand und Emote-Pose an den PlayerEntityRenderState (für Hüte/Wings/Emotes). */
public interface ChaosPlayerState {
    UUID chaos$uuid();
    void chaos$setUuid(UUID uuid);
    /** true, wenn der Spieler nicht am Boden ist (Sprung/Fall). */
    boolean chaos$airborne();
    /** Vertikale Geschwindigkeit (Blöcke/Tick, negativ = fällt). */
    float chaos$velY();
    void chaos$setMotion(boolean airborne, float velY);
    /** Emote-Pose dieses Frames (einmal pro Frame im Renderer gesetzt, damit Modell und Cosmetics dieselbe Pose nutzen) oder null. */
    EmotePose chaos$emotePose();
    void chaos$setEmotePose(EmotePose pose);
}
