package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.HudModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * ArmorStatus — armor + mainhand durability on screen.
 * Always renders 5 visible slots (helmet/chest/legs/boots/mainhand), even when
 * empty, so the element is never invisible. Equipped items show the icon, a
 * durability bar and a percentage text. Draggable HUD element.
 * Yarn mappings 1.21.11.
 */
public class ArmorStatusModule extends HudModule {

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final int SLOT = 18;

    public ArmorStatusModule() {
        super("ArmorStatus", "Show armor and item durability.", 8, 44);
    }

    @Override
    public int getHudWidth() { return SLOT + 32; }

    @Override
    public int getHudHeight() { return SLOT * 5; }

    @Override
    public void renderHud(DrawContext ctx) {
        if (mc.player == null) return;
        int y = posY;
        for (EquipmentSlot slot : SLOTS) {
            drawSlot(ctx, mc.player.getEquippedStack(slot), posX, y);
            y += SLOT;
        }
        drawSlot(ctx, mc.player.getMainHandStack(), posX, y);
    }

    private void drawSlot(DrawContext ctx, ItemStack stack, int x, int y) {
        // slot background — always drawn so the module is visible
        ctx.fill(x, y, x + SLOT - 1, y + SLOT - 1, 0x66000000);
        if (stack == null || stack.isEmpty()) return;

        ctx.drawItem(stack, x, y);

        if (stack.isDamageable()) {
            int max = stack.getMaxDamage();
            int left = Math.max(0, max - stack.getDamage());
            float ratio = max > 0 ? (float) left / max : 0f;
            int color = ratio > 0.5f ? 0xFF00E676 : ratio > 0.25f ? 0xFFFFAA00 : 0xFFFF5555;

            // durability bar under the icon
            int barW = (int) (13 * ratio);
            ctx.fill(x + 2, y + 14, x + 15, y + 15, 0xFF000000);
            ctx.fill(x + 2, y + 14, x + 2 + barW, y + 15, color);

            // percentage text right of the icon
            String pct = (int) (ratio * 100) + "%";
            ctx.drawTextWithShadow(mc.textRenderer, pct, x + SLOT + 2, y + 5, color);
        }
    }
}
