package com.onyx.visuals.gui;

import com.onyx.visuals.OnyxVisuals;
import com.onyx.visuals.module.HudModule;
import com.onyx.visuals.module.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Drag-and-drop editor for the on-screen HUD elements.
 * Every enabled HUD module can be grabbed and moved freely.
 * Positions are saved to the config when the screen closes.
 * Yarn mappings 1.21.11.
 */
public class HudEditorScreen extends Screen {

    private final Screen parent;
    private HudModule dragging;
    private int dragOffX;
    private int dragOffY;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    private List<HudModule> huds() {
        return OnyxVisuals.getInstance().getModuleManager().getModules().stream()
                .filter(m -> m instanceof HudModule && m.isEnabled())
                .map(m -> (HudModule) m)
                .collect(Collectors.toList());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderInGameBackground(ctx);

        for (HudModule hud : huds()) {
            hud.renderHud(ctx);
            // selection outline so you see the draggable bounds
            int c = (hud == dragging) ? 0xFFE11D2E : 0xFF6B7480;
            int x = hud.getX(), y = hud.getY(), w = hud.getHudWidth(), h = hud.getHudHeight();
            ctx.fill(x - 1, y - 1, x + w + 1, y, c);
            ctx.fill(x - 1, y + h, x + w + 1, y + h + 1, c);
            ctx.fill(x - 1, y, x, y + h, c);
            ctx.fill(x + w, y, x + w + 1, y + h, c);
            // name label above
            ctx.drawTextWithShadow(textRenderer, hud.getName(), x, y - 10, 0xFFE11D2E);
        }

        String hint = "Drag to move  |  ESC: done (positions saved)";
        ctx.drawTextWithShadow(textRenderer, hint,
                this.width / 2 - textRenderer.getWidth(hint) / 2, this.height - 14, 0xFFAAB2BD);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean dc) {
        if (click.button() == 0) {
            int mx = (int) click.x(), my = (int) click.y();
            // top-most first
            List<HudModule> list = huds();
            for (int i = list.size() - 1; i >= 0; i--) {
                HudModule hud = list.get(i);
                if (hud.isInside(mx, my)) {
                    dragging = hud;
                    dragOffX = mx - hud.getX();
                    dragOffY = my - hud.getY();
                    return true;
                }
            }
        }
        return super.mouseClicked(click, dc);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (dragging != null) {
            dragging.setPos((int) click.x() - dragOffX, (int) click.y() - dragOffY);
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput key) {
        if (key.key() == 256) { close(); return true; }
        return super.keyPressed(key);
    }

    @Override
    public void close() {
        OnyxVisuals.getInstance().getConfigManager().save();
        this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
