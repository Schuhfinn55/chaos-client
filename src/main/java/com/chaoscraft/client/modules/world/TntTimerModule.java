package com.chaoscraft.client.modules.world;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudOverlay;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.render.WorldProjector;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;

/** TNT Timer: Restzeit bis zur Explosion über gezündetem TNT. */
public class TntTimerModule extends Module implements HudOverlay {

    private final BooleanSetting decimals = add(new BooleanSetting("Zehntel", "Zeit mit einer Nachkommastelle.", true));

    public TntTimerModule() {
        super("TNT Timer", "Zeigt über gezündetem TNT, wann es explodiert.", Category.WORLD, "✹");
        tags("tnt", "timer", "explosion", "zünder");
    }

    @Override
    public void renderOverlay(DrawContext ctx) {
        if (mc.world == null || !WorldProjector.isValid()) return;
        float delta = mc.getRenderTickCounter().getTickProgress(true);
        for (Entity en : mc.world.getEntities()) {
            if (!(en instanceof TntEntity tnt)) continue;
            var p = tnt.getLerpedPos(delta);
            float[] s = WorldProjector.project(p.x, p.y + 1.1, p.z);
            if (s == null) continue;
            float sec = Math.max(0f, tnt.getFuse() / 20f);
            String t = decimals.isEnabled() ? String.format(java.util.Locale.ROOT, "%.1fs", sec) : Math.round(sec) + "s";
            int col = sec < 1f ? 0xFFF87171 : sec < 2.5f ? 0xFFFBBF24 : 0xFFFFFFFF;
            int w = mc.textRenderer.getWidth(t) + 6;
            Draw.roundedRect(ctx, (int) s[0] - w / 2, (int) s[1] - 6, w, 12, 3, 0xAA000000);
            ctx.drawText(mc.textRenderer, t, (int) s[0] - (w - 6) / 2, (int) s[1] - 4, col, true);
        }
    }
}
