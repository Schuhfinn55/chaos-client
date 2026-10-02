package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.screenshot.ScreenshotManager;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import com.chaoscraft.client.ui.widgets.Widget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/** Screenshot-Historie mit Vorschau, Öffnen, Ordner, Löschen. */
public class ScreenshotsScreen extends ChaosScreen {

    private ScrollPanel grid;
    private int px, py, pw, ph;

    public ScreenshotsScreen(Screen parent) { super("Screenshots", parent); }

    private ScreenshotManager sm() { return ChaosClient.get().getScreenshots(); }

    @Override
    protected void build() {
        pw = panelWidth(760);
        ph = panelHeight(500);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        add(new Button(px + pw - 62, py + 13, 50, 20, "Zurück", this::close));
        add(new Button(px + pw - 170, py + 13, 100, 20, "Ordner öffnen", () -> sm().openFolder()));
        add(new Button(px + pw - 270, py + 13, 94, 20, "Aufnehmen", () -> { close(); client.execute(() -> sm().take()); }).style(Button.Style.PRIMARY).tooltip("Menü schließen und Screenshot aufnehmen"));
        grid = add(new ScrollPanel(px + 8, py + 50, pw - 16, ph - 62));
        List<ScreenshotManager.Shot> shots = sm().history(60);
        int cols = Math.max(1, grid.w / 180);
        int cw = (grid.w - 12 - (cols - 1) * 8) / cols;
        int chh = (int) (cw * 9 / 16f) + 34;
        for (int i = 0; i < shots.size(); i++) {
            int cx = grid.x + 2 + (i % cols) * (cw + 8);
            int cy = grid.y + 2 + (i / cols) * (chh + 8);
            grid.add(new ShotCard(cx, cy, cw, chh, shots.get(i)));
        }
        if (shots.isEmpty()) grid.add(new com.chaoscraft.client.ui.widgets.Label(grid.x + 8, grid.y + 8, grid.w, "Noch keine Screenshots.", theme().textFaint()));
        grid.setContentHeight(((shots.size() + cols - 1) / cols) * (chh + 8) + 8);
    }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        panel(ctx, px, py, pw, ph);
        header(ctx, px + 16, py + 12, "SCREENSHOTS", null);
        ctx.fill(px + 8, py + 44, px + pw - 8, py + 45, theme().border());
    }

    @Override
    public void close() {
        sm().releaseAll();
        super.close();
    }

    private final class ShotCard extends Widget {
        private final ScreenshotManager.Shot shot;
        private final Button open, del;
        ShotCard(int x, int y, int w, int h, ScreenshotManager.Shot s) {
            super(x, y, w, h);
            shot = s;
            open = new Button(x + 4, y + h - 20, (w - 12) / 2, 16, "Öffnen", () -> sm().open(s.file()));
            del = new Button(x + 8 + (w - 12) / 2, y + h - 20, (w - 12) / 2, 16, "Löschen", () -> { sm().delete(s.file()); init(); }).style(Button.Style.DANGER);
        }
        @Override
        public void render(DrawContext ctx, int mx, int my, float delta) {
            int r = theme().radius();
            Draw.roundedRect(ctx, x, y, w, h, r, hovered(mx, my) ? theme().bg3() : theme().bg2());
            Draw.roundedBorder(ctx, x, y, w, h, r, theme().border());
            int ih = (int) ((w - 8) * 9 / 16f);
            Identifier tex = sm().preview(shot.file());
            if (tex != null) {
                int[] size = sm().previewSize(shot.file());
                ctx.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x + 4, y + 4, 0f, 0f, w - 8, ih, size[0], size[1], size[0], size[1]);
            } else {
                Draw.roundedRect(ctx, x + 4, y + 4, w - 8, ih, 4, theme().bg0());
            }
            String date = new SimpleDateFormat("dd.MM. HH:mm").format(new Date(shot.modified()));
            ctx.drawTextWithShadow(font(), Draw.trim(shot.name(), w - 60), x + 4, y + ih + 7, theme().text());
            ctx.drawTextWithShadow(font(), date, x + w - 4 - font().getWidth(date), y + ih + 7, theme().textFaint());
            open.render(ctx, mx, my, delta);
            del.render(ctx, mx, my, delta);
        }
        @Override public boolean mouseClicked(double mx, double my, int b) { return open.mouseClicked(mx, my, b) || del.mouseClicked(mx, my, b); }
    }
}
