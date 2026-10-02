package com.onyx.visuals.gui;

import com.onyx.visuals.module.Category;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import com.onyx.visuals.setting.BooleanSetting;
import com.onyx.visuals.setting.DoubleSetting;
import com.onyx.visuals.setting.EnumSetting;
import com.onyx.visuals.setting.KeybindSetting;
import com.onyx.visuals.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Onyx Visuals ClickGUI — NoRisk-style, draggable category windows.
 * L-click toggles a module, R-click expands its settings.
 * Yarn mappings 1.21.11.
 */
public class ClickGuiScreen extends Screen {

    // Original color scheme
    private static final int COL_BG          = 0xF00B0A0C;
    private static final int COL_HEADER      = 0xFF1A1114;
    private static final int COL_HEADER_HOV  = 0xFF26161B;
    private static final int COL_MODULE      = 0xFF121014;
    private static final int COL_MODULE_HOV  = 0xFF1E1518;
    private static final int COL_SETTING_BG  = 0xFF0A0809;
    private static final int COL_ACCENT      = 0xFFE11D2E;
    private static final int COL_ENABLED     = 0xFFE11D2E;
    private static final int COL_DISABLED    = 0xFFAAB2BD;
    private static final int COL_TEXT_DIM    = 0xFF6B7480;
    private static final int COL_SLIDER_BG   = 0xFF2A1A1E;
    private static final int COL_SLIDER_FILL = 0xFFE11D2E;
    private static final int COL_BORDER      = 0xFFE11D2E;

    private final ModuleManager moduleManager;
    private final Map<Category, CategoryWindow> windows = new EnumMap<>(Category.class);

    private CategoryWindow dragWindow;
    private DoubleSetting draggingSlider;
    private KeybindSetting capturingKeybind;

    public boolean isCapturingKeybind() { return capturingKeybind != null; }

    public ClickGuiScreen(ModuleManager moduleManager) {
        super(Text.literal("Chaos Client"));
        this.moduleManager = moduleManager;
    }

    @Override
    protected void init() {
        windows.clear();
        int x = 20;
        for (Category category : Category.values()) {
            windows.put(category, new CategoryWindow(category, x, 30));
            x += 160; // Original spacing
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderInGameBackground(ctx);
        for (Category category : Category.values()) {
            renderWindow(ctx, windows.get(category), moduleManager.getByCategory(category), mouseX, mouseY);
        }
        ctx.drawTextWithShadow(textRenderer, "§cChaos §7Client §8| §7L: toggle §8| §7R: settings §8| §7ESC: close",
                6, this.height - 12, COL_TEXT_DIM);

        // "Edit HUD" button top-right
        int bw = 90, bh = 16, bx = this.width - bw - 8, by = 8;
        boolean bhov = inBounds(mouseX, mouseY, bx, by, bw, bh);
        ctx.fill(bx, by, bx + bw, by + bh, bhov ? 0xFF1E2832 : 0xFF161D27);
        drawBorder(ctx, bx, by, bw, bh, COL_ACCENT);
        String label = "Edit HUD";
        ctx.drawTextWithShadow(textRenderer, label, bx + (bw - textRenderer.getWidth(label)) / 2, by + 4, COL_ACCENT);
    }

    private boolean overEditButton(int mx, int my) {
        int bw = 90, bh = 16, bx = this.width - bw - 8, by = 8;
        return inBounds(mx, my, bx, by, bw, bh);
    }

    private void renderWindow(DrawContext ctx, CategoryWindow win, List<Module> modules, int mx, int my) {
        int x = win.getX(), y = win.getY(), w = win.getWidth();

        int totalH = win.getHeaderHeight() + win.getPadding();
        for (Module m : modules) {
            totalH += win.getModuleHeight();
            if (win.isModuleExpanded(m.getName())) {
                for (Setting<?> s : m.getSettings()) totalH += s.getHeight();
            }
        }

        ctx.fill(x, y, x + w, y + totalH, COL_BG);
        drawBorder(ctx, x, y, w, totalH, COL_BORDER);

        boolean hov = win.isHeaderHovered(mx, my);
        ctx.fill(x + 1, y + 1, x + w - 1, y + win.getHeaderHeight(), hov ? COL_HEADER_HOV : COL_HEADER);
        ctx.fill(x + 1, y + win.getHeaderHeight(), x + w - 1, y + win.getHeaderHeight() + 1, COL_ACCENT);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal(win.getCategory().getName()).formatted(Formatting.BOLD),
                x + 8, y + 6, COL_ACCENT);
        String count = String.valueOf(modules.size());
        ctx.drawTextWithShadow(textRenderer, count, x + w - 8 - textRenderer.getWidth(count), y + 6, COL_TEXT_DIM);

        int my2 = y + win.getHeaderHeight() + win.getPadding();
        for (Module module : modules) {
            int mh = win.getModuleHeight();
            boolean mHov = inBounds(mx, my, x + 1, my2, w - 2, mh);

            ctx.fill(x + 1, my2, x + w - 1, my2 + mh, mHov ? COL_MODULE_HOV : COL_MODULE);
            if (module.isEnabled()) ctx.fill(x + 1, my2, x + 3, my2 + mh, COL_ACCENT);

            int lc = module.isEnabled() ? COL_ENABLED : COL_DISABLED;
            ctx.drawTextWithShadow(textRenderer, module.getName(), x + 8, my2 + 4, lc);

            if (module.hasSettings()) {
                String arrow = win.isModuleExpanded(module.getName()) ? "-" : "+";
                ctx.drawTextWithShadow(textRenderer, arrow, x + w - 14, my2 + 4, COL_TEXT_DIM);
            }

            my2 += mh;

            if (win.isModuleExpanded(module.getName())) {
                for (Setting<?> setting : module.getSettings()) {
                    renderSetting(ctx, setting, x + 1, my2, w - 2, mx, my);
                    my2 += setting.getHeight();
                }
            }
        }
    }

