package com.chaoscraft.client.modules.pvp;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.render.BoxRenderer;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.ColorSetting;
import com.chaoscraft.client.settings.IntSetting;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Hitboxes: Trefferboxen von Spielern, Mobs und Items farbig anzeigen (ohne F3+B). */
public class HitBoxModule extends Module {

    private final BooleanSetting players = add(new BooleanSetting("Spieler", "Hitboxen von Spielern.", true));
    private final BooleanSetting mobs = add(new BooleanSetting("Mobs", "Hitboxen von Mobs/Tieren.", true));
    private final BooleanSetting items = add(new BooleanSetting("Items", "Hitboxen von Items am Boden.", false));
    private final BooleanSetting self = add(new BooleanSetting("Eigene", "Eigene Hitbox in der dritten Person.", false));
    private final ColorSetting playerColor = add(new ColorSetting("Farbe Spieler", "Farbe der Spieler-Hitboxen.", 0xFFE11D2E));
    private final ColorSetting mobColor = add(new ColorSetting("Farbe Mobs", "Farbe der Mob-Hitboxen.", 0xFF4ADE80));
    private final ColorSetting itemColor = add(new ColorSetting("Farbe Items", "Farbe der Item-Hitboxen.", 0xFFFBBF24));
    private final IntSetting range = add(new IntSetting("Reichweite", "Nur Entities bis zu dieser Entfernung (Blöcke).", 32, 8, 128));

    public HitBoxModule() {
        super("Hitboxes", "Trefferboxen von Spielern, Mobs und Items anzeigen.", Category.PVP, "▢");
        tags("hitbox", "box", "trefferbox", "f3+b");
    }

    public static void registerRender() {
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            ChaosClient cc = ChaosClient.get();
            if (cc == null) return;
            HitBoxModule mod = cc.getModuleManager().get(HitBoxModule.class);
            if (mod == null || !mod.isEnabled() || ctx.consumers() == null) return;
            try { mod.render(ctx.matrices(), ctx.consumers().getBuffer(RenderLayers.lines())); } catch (Exception ignored) {}
        });
    }

    private void render(net.minecraft.client.util.math.MatrixStack matrices, VertexConsumer vc) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        Vec3d cam = mc.gameRenderer.getCamera().getCameraPos();
        float delta = mc.getRenderTickCounter().getTickProgress(true);
        double r2 = (double) range.getInt() * range.getInt();
        for (Entity e : mc.world.getEntities()) {
            if (e.squaredDistanceTo(mc.player) > r2) continue;
            ColorSetting col;
            if (e == mc.player) { if (!self.isEnabled() || mc.options.getPerspective().isFirstPerson()) continue; col = playerColor; }
            else if (e instanceof PlayerEntity) { if (!players.isEnabled()) continue; col = playerColor; }
            else if (e instanceof LivingEntity) { if (!mobs.isEnabled()) continue; col = mobColor; }
            else if (e instanceof ItemEntity) { if (!items.isEnabled()) continue; col = itemColor; }
            else continue;
            Vec3d p = e.getLerpedPos(delta);
            Box b = e.getBoundingBox().offset(p.x - e.getX() - cam.x, p.y - e.getY() - cam.y, p.z - e.getZ() - cam.z);
            BoxRenderer.drawBox(matrices, vc, b, col.red(), col.green(), col.blue(), col.alpha());
        }
    }
}
