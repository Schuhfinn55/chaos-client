package com.chaoscraft.client.modules.world;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudOverlay;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.render.WorldProjector;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.IntSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

/** Light Level: Lichtwert auf Blöcken rund um dich – wo Monster spawnen können. */
public class LightLevelModule extends Module implements HudOverlay {

    private final IntSetting radius = add(new IntSetting("Radius", "Blöcke um dich herum.", 8, 3, 16));
    private final IntSetting threshold = add(new IntSetting("Schwelle", "Nur Werte unter dieser Zahl anzeigen (16 = alle).", 8, 1, 16));
    private final BooleanSetting sky = add(new BooleanSetting("Himmelslicht", "Himmelslicht statt Blocklicht zählen.", false));

    public LightLevelModule() {
        super("Light Level", "Zeigt Lichtwerte auf dem Boden – Spawnflächen erkennen.", Category.WORLD, "☼");
        tags("licht", "light", "spawn", "monster", "level");
    }

    @Override
    public void renderOverlay(DrawContext ctx) {
        if (mc.world == null || mc.player == null || !WorldProjector.isValid()) return;
        int r = radius.getInt(), thr = threshold.getInt();
        BlockPos center = mc.player.getBlockPos();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        int drawn = 0;
        for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) for (int dy = -3; dy <= 3; dy++) {
            pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
            BlockState st = mc.world.getBlockState(pos);
            if (!st.isAir()) continue;
            BlockState below = mc.world.getBlockState(pos.down());
            if (!below.isSolidBlock(mc.world, pos.down())) continue;
            int light = mc.world.getLightLevel(sky.isEnabled() ? LightType.SKY : LightType.BLOCK, pos);
            if (light >= thr) continue;
            float[] s = WorldProjector.project(pos.getX() + 0.5, pos.getY() + 0.02, pos.getZ() + 0.5);
            if (s == null || s[2] > r + 2) continue;
            String t = String.valueOf(light);
            int col = light == 0 ? 0xFFF87171 : light < 8 ? 0xFFFBBF24 : 0xFF4ADE80;
            ctx.drawText(mc.textRenderer, t, (int) s[0] - mc.textRenderer.getWidth(t) / 2, (int) s[1] - 4, col, true);
            if (++drawn > 600) return;
        }
    }
}