    private void renderSetting(DrawContext ctx, Setting<?> s, int x, int y, int w, int mx, int my) {
        if (s instanceof BooleanSetting bs) renderBool(ctx, bs, x, y, w);
        else if (s instanceof DoubleSetting ds) renderDouble(ctx, ds, x, y, w);
        else if (s instanceof EnumSetting<?> es) renderEnum(ctx, es, x, y, w);
        else if (s instanceof KeybindSetting ks) renderKey(ctx, ks, x, y, w);
        else ctx.drawTextWithShadow(textRenderer, s.getName() + ": " + s.get(), x + 6, y + 3, COL_TEXT_DIM);
    }

    private void renderBool(DrawContext ctx, BooleanSetting bs, int x, int y, int w) {
        ctx.fill(x, y, x + w, y + bs.getHeight(), COL_SETTING_BG);
        ctx.drawTextWithShadow(textRenderer, bs.getName(), x + 8, y + 3, COL_DISABLED);
        int tx = x + w - 22, ty = y + 3;
        ctx.fill(tx, ty, tx + 16, ty + 8, bs.isEnabled() ? COL_ACCENT : COL_SLIDER_BG);
        int knobX = bs.isEnabled() ? tx + 9 : tx + 1;
        ctx.fill(knobX, ty + 1, knobX + 6, ty + 7, 0xFFFFFFFF);
    }

    private void renderDouble(DrawContext ctx, DoubleSetting ds, int x, int y, int w) {
        int h = ds.getHeight();
        ctx.fill(x, y, x + w, y + h, COL_SETTING_BG);
        ctx.drawTextWithShadow(textRenderer, ds.getName(), x + 8, y + 2, COL_DISABLED);
        String val = String.format("%.1f", ds.get());
        ctx.drawTextWithShadow(textRenderer, val, x + w - 8 - textRenderer.getWidth(val), y + 2, COL_ACCENT);
        int tx = x + 8, ty = y + 12, tw = w - 16, th = 2;
        ctx.fill(tx, ty, tx + tw, ty + th, COL_SLIDER_BG);
        int fw = (int) (tw * ds.getProgress());
        ctx.fill(tx, ty, tx + fw, ty + th, COL_SLIDER_FILL);
        ctx.fill(tx + fw - 1, ty - 2, tx + fw + 2, ty + th + 2, 0xFFFFFFFF);
    }

    private void renderEnum(DrawContext ctx, EnumSetting<?> es, int x, int y, int w) {
        ctx.fill(x, y, x + w, y + es.getHeight(), COL_SETTING_BG);
        ctx.drawTextWithShadow(textRenderer, es.getName(), x + 8, y + 3, COL_DISABLED);
        String v = es.get().toString();
        ctx.drawTextWithShadow(textRenderer, "< " + v + " >", x + w - 8 - textRenderer.getWidth("< " + v + " >"), y + 3, COL_ACCENT);
    }

