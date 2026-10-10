package com.chaoscraft.client.emotes;

import java.util.List;
import java.util.function.Function;

/**
 * Die Chaos-Emotes: prozedurale Ganzkörper-Animationen (Kopf, Körper, Arme, Beine,
 * Sprünge, Drehungen). Jede Animation liefert zur Zeit t (Sekunden) eine {@link EmotePose};
 * Ein-/Ausblenden übernimmt der {@link EmoteManager}. Legendäre Emotes (exclusive) gibt es
 * nur per Code – Freischaltung „emote:&lt;id&gt;“ im Cosmetics-Profil.
 *
 * Dieselben Animationen sind im Launcher (src/lib/builtinEmotes.ts) für die Vorschau gespiegelt.
 */
public final class EmoteAnimations {
    private EmoteAnimations() {}

    /** Animiertes Emote. */
    public record Animated(String id, String name, String description, String icon, float seconds, boolean loop, boolean exclusive, Function<Float, EmotePose> pose) implements EmoteManager.Emote {
        @Override public int duration() { return Math.round(seconds * 20); }
        @Override public void tick(net.minecraft.client.MinecraftClient mc, int tick) {}
        public EmotePose poseAt(float t) { return pose.apply(t); }
    }

    private static Animated free(String id, String name, String desc, String icon, float seconds, boolean loop, Function<Float, EmotePose> pose) {
        return new Animated(id, name, desc, icon, seconds, loop, false, pose);
    }
    private static Animated legendary(String id, String name, String desc, String icon, float seconds, boolean loop, Function<Float, EmotePose> pose) {
        return new Animated(id, name, desc, icon, seconds, loop, true, pose);
    }

    private static final float TAU = (float) (Math.PI * 2);

    private static float sin(float x) { return (float) Math.sin(x); }
    private static float cos(float x) { return (float) Math.cos(x); }
    private static float abs(float x) { return Math.abs(x); }
    /** Weiche Hin-und-Her-Kurve 0..1 mit Frequenz f (Hz). */
    private static float osc(float t, float f) { return 0.5f + 0.5f * sin(t * TAU * f); }
    private static float ease(float x) { x = Math.max(0, Math.min(1, x)); return x * x * (3 - 2 * x); }
    /** Hüllkurve: 0→1 in `in` Sekunden, 1 bis `hold`, dann →0 in `out` Sekunden. */
    private static float env(float t, float in, float hold, float out) {
        return t < in ? ease(t / in) : t < hold ? 1 : 1 - ease((t - hold) / out);
    }

