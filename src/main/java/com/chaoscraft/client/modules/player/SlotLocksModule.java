package com.chaoscraft.client.modules.player;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudOverlay;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.ColorSetting;
import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

/** Slot Locks: Hotbar-Slots sperren – gesperrte Items können nicht weggeworfen werden. */
public class SlotLocksModule extends Module implements HudOverlay {

    private final KeybindSetting lockKey = add(new KeybindSetting("Sperren-Taste", "Sperrt/entsperrt den aktuellen Hotbar-Slot.", GLFW.GLFW_KEY_L));
    private final ColorSetting color = add(new ColorSetting("Farbe", "Rahmenfarbe gesperrter Slots.", 0xFFE11D2E));

    private final Set<Integer> locked = new HashSet<>();
    private boolean wasDown;
    private long lastNotify;

    public SlotLocksModule() {
        super("Slot Locks", "Hotbar-Slots sperren, damit nichts versehentlich weggeworfen wird.", Category.PLAYER, "⚿");
        tags("slot", "sperren", "lock", "hotbar", "q");
    }

    private boolean keyDown(int key) {
        if (key == KeybindSetting.NONE || mc.getWindow() == null) return false;
        if (key >= KeybindSetting.MOUSE_OFFSET) return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), key - KeybindSetting.MOUSE_OFFSET) == GLFW.GLFW_PRESS;
        return InputUtil.isKeyPressed(mc.getWindow(), key);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        boolean down = mc.currentScreen == null && keyDown(lockKey.getKey());
        if (down && !wasDown) {
            int slot = mc.player.getInventory().getSelectedSlot();
            if (!locked.remove(slot)) locked.add(slot);
            ChaosClient.get().getNotifications().info("Slot " + (slot + 1) + (locked.contains(slot) ? " gesperrt" : " entsperrt"));
        }
        wasDown = down;
    }

    public boolean isSelectedLocked() {
        return mc.player != null && locked.contains(mc.player.getInventory().getSelectedSlot());
    }

    public void notifyBlocked() {
        long now = System.currentTimeMillis();
        if (now - lastNotify > 1500) { lastNotify = now; ChaosClient.get().getNotifications().warn("Slot gesperrt – Taste " + com.chaoscraft.client.settings.KeybindSetting.keyName(lockKey.getKey()) + " zum Entsperren"); }
    }

    @Override
    public void renderOverlay(DrawContext ctx) {
        if (mc.player == null || locked.isEmpty() || mc.options.hudHidden) return;
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        for (int slot : locked) {
            int x = sw / 2 - 90 + slot * 20 + 2, y = sh - 20;
            Draw.roundedBorder(ctx, x, y, 18, 18, 2, color.argb());
            ctx.fill(x + 13, y + 1, x + 17, y + 5, color.argb());
        }
    }
}
