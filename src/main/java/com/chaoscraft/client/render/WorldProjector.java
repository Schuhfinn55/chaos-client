package com.chaoscraft.client.render;

import com.chaoscraft.client.mixin.GameRendererAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Projiziert Weltkoordinaten auf den Bildschirm (skalierte GUI-Pixel). Die View-Matrix
 * wird pro Frame beim Welt-Rendering (nach den Entities) gesichert; HUD-Overlays
 * (Lebensbalken, TNT-Timer, Lichtlevel) zeichnen damit 2D-Text an 3D-Positionen.
 */
public final class WorldProjector {
    private WorldProjector() {}

    private static final Matrix4f VIEW_PROJ = new Matrix4f();
    private static Vec3d cam = Vec3d.ZERO;
    private static boolean valid;

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            try {
                MinecraftClient mc = MinecraftClient.getInstance();
                Camera camera = mc.gameRenderer.getCamera();
                float delta = mc.getRenderTickCounter().getTickProgress(true);
                float fov = ((GameRendererAccessor) mc.gameRenderer).chaos$getFov(camera, delta, true);
                Matrix4f proj = mc.gameRenderer.getBasicProjectionMatrix(fov);
                VIEW_PROJ.set(proj).mul(ctx.matrices().peek().getPositionMatrix());
                cam = camera.getCameraPos();
                valid = true;
            } catch (Exception e) {
                valid = false;
            }
        });
    }

    public static boolean isValid() { return valid; }
    public static Vec3d cameraPos() { return cam; }

    /** Bildschirmposition {x, y, tiefe} oder null, wenn der Punkt hinter der Kamera liegt. */
    public static float[] project(double x, double y, double z) {
        if (!valid) return null;
        Vector4f v = new Vector4f((float) (x - cam.x), (float) (y - cam.y), (float) (z - cam.z), 1f).mul(VIEW_PROJ);
        if (v.w <= 0.01f) return null;
        float nx = v.x / v.w, ny = v.y / v.w;
        MinecraftClient mc = MinecraftClient.getInstance();
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        return new float[]{(nx + 1f) / 2f * sw, (1f - ny) / 2f * sh, v.w};
    }
}
