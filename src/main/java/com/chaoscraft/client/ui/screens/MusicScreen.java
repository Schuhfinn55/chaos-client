package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.music.MusicPlayer;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.Label;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;

/** Ingame-Music-Player: Playlist, Start/Pause, Vor/Zurück, Lautstärke, Ordner. */
public class MusicScreen extends ChaosScreen {

    private ScrollPanel list;
    private int px, py, pw, ph;

    public MusicScreen(Screen parent) { super("Musik", parent); }

    private MusicPlayer mp() { return ChaosClient.get().getMusic(); }

    @Override
    protected void build() {
        pw = panelWidth(520);
        ph = panelHeight(420);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        MusicPlayer m = mp();
        if (m.playlist().isEmpty()) m.scan();
        add(new Button(px + pw - 62, py + 13, 50, 20, "Zurück", this::close));
        int by = py + 52;
        add(new Button(px + 8, by, 36, 20, "⏮", m::previous));
        add(new Button(px + 48, by, 60, 20, m.isPlaying() ? "⏸ Pause" : "▶ Play", () -> { m.togglePlay(); init(); }).style(Button.Style.PRIMARY));
        add(new Button(px + 112, by, 36, 20, "⏭", m::next));
        add(new Button(px + 152, by, 36, 20, "⏹", () -> { m.stop(); init(); }));
        add(new Button(px + 196, by, 44, 20, "🔀", () -> { m.setShuffle(!m.shuffle()); init(); }).active(m.shuffle()).tooltip("Zufällig"));
        add(new Button(px + 244, by, 44, 20, "🔁", () -> { m.setRepeat(!m.repeat()); init(); }).active(m.repeat()).tooltip("Wiederholen"));
        add(new Button(px + 292, by, 36, 20, "−", () -> m.setVolume(m.volume() - 0.1f)));
        add(new Button(px + 332, by, 36, 20, "+", () -> m.setVolume(m.volume() + 0.1f)));
        add(new Button(px + pw - 150, by, 70, 20, "Scannen", () -> { m.scan(); init(); }));
        add(new Button(px + pw - 76, by, 68, 20, "Ordner", () -> Util.getOperatingSystem().open(m.dirs().get(m.dirs().size() - 1))).tooltip("Eigene WAV/OGG-Dateien hier ablegen"));
        list = add(new ScrollPanel(px + 8, py + 96, pw - 16, ph - 110));
        int y = list.y + 2;
        if (m.playlist().isEmpty()) list.add(new Label(list.x + 6, y + 4, list.w - 12, "Keine Titel. WAV oder OGG in chaos-client/music (oder Launcher-Musikordner) ablegen.", theme().textFaint()));
        for (int i = 0; i < m.playlist().size(); i++) {
            final int idx = i;
            MusicPlayer.Track t = m.playlist().get(i);
            boolean cur = m.current() == t;
            list.add(new Label(list.x + 6, y + 4, list.w - 90, (cur ? "♫ " : "   ") + t.title(), cur ? theme().accentLight() : theme().text()));
            list.add(new Button(list.x + list.w - 80, y, 70, 16, cur && m.isPlaying() ? "spielt" : "Abspielen", () -> { m.play(idx); init(); }).style(cur ? Button.Style.PRIMARY : Button.Style.DEFAULT));
            y += 20;
        }
        list.setContentHeight(y + 6 - list.y);
    }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        panel(ctx, px, py, pw, ph);
        header(ctx, px + 16, py + 12, "MUSIC", null);
        ctx.fill(px + 8, py + 44, px + pw - 8, py + 45, theme().border());
        MusicPlayer m = mp();
        String now = m.current() != null ? (m.isPlaying() ? "▶ " : "⏸ ") + m.current().title() : "Kein Titel";
        ctx.drawTextWithShadow(textRenderer, Draw.trim(now, pw - 160), px + 12, py + 80, theme().textDim());
        String vol = "Lautstärke " + Math.round(m.volume() * 100) + "%";
        ctx.drawTextWithShadow(textRenderer, vol, px + pw - 12 - textRenderer.getWidth(vol), py + 80, theme().accentLight());
    }
}
