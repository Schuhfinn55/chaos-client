package com.chaoscraft.client.emotes;

/**
 * Pose eines Spielermodells für einen Animationsmoment. Winkel in Grad,
 * Minecraft-Konvention: Pitch positiv = nach vorn/unten kippen, Arme: Pitch −90 = nach vorn,
 * −180 = senkrecht nach oben; Roll: rechter Arm positiv = nach außen, linker Arm negativ = nach außen.
 * Teile: 0 Kopf, 1 Körper, 2 rechter Arm, 3 linker Arm, 4 rechtes Bein, 5 linkes Bein.
 */
public final class EmotePose {
    public static final int HEAD = 0, BODY = 1, R_ARM = 2, L_ARM = 3, R_LEG = 4, L_LEG = 5;

    public final float[] pitch = new float[6];
    public final float[] yaw = new float[6];
    public final float[] roll = new float[6];
    public final boolean[] set = new boolean[6];
    /** Verschiebung des gesamten Modells (Modell-Einheiten; y positiv = nach unten). */
    public float offsetY, offsetZ;
    /** Drehung des gesamten Modells um die Körpermitte (Grad). */
    public float groupPitch, groupYaw;

    public EmotePose part(int i, float p, float y, float r) {
        pitch[i] = p; yaw[i] = y; roll[i] = r; set[i] = true;
        return this;
    }

    public EmotePose head(float p, float y, float r) { return part(HEAD, p, y, r); }
    public EmotePose body(float p, float y, float r) { return part(BODY, p, y, r); }
    public EmotePose rArm(float p, float y, float r) { return part(R_ARM, p, y, r); }
    public EmotePose lArm(float p, float y, float r) { return part(L_ARM, p, y, r); }
    public EmotePose rLeg(float p, float y, float r) { return part(R_LEG, p, y, r); }
    public EmotePose lLeg(float p, float y, float r) { return part(L_LEG, p, y, r); }
    public EmotePose arms(float p, float y, float r) { rArm(p, y, r); return lArm(p, -y, -r); }
    public EmotePose legs(float p, float y, float r) { rLeg(p, y, r); return lLeg(p, -y, -r); }
    public EmotePose offset(float y, float z) { offsetY = y; offsetZ = z; return this; }
    public EmotePose group(float pitchDeg, float yawDeg) { groupPitch = pitchDeg; groupYaw = yawDeg; return this; }

    /** Lineare Mischung zweier Posen (für Ein-/Ausblenden). */
    public static EmotePose mix(EmotePose a, EmotePose b, float t) {
        EmotePose o = new EmotePose();
        for (int i = 0; i < 6; i++) {
            if (a.set[i] || b.set[i]) {
                float ap = a.set[i] ? a.pitch[i] : 0, bp = b.set[i] ? b.pitch[i] : 0;
                float ay = a.set[i] ? a.yaw[i] : 0, by = b.set[i] ? b.yaw[i] : 0;
                float ar = a.set[i] ? a.roll[i] : 0, br = b.set[i] ? b.roll[i] : 0;
                o.part(i, ap + (bp - ap) * t, ay + (by - ay) * t, ar + (br - ar) * t);
            }
        }
        o.offsetY = a.offsetY + (b.offsetY - a.offsetY) * t;
        o.offsetZ = a.offsetZ + (b.offsetZ - a.offsetZ) * t;
        o.groupPitch = a.groupPitch + (b.groupPitch - a.groupPitch) * t;
        o.groupYaw = a.groupYaw + (b.groupYaw - a.groupYaw) * t;
        return o;
    }
}
