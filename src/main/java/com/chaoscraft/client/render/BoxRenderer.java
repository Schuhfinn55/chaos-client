package com.chaoscraft.client.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

/** Zeichnet Box-Kanten in einen LINES-VertexConsumer (Position, Color, Normal, LineWidth). */
public final class BoxRenderer {
    private BoxRenderer() {}

    public static void drawBox(MatrixStack stack, VertexConsumer vc, Box box, int r, int g, int b, int a) {
        Matrix4f m = stack.peek().getPositionMatrix();
        double x1 = box.minX, y1 = box.minY, z1 = box.minZ, x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;
        line(vc, m, x1, y1, z1, x2, y1, z1, r, g, b, a); line(vc, m, x2, y1, z1, x2, y1, z2, r, g, b, a);
        line(vc, m, x2, y1, z2, x1, y1, z2, r, g, b, a); line(vc, m, x1, y1, z2, x1, y1, z1, r, g, b, a);
        line(vc, m, x1, y2, z1, x2, y2, z1, r, g, b, a); line(vc, m, x2, y2, z1, x2, y2, z2, r, g, b, a);
        line(vc, m, x2, y2, z2, x1, y2, z2, r, g, b, a); line(vc, m, x1, y2, z2, x1, y2, z1, r, g, b, a);
        line(vc, m, x1, y1, z1, x1, y2, z1, r, g, b, a); line(vc, m, x2, y1, z1, x2, y2, z1, r, g, b, a);
        line(vc, m, x2, y1, z2, x2, y2, z2, r, g, b, a); line(vc, m, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private static void line(VertexConsumer vc, Matrix4f m, double x1, double y1, double z1, double x2, double y2, double z2, int r, int g, int b, int a) {
        vc.vertex(m, (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(0, 1, 0).lineWidth(1.0f);
        vc.vertex(m, (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(0, 1, 0).lineWidth(1.0f);
    }
}