    private void renderKey(DrawContext ctx, KeybindSetting ks, int x, int y, int w) {
        int h = ks.getHeight();
        ctx.fill(x, y, x + w, y + h, COL_SETTING_BG);
        ctx.drawTextWithShadow(textRenderer, ks.getName(), x + 8, y + 3, COL_DISABLED);
        String d = ks.getDisplayName();
        int dc = ks.isListening() ? 0xFFFFAA00 : (ks.isBound() ? COL_ACCENT : COL_TEXT_DIM);
        ctx.drawTextWithShadow(textRenderer, d, x + w - 8 - textRenderer.getWidth(d), y + 3, dc);
    }

    @Override
    public boolean mouseClicked(Click click, boolean dc) {
        int mx = (int) click.x(), my = (int) click.y(), btn = click.button();

        // "Edit HUD" button
        if (btn == 0 && overEditButton(mx, my)) {
            MinecraftClient.getInstance().setScreen(new HudEditorScreen(this));
            return true;
        }

        if (btn == 0 && draggingSlider != null) draggingSlider = null;

        List<Category> cats = new ArrayList<>(List.of(Category.values()));
        for (int i = cats.size() - 1; i >= 0; i--) {
            CategoryWindow win = windows.get(cats.get(i));
            if (win.isHeaderHovered(mx, my)) {
                if (btn == 0) { win.startDrag(mx, my); dragWindow = win; }
                return true;
            }
        }

        for (Category cat : Category.values()) {
            CategoryWindow win = windows.get(cat);
            int y = win.getY() + win.getHeaderHeight() + win.getPadding();
            for (Module module : moduleManager.getByCategory(cat)) {
                int mh = win.getModuleHeight();
                if (inBounds(mx, my, win.getX() + 1, y, win.getWidth() - 2, mh)) {
                    if (btn == 0) module.toggle();
                    else if (btn == 1 && module.hasSettings()) win.toggleExpand(module.getName());
                    return true;
                }
                y += mh;
                if (win.isModuleExpanded(module.getName())) {
                    for (Setting<?> s : module.getSettings()) {
                        int sx = win.getX() + 1, sw = win.getWidth() - 2;
                        if (inBounds(mx, my, sx, y, sw, s.getHeight())) {
                            handleSettingClick(s, mx, sx, sw, btn);
                            return true;
                        }
                        y += s.getHeight();
                    }
                }
            }
        }
        return super.mouseClicked(click, dc);
    }

    private void handleSettingClick(Setting<?> s, int mx, int x, int w, int btn) {
        if (s instanceof BooleanSetting bs) bs.toggle();
        else if (s instanceof DoubleSetting ds) {
            if (btn == 0) {
                double p = Math.max(0, Math.min(1, (double) (mx - (x + 8)) / (w - 16)));
                ds.setFromProgress(p);
                draggingSlider = ds;
            }
        } else if (s instanceof EnumSetting<?> es) es.cycle(btn == 0);
        else if (s instanceof KeybindSetting ks) {
            if (capturingKeybind != null) capturingKeybind.stopListening();
            if (btn == 0) { ks.startListening(); capturingKeybind = ks; }
            else if (btn == 1) ks.setKey(-1);
        }
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        int mx = (int) click.x(), my = (int) click.y();
        if (dragWindow != null && dragWindow.isDragging()) {
            dragWindow.drag(mx, my);
            return true;
        }
        if (draggingSlider != null) {
            for (CategoryWindow win : windows.values()) {
                int y = win.getY() + win.getHeaderHeight() + win.getPadding();
                for (Module m : moduleManager.getByCategory(win.getCategory())) {
                    y += win.getModuleHeight();
                    if (win.isModuleExpanded(m.getName())) {
                        for (Setting<?> s : m.getSettings()) {
                            if (s == draggingSlider) {
                                int sx = win.getX() + 1 + 8;
                                int sw = win.getWidth() - 2 - 16;
                                double p = Math.max(0, Math.min(1, (double) (mx - sx) / Math.max(1, sw)));
                                draggingSlider.setFromProgress(p);
                                return true;
                            }
                            y += s.getHeight();
                        }
                    }
                }
            }
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragWindow != null) { dragWindow.stopDrag(); dragWindow = null; }
        draggingSlider = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput key) {
        int code = key.key();
        if (capturingKeybind != null && capturingKeybind.isListening()) {
            if (code == 256) { capturingKeybind.stopListening(); capturingKeybind = null; }
            else { capturingKeybind.setKey(code); capturingKeybind = null; }
            return true;
        }
        if (code == 256) { close(); return true; }
        return super.keyPressed(key);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        com.onyx.visuals.OnyxVisuals.getInstance().getConfigManager().save();
        super.close();
    }

    private static boolean inBounds(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }
}