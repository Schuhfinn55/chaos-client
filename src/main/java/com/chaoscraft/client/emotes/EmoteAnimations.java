package com.chaoscraft.client.emotes;

import java.util.List;
import java.util.function.Function;

/**
 * Die Chaos-Emotes: prozedurale Ganzkörper-Animationen (Kopf, Körper, Arme, Beine,
 * Sprünge, Drehungen). Jede Animation liefert zur Zeit t (Sekunden) eine {@link EmotePose};
 * Ein-/Ausblenden übernimmt der {@link EmoteManager}.
 */
public final class EmoteAnimations {
    private EmoteAnimations() {}

    /** Animiertes Emote. */
    public record Animated(String id, String name, String description, String icon, float seconds, boolean loop, Function<Float, EmotePose> pose) implements EmoteManager.Emote {
        @Override public int duration() { return Math.round(seconds * 20); }
        @Override public void tick(net.minecraft.client.MinecraftClient mc, int tick) {}
        public EmotePose poseAt(float t) { return pose.apply(t); }
    }

    private static final float TAU = (float) (Math.PI * 2);

    private static float sin(float x) { return (float) Math.sin(x); }
    private static float cos(float x) { return (float) Math.cos(x); }
    /** Weiche Hin-und-Her-Kurve 0..1 mit Frequenz f (Hz). */
    private static float osc(float t, float f) { return 0.5f + 0.5f * sin(t * TAU * f); }
    private static float ease(float x) { x = Math.max(0, Math.min(1, x)); return x * x * (3 - 2 * x); }

