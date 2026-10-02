package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.world.BlockOverlayModule;
import com.chaoscraft.client.render.BoxRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Block Overlay: farbiger Rahmen um den anvisierten Block (zusätzlich zur Vanilla-Linie). */
@Mixin(WorldRenderer.class)
public class OutlineMixin {
    @Inject(method = "drawBlockOutline", at = @At("HEAD"))
    private void chaosclient$blockOverlay(MatrixStack matrices, VertexConsumer vertexConsumer, double camX, double camY, double camZ,
                                          OutlineRenderState state, int color, float lineWidth, CallbackInfo ci) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        BlockOverlayModule overlay = cc.getModuleManager().get(BlockOverlayModule.class);
        if (overlay == null || !overlay.isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = hit.getBlockPos();
        Box box = new Box(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ,
                pos.getX() + 1 - camX, pos.getY() + 1 - camY, pos.getZ() + 1 - camZ).expand(0.002);
        BoxRenderer.drawBox(matrices, vertexConsumer, box, overlay.getRed(), overlay.getGreen(), overlay.getBlue(), overlay.getAlpha());
    }
}