    public static final List<Animated> ALL = List.of(
        /* ======================= Klassiker ======================= */
        free("wave", "Winken", "Arm hoch und kräftig winken – mit Blickkontakt.", "👋", 2.6f, false, t -> {
            float w = sin(t * TAU * 2.2f);
            return new EmotePose().rArm(-150 + w * 6, 0, 35 + w * 28).lArm(-8, 0, -4).head(-8, -18, 0).body(0, -6, 0);
        }),
        free("dab", "Dab", "Der Klassiker. Kopf in die Armbeuge, Arm nach oben.", "😎", 1.3f, false, t -> {
            float b = 1 + 0.03f * sin(t * TAU * 3);
            return new EmotePose().head(28 * b, -42, -10).body(6, -18, 0).rArm(-105, -62, -35).lArm(-150, 20, -62).rLeg(-6, 0, 0).lLeg(6, 0, 0).offset(1.5f * sin(t * TAU * 3), 0);
        }),
        free("dance", "Chaos Dance", "Hüften, Arme, Kopf – alles im Takt. Läuft, bis du dich bewegst.", "🕺", 6f, true, t -> {
            float a = sin(t * TAU * 1.9f), b = sin(t * TAU * 0.95f), c = cos(t * TAU * 1.9f);
            return new EmotePose()
                .head(6 * c, 22 * b, 8 * a)
                .body(4 + 3 * c, 16 * b, 4 * a)
                .rArm(-95 - 45 * a, -12 * b, 28 + 18 * c)
                .lArm(-95 + 45 * a, 12 * b, -28 + 18 * c)
                .rLeg(-14 * Math.max(0, a), 10 * b, 4)
                .lLeg(14 * Math.max(0, -a), 10 * b, -4)
                .offset(-2.5f * abs(a), 0);
        }),
        free("floss", "Floss", "Arme schwingen, Hüfte gegenläufig – der Floss.", "🌊", 4f, true, t -> {
            float s = sin(t * TAU * 2.4f);
            return new EmotePose()
                .body(0, 14 * s, 6 * s)
                .rArm(-10 + 18 * s, 0, 42 * s)
                .lArm(-10 - 18 * s, 0, 42 * s)
                .head(4, -10 * s, -5 * s)
                .rLeg(0, 0, 3 * s).lLeg(0, 0, 3 * s);
        }),
        free("flex", "Flex", "Beide Bizeps zeigen, Kopf stolz von links nach rechts.", "💪", 2.6f, false, t -> {
            float look = sin(t * TAU * 0.55f);
            float pump = 1 + 0.05f * sin(t * TAU * 2.5f);
            return new EmotePose().rArm(-125 * pump, -35, 78).lArm(-125 * pump, 35, -78).head(-10, 38 * look, 0).body(-4, 10 * look, 0).offset(-0.5f, 0);
        }),
        free("tpose", "T-Pose", "Arme waagerecht. Dominanz.", "✝", 2f, false, t -> new EmotePose().arms(0, 0, 90).head(0, 0, 0).body(0, 0, 0)),
        free("backflip", "Backflip", "Ein kompletter Rückwärtssalto.", "🔄", 0.95f, false, t -> {
            float p = Math.max(0, Math.min(1, t / 0.95f));
            float rot = -360 * ease(p);
            float jump = -14 * sin((float) (p * Math.PI));
            return new EmotePose().group(rot, 0).offset(jump, 0).arms(-150, 0, 20).legs(20, 0, 0).head(-10, 0, 0);
        }),
        free("spin", "Drehung", "Pirouette mit erhobenen Armen.", "🌀", 1.2f, false, t -> {
            float p = Math.max(0, Math.min(1, t / 1.2f));
            return new EmotePose().group(0, 360 * ease(p)).arms(-170, 0, 12).legs(0, 0, 4 * sin(p * TAU * 2)).offset(-3 * sin((float) (p * Math.PI)), 0);
        }),
        free("sit", "Sitzen", "Hinsetzen, Arme auf den Knien – bis du aufstehst.", "🪑", 6f, true, t -> {
            float breathe = sin(t * TAU * 0.4f);
            return new EmotePose().legs(-90, 0, 4).rArm(-30 + 2 * breathe, 0, 8).lArm(-30 + 2 * breathe, 0, -8).body(-6, 0, 0).head(8 - 2 * breathe, 12 * sin(t * TAU * 0.2f), 0).offset(10, -2);
        }),
        free("bow", "Verbeugung", "Tiefe Verbeugung mit Hand auf der Brust.", "🙇", 2.4f, false, t -> {
            float p = env(t, 0.6f, 1.7f, 0.7f);
            return new EmotePose().body(32 * p, 0, 0).head(30 * p, 0, 0).rArm(-60 * p, -40 * p, -20 * p).lArm(10 * p, 0, -15 * p).offset(2 * p, -4 * p);
        }),
        free("clap", "Applaus", "Schnelles, lautes Klatschen.", "👏", 2.6f, false, t -> {
            float c = abs(sin(t * TAU * 3.2f));
            return new EmotePose().rArm(-72, -30 + 26 * c, 20 - 32 * c).lArm(-72, 30 - 26 * c, -20 + 32 * c).head(-6, 0, 0).body(0, 0, 0).offset(-0.6f * c, 0);
        }),
        free("cheer", "Jubel", "Arme hoch, hüpfen, feiern.", "🎉", 3f, true, t -> {
            float j = abs(sin(t * TAU * 1.5f));
            float a = sin(t * TAU * 3f);
            return new EmotePose().rArm(-165 + 10 * a, -10, 22 + 10 * a).lArm(-165 - 10 * a, 10, -22 + 10 * a).head(-18, 0, 6 * a).body(-3, 0, 0).legs(-6 * j, 0, 0).offset(-6 * j, 0);
        }),
        free("salute", "Salut", "Zackiger Gruß an die Stirn.", "🫡", 1.8f, false, t -> {
            float p = env(t, 0.25f, 1.4f, 0.4f);
            return new EmotePose().rArm(-150 * p, 0, -42 * p).lArm(0, 0, 2).head(-6 * p, 0, 0).body(-2 * p, 0, 0);
        }),
        free("facepalm", "Facepalm", "Hand ins Gesicht, Kopf runter.", "🤦", 2.2f, false, t -> {
            float p = env(t, 0.3f, 1.7f, 0.5f);
            float shake = sin(t * TAU * 1.5f) * 6 * p;
            return new EmotePose().rArm(-152 * p, 0, -34 * p).head(26 * p, shake, 0).body(6 * p, 0, 0).lArm(0, 0, 3);
        }),
        free("headbang", "Headbang", "Kopf schütteln, Arme oben – Metal.", "🤘", 4f, true, t -> {
            float h = sin(t * TAU * 2.6f);
            float a = sin(t * TAU * 1.3f);
            return new EmotePose().head(28 + 30 * h, 0, 0).rArm(-150, -20, 30 + 8 * a).lArm(-150, 20, -30 - 8 * a).body(6 + 4 * h, 0, 0).offset(-1.5f * abs(h), 0).legs(0, 0, 0);
        }),
        free("think", "Nachdenken", "Hand ans Kinn, Blick in die Ferne.", "🤔", 2.4f, false, t -> {
            float p = env(t, 0.3f, 2.0f, 0.4f);
            return new EmotePose().rArm(-122 * p, 0, -32 * p).head(-6 * p, 24 * p * sin(t * TAU * 0.4f), 10 * p).body(0, 8 * p, 0);
        }),

        /* ======================= Neu (frei) ======================= */
        free("griddy", "Griddy", "Beine kicken abwechselnd, Arme kreuzen vorm Körper – der Griddy.", "🏈", 4f, true, t -> {
            float s = sin(t * TAU * 2.2f), c = cos(t * TAU * 2.2f);
            return new EmotePose()
                .rLeg(-38 * Math.max(0, s), 0, 6).lLeg(-38 * Math.max(0, -s), 0, -6)
                .rArm(-75 + 12 * c, -35, 18).lArm(-75 - 12 * c, 35, -18)
                .body(4, 9 * s, 0).head(-4, -7 * s, 3 * c)
                .offset(-2f * abs(s), 0);
        }),
        free("orange", "Orange Justice", "Arme kreisen wild, Körper wippt – purer Spaß.", "🍊", 3.2f, true, t -> {
            float a = sin(t * TAU * 1.25f), b = cos(t * TAU * 1.25f), s = sin(t * TAU * 2.5f);
            return new EmotePose()
                .rArm(-90 + 85 * a, 0, 55 + 25 * b).lArm(-90 - 85 * a, 0, -55 - 25 * b)
                .body(6 + 4 * s, 0, 11 * b).head(-6, 0, -9 * b)
                .rLeg(-10 * Math.max(0, s), 0, 5).lLeg(-10 * Math.max(0, -s), 0, -5)
                .offset(-3 * abs(s), 0);
        }),
        free("jumpjack", "Hampelmann", "Hoch, Arme und Beine spreizen, runter – und wieder.", "🤸", 2.4f, true, t -> {
            float j = osc(t, 1.5f), s = sin(t * TAU * 1.5f);
            return new EmotePose()
                .rArm(0, 0, 12 + 158 * j).lArm(0, 0, -12 - 158 * j)
                .rLeg(0, 0, 5 + 22 * j).lLeg(0, 0, -5 - 22 * j)
                .head(-4 * j, 0, 0).body(0, 0, 0)
                .offset(-9 * abs(s), 0);
        }),
        free("pushup", "Liegestütze", "Runter auf den Boden und pumpen.", "🏋", 4f, true, t -> {
            float p = osc(t, 1f);
            return new EmotePose().group(86, 0).offset(8.5f + 2.5f * p, 0)
                .arms(-100 + 30 * p, 0, 8).legs(0, 0, 3).head(-38, 0, 0).body(0, 0, 0);
        }),
        free("sleep", "Schlafen", "Hinlegen, Arme an den Körper, tief durchatmen.", "💤", 8f, true, t -> {
            float b = sin(t * TAU * 0.25f);
            return new EmotePose().group(-88, 0).offset(8.5f, 0)
                .arms(12, 0, 6).legs(4, 0, 2).head(-8 + 2 * b, 14, 0).body(0, 0, 0);
        }),
        free("rage", "Rage", "Fäuste in die Luft, Kopf schütteln, stampfen.", "😡", 3f, false, t -> {
            float f = sin(t * TAU * 9f), s = sin(t * TAU * 3f);
            return new EmotePose()
                .rArm(-160 + 6 * f, -12, 22 + 5 * f).lArm(-160 - 6 * f, 12, -22 - 5 * f)
                .head(-12 + 4 * f, 14 * s, 0).body(-4 + 2 * f, 0, 0)
                .rLeg(-24 * Math.max(0, s), 0, 2).lLeg(-24 * Math.max(0, -s), 0, -2)
                .offset(-1.5f * abs(f), 0);
        }),
        free("laugh", "Lachen", "Nach vorn gebeugt, Hände am Bauch, schüttelt sich.", "😂", 3f, false, t -> {
            float p = env(t, 0.3f, 2.4f, 0.6f), s = sin(t * TAU * 6f);
            return new EmotePose()
                .body(18 * p + 4 * s * p, 0, 0).head((8 + 6 * s) * p, 0, 5 * s * p)
                .rArm(-55 * p, -45 * p, -12 * p).lArm(-55 * p, 45 * p, 12 * p)
                .offset(1.5f * p, 0);
        }),
        free("shrug", "Schulterzucken", "Hände hoch, Kopf schief – keine Ahnung.", "🤷", 2.2f, false, t -> {
            float p = env(t, 0.3f, 1.6f, 0.5f);
            return new EmotePose().rArm(-28 * p, 0, 62 * p).lArm(-28 * p, 0, -62 * p).head(-4 * p, 10 * p, 16 * p).body(0, 0, 4 * p).offset(-0.8f * p, 0);
        }),
        free("point", "Zeigen", "Da! Arm ausgestreckt nach vorn.", "👉", 2f, false, t -> {
            float p = env(t, 0.2f, 1.5f, 0.4f);
            return new EmotePose().rArm(-92 * p, -8 * p, 4 * p).lArm(0, 0, 2).head(-3 * p, -6 * p, 0).body(-2 * p, -8 * p, 0);
        }),
        free("cry", "Weinen", "Hände vors Gesicht, Schultern zucken.", "😭", 3.2f, false, t -> {
            float p = env(t, 0.4f, 2.5f, 0.6f), s = sin(t * TAU * 4f);
            return new EmotePose().rArm(-150 * p, 0, -36 * p).lArm(-150 * p, 0, 36 * p).head(24 * p, 0, 0).body((9 + 2 * s) * p, 0, 0).offset(0.8f * p, 0);
        }),

        /* ======================= Legendär (nur mit Code) ======================= */
        legendary("breakdance", "Breakdance", "Auf dem Rücken, Beine kreisen, der ganze Körper rotiert – Windmill.", "🌪", 4f, true, t -> {
            float w = t * TAU * 1.5f;
            return new EmotePose().group(-84, 360f * (t / 4f))
                .offset(8.5f + 1.2f * sin(w), 0)
                .rLeg(-30 + 25 * sin(w), 0, 70 * cos(w)).lLeg(-30 - 25 * sin(w), 0, 70 * cos(w))
                .rArm(-150, -20, 30).lArm(-150, 20, -30).head(-30, 0, 0).body(0, 0, 0);
        }),
        legendary("chaos-rave", "Chaos Rave", "Volle Energie: Arme fliegen, Körper dreht, nichts steht still.", "🔥", 4f, true, t -> {
            float a = sin(t * TAU * 3f), b = cos(t * TAU * 3f), s = sin(t * TAU * 1.5f);
            return new EmotePose().group(0, 360f * (t / 4f))
                .rArm(-150 + 35 * a, -15, 30 + 30 * b).lArm(-150 - 35 * a, 15, -30 + 30 * b)
                .body(5, 22 * s, 6 * a).head(-10 + 6 * a, -12 * s, 8 * b)
                .rLeg(-28 * Math.max(0, a), 0, 6).lLeg(-28 * Math.max(0, -a), 0, -6)
                .offset(-4 * abs(a), 0);
        }),
        legendary("helicopter", "Helikopter", "Arme waagerecht, immer schneller drehen – und abheben.", "🚁", 3f, true, t -> {
            float p = t / 3f;
            float lift = -4 - 6 * osc(t, 0.33f);
            return new EmotePose().group(0, 720f * p).arms(0, 0, 90).legs(0, 0, 3).head(-8, 0, 0).body(0, 0, 0).offset(lift, 0);
        }),
        legendary("levitate", "Levitation", "Im Schneidersitz schwebend, langsam um die eigene Achse.", "🧘", 6f, true, t -> {
            float f = sin(t * TAU * 0.5f);
            return new EmotePose().group(0, 360f * (t / 6f))
                .rLeg(-88, 32, -14).lLeg(-88, -32, 14)
                .rArm(-45, -28, 22).lArm(-45, 28, -22)
                .head(10 + 3 * f, 0, 0).body(0, 0, 0)
                .offset(-12 + 2.5f * f, -1);
        }),
        legendary("moonwalk", "Moonwalk", "Gleitende Schritte rückwärts, lässig wie Michael.", "🕴", 2.4f, true, t -> {
            float s = sin(t * TAU * 1.6f), c = cos(t * TAU * 1.6f);
            return new EmotePose()
                .rLeg(28 * s, 0, 2).lLeg(-28 * s, 0, -2)
                .rArm(10 * c, 0, 6).lArm(-10 * c, 0, -6)
                .body(-7, 0, 0).head(6, -8, 0)
                .offset(-0.8f * abs(c), 0);
        }),
        legendary("dragon-roar", "Drachenbrüllen", "Ducken, aufrichten, Arme spreizen und brüllen – die Erde bebt.", "🐉", 3.4f, false, t -> {
            if (t < 1.0f) {
                float p = ease(t / 1.0f);
                return new EmotePose().body(22 * p, 0, 0).head(10 * p, 0, 0).arms(-20 * p, 0, 12).legs(-18 * p, 0, 4).offset(4 * p, 0);
            }
            float r = Math.min(1, (t - 1.0f) / 0.25f), q = ease(r), tail = t > 2.9f ? 1 - ease((t - 2.9f) / 0.5f) : 1;
            float sh = sin(t * TAU * 11f) * 2.5f * tail;
            return new EmotePose()
                .body((-10 + sh) * q * tail, 0, 0).head((-30 + sh) * q * tail, 0, 0)
                .rArm((-60 - 20 * q) * tail, 0, (20 + 65 * q) * tail).lArm((-60 - 20 * q) * tail, 0, (-20 - 65 * q) * tail)
                .legs(-6 * tail, 0, 8 * tail).offset((-3 * q + sh * 0.2f) * tail, 0);
        })
    );

    public static Animated byId(String id) {
        for (Animated a : ALL) if (a.id().equals(id)) return a;
        return null;
    }
}