    public static final List<Animated> ALL = List.of(
        new Animated("wave", "Winken", "Arm hoch und kräftig winken – mit Blickkontakt.", "👋", 2.6f, false, t -> {
            float w = sin(t * TAU * 2.2f);
            return new EmotePose().rArm(-150 + w * 6, 0, 35 + w * 28).lArm(-8, 0, -4).head(-8, -18, 0).body(0, -6, 0);
        }),
        new Animated("dab", "Dab", "Der Klassiker. Kopf in die Armbeuge, Arm nach oben.", "😎", 1.3f, false, t -> {
            float b = 1 + 0.03f * sin(t * TAU * 3);
            return new EmotePose().head(28 * b, -42, -10).body(6, -18, 0).rArm(-105, -62, -35).lArm(-150, 20, -62).rLeg(-6, 0, 0).lLeg(6, 0, 0).offset(1.5f * sin(t * TAU * 3), 0);
        }),
        new Animated("dance", "Chaos Dance", "Hüften, Arme, Kopf – alles im Takt. Läuft, bis du dich bewegst.", "🕺", 6f, true, t -> {
            float a = sin(t * TAU * 1.9f), b = sin(t * TAU * 0.95f), c = cos(t * TAU * 1.9f);
            return new EmotePose()
                .head(6 * c, 22 * b, 8 * a)
                .body(4 + 3 * c, 16 * b, 4 * a)
                .rArm(-95 - 45 * a, -12 * b, 28 + 18 * c)
                .lArm(-95 + 45 * a, 12 * b, -28 + 18 * c)
                .rLeg(-14 * Math.max(0, a), 10 * b, 4)
                .lLeg(14 * Math.max(0, -a), 10 * b, -4)
                .offset(-2.5f * Math.abs(a), 0);
        }),
        new Animated("floss", "Floss", "Arme schwingen, Hüfte gegenläufig – der Floss.", "🌊", 4f, true, t -> {
            float s = sin(t * TAU * 2.4f);
            return new EmotePose()
                .body(0, 14 * s, 6 * s)
                .rArm(-10 + 18 * s, 0, 42 * s)
                .lArm(-10 - 18 * s, 0, 42 * s)
                .head(4, -10 * s, -5 * s)
                .rLeg(0, 0, 3 * s).lLeg(0, 0, 3 * s);
        }),
        new Animated("flex", "Flex", "Beide Bizeps zeigen, Kopf stolz von links nach rechts.", "💪", 2.6f, false, t -> {
            float look = sin(t * TAU * 0.55f);
            float pump = 1 + 0.05f * sin(t * TAU * 2.5f);
            return new EmotePose().rArm(-125 * pump, -35, 78).lArm(-125 * pump, 35, -78).head(-10, 38 * look, 0).body(-4, 10 * look, 0).offset(-0.5f, 0);
        }),
        new Animated("tpose", "T-Pose", "Arme waagerecht. Dominanz.", "✝", 2f, false, t -> new EmotePose().arms(0, 0, 90).head(0, 0, 0).body(0, 0, 0)),
        new Animated("backflip", "Backflip", "Ein kompletter Rückwärtssalto.", "🔄", 0.95f, false, t -> {
            float p = Math.max(0, Math.min(1, t / 0.95f));
            float rot = -360 * ease(p);
            float jump = -14 * sin((float) (p * Math.PI));
            return new EmotePose().group(rot, 0).offset(jump, 0).arms(-150, 0, 20).legs(20, 0, 0).head(-10, 0, 0);
        }),
        new Animated("spin", "Drehung", "Pirouette mit erhobenen Armen.", "🌀", 1.2f, false, t -> {
            float p = Math.max(0, Math.min(1, t / 1.2f));
            return new EmotePose().group(0, 360 * ease(p)).arms(-170, 0, 12).legs(0, 0, 4 * sin(p * TAU * 2)).offset(-3 * sin((float) (p * Math.PI)), 0);
        }),
        new Animated("sit", "Sitzen", "Hinsetzen, Arme auf den Knien – bis du aufstehst.", "🪑", 6f, true, t -> {
            float breathe = sin(t * TAU * 0.4f);
            return new EmotePose().legs(-90, 0, 4).rArm(-30 + 2 * breathe, 0, 8).lArm(-30 + 2 * breathe, 0, -8).body(-6, 0, 0).head(8 - 2 * breathe, 12 * sin(t * TAU * 0.2f), 0).offset(10, -2);
        }),
        new Animated("bow", "Verbeugung", "Tiefe Verbeugung mit Hand auf der Brust.", "🙇", 2.4f, false, t -> {
            float p = t < 0.6f ? ease(t / 0.6f) : t < 1.7f ? 1 : 1 - ease((t - 1.7f) / 0.7f);
            return new EmotePose().body(32 * p, 0, 0).head(30 * p, 0, 0).rArm(-60 * p, -40 * p, -20 * p).lArm(10 * p, 0, -15 * p).offset(2 * p, -4 * p);
        }),
        new Animated("clap", "Applaus", "Schnelles, lautes Klatschen.", "👏", 2.6f, false, t -> {
            float c = Math.abs(sin(t * TAU * 3.2f));
            return new EmotePose().rArm(-72, -30 + 26 * c, 20 - 32 * c).lArm(-72, 30 - 26 * c, -20 + 32 * c).head(-6, 0, 0).body(0, 0, 0).offset(-0.6f * c, 0);
        }),
        new Animated("cheer", "Jubel", "Arme hoch, hüpfen, feiern.", "🎉", 3f, true, t -> {
            float j = Math.abs(sin(t * TAU * 1.5f));
            float a = sin(t * TAU * 3f);
            return new EmotePose().rArm(-165 + 10 * a, -10, 22 + 10 * a).lArm(-165 - 10 * a, 10, -22 + 10 * a).head(-18, 0, 6 * a).body(-3, 0, 0).legs(-6 * j, 0, 0).offset(-6 * j, 0);
        }),
        new Animated("salute", "Salut", "Zackiger Gruß an die Stirn.", "🫡", 1.8f, false, t -> {
            float p = t < 0.25f ? ease(t / 0.25f) : t < 1.4f ? 1 : 1 - ease((t - 1.4f) / 0.4f);
            return new EmotePose().rArm(-140 * p, -55 * p, 10 * p).lArm(0, 0, 2).head(-4 * p, 0, 0).body(-2 * p, 0, 0);
        }),
        new Animated("facepalm", "Facepalm", "Hand ins Gesicht, Kopf runter.", "🤦", 2.2f, false, t -> {
            float p = t < 0.3f ? ease(t / 0.3f) : t < 1.7f ? 1 : 1 - ease((t - 1.7f) / 0.5f);
            float shake = sin(t * TAU * 1.5f) * 6 * p;
            return new EmotePose().rArm(-150 * p, 22 * p, -14 * p).head(28 * p, shake, 0).body(6 * p, 0, 0).lArm(0, 0, 3);
        }),
        new Animated("headbang", "Headbang", "Kopf schütteln, Arme oben – Metal.", "🤘", 4f, true, t -> {
            float h = sin(t * TAU * 2.6f);
            float a = sin(t * TAU * 1.3f);
            return new EmotePose().head(28 + 30 * h, 0, 0).rArm(-150, -20, 30 + 8 * a).lArm(-150, 20, -30 - 8 * a).body(6 + 4 * h, 0, 0).offset(-1.5f * Math.abs(h), 0).legs(0, 0, 0);
        }),
        new Animated("think", "Nachdenken", "Hand ans Kinn, Blick in die Ferne.", "🤔", 2.4f, false, t -> {
            float p = t < 0.3f ? ease(t / 0.3f) : t < 2.0f ? 1 : 1 - ease((t - 2.0f) / 0.4f);
            return new EmotePose().rArm(-120 * p, 12 * p, -28 * p).head(-6 * p, 24 * p * sin(t * TAU * 0.4f), 10 * p).body(0, 8 * p, 0);
        })
    );
}
