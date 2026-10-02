package com.onyx.visuals.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

/**
 * World-space rendering helper. Uses POSITION_COLOR format for Tessellator
 * and writes Normal+LineWidth when using the LINES render layer VertexConsumer.
 * Yarn mappings 1.21.11.
 */
public final class WorldRenderer {

    private WorldRenderer() {}

    private static VertexFormat posColorFormat;

    private static VertexFormat getFormat() {
        if (posColorFormat == null) {
            posColorFormat = VertexFormat.builder()
                    .add("Position", VertexFormatElement.POSITION)
                    .add("Color", VertexFormatElement.COLOR)
                    .build();
        }
        return posColorFormat;
    }

    /** Draws a box outline using an existing VertexConsumer (from a render pass).
     *  The LINES render layer requires Position, Color, Normal, LineWidth per vertex. */
    public static void drawBox(MatrixStack stack, VertexConsumer vc, Box box, int r, int g, int b, int a) {
        Matrix4f m = stack.peek().getPositionMatrix();
        double x1 = box.minX, y1 = box.minY, z1 = box.minZ;
        double x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;

        line(vc, m, x1, y1, z1, x2, y1, z1, r, g, b, a);
        line(vc, m, x2, y1, z1, x2, y1, z2, r, g, b, a);
        line(vc, m, x2, y1, z2, x1, y1, z2, r, g, b, a);
        line(vc, m, x1, y1, z2, x1, y1, z1, r, g, b, a);
        line(vc, m, x1, y2, z1, x2, y2, z1, r, g, b, a);
        line(vc, m, x2, y2, z1, x2, y2, z2, r, g, b, a);
        line(vc, m, x2, y2, z2, x1, y2, z2, r, g, b, a);
        line(vc, m, x1, y2, z2, x1, y2, z1, r, g, b, a);
        line(vc, m, x1, y1, z1, x1, y2, z1, r, g, b, a);
        line(vc, m, x2, y1, z1, x2, y2, z1, r, g, b, a);
        line(vc, m, x2, y1, z2, x2, y2, z2, r, g, b, a);
        line(vc, m, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    /** Draws a box outline using Tessellator (standalone, for non-render-pass contexts). */
    public static void drawBox(MatrixStack stack, Box box, int r, int g, int b, int a) {
        Matrix4f m = stack.peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, getFormat());

        double x1 = box.minX, y1 = box.minY, z1 = box.minZ;
        double x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;

        line(buf, m, x1, y1, z1, x2, y1, z1, r, g, b, a);
        line(buf, m, x2, y1, z1, x2, y1, z2, r, g, b, a);
        line(buf, m, x2, y1, z2, x1, y1, z2, r, g, b, a);
        line(buf, m, x1, y1, z2, x1, y1, z1, r, g, b, a);
        line(buf, m, x1, y2, z1, x2, y2, z1, r, g, b, a);
        line(buf, m, x2, y2, z1, x2, y2, z2, r, g, b, a);
        line(buf, m, x2, y2, z2, x1, y2, z2, r, g, b, a);
        line(buf, m, x1, y2, z2, x1, y2, z1, r, g, b, a);
        line(buf, m, x1, y1, z1, x1, y2, z1, r, g, b, a);
        line(buf, m, x2, y1, z1, x2, y2, z1, r, g, b, a);
        line(buf, m, x2, y1, z2, x2, y2, z2, r, g, b, a);
        line(buf, m, x1, y1, z2, x1, y2, z2, r, g, b, a);

        BuiltBuffer built = buf.end();
        if (built != null) built.close();
    }

    /** Writes a line to a VertexConsumer with all required elements for LINES render layer:
     *  Position, Color, Normal (0,1,0), LineWidth (1.0). */
    private static void line(VertexConsumer vc, Matrix4f m, double x1, double y1, double z1,
                             double x2, double y2, double z2, int r, int g, int b, int a) {
        vc.vertex(m, (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(0, 1, 0).lineWidth(1.0f);
        vc.vertex(m, (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(0, 1, 0).lineWidth(1.0f);
    }

    /** Writes a line to a BufferBuilder (Tessellator) with Position+Color only. */
    private static void line(BufferBuilder buf, Matrix4f m, double x1, double y1, double z1,
                             double x2, double y2, double z2, int r, int g, int b, int a) {
        buf.vertex(m, (float) x1, (float) y1, (float) z1).color(r, g, b, a);
        buf.vertex(m, (float) x2, (float) y2, (float) z2).color(r, g, b, a);
    }
}
