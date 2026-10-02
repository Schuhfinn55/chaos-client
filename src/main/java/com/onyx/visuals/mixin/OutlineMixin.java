package com.onyx.visuals.mixin;

import com.onyx.visuals.OnyxVisuals;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.module.impl.BlockOverlayModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BlockOverlay implementation: hooks WorldRenderer.drawBlockOutline.
 * Signature in 1.21.11: (MatrixStack, VertexConsumer, double, double, double,
 *                        OutlineRenderState, int, float) = method_22712.
 * We draw our own colored outline using the provided MatrixStack + VertexConsumer.
 * The block position comes from mc.crosshairTarget.
 * Yarn mappings 1.21.11.
 */
@Mixin(WorldRenderer.class)
public class OutlineMixin {

    @Inject(method = "drawBlockOutline", at = @At("HEAD"))
    private void onyxvisuals$blockOverlay(MatrixStack matrices, VertexConsumer vertexConsumer,
                                           double camX, double camY, double camZ,
                                           net.minecraft.client.render.state.OutlineRenderState state,
                                           int color, float lineWidth,
                                           CallbackInfo ci) {
        OnyxVisuals ov = OnyxVisuals.getInstance();
        if (ov == null || ov.getModuleManager() == null) return;
        Module mod = ov.getModuleManager().getByName("BlockOverlay");
        if (mod == null || !mod.isEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
        BlockOverlayModule overlay = (BlockOverlayModule) mod;

        // camera-space box for the looked-at block
        Box box = new Box(
                pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ,
                pos.getX() + 1 - camX, pos.getY() + 1 - camY, pos.getZ() + 1 - camZ
        ).expand(0.002, 0.002, 0.002);

        com.onyx.visuals.render.WorldRenderer.drawBox(matrices, vertexConsumer, box,
                overlay.getRed(), overlay.getGreen(), overlay.getBlue(), overlay.getAlpha());
    }
}
