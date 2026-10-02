package com.chaoscraft.client.modules.audio;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.KeyManager;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.ui.screens.MusicScreen;
import org.lwjgl.glfw.GLFW;

/** Music Player: Menü-Taste, Play/Pause-, Weiter-/Zurück-Tasten, Autostart. */
public class MusicPlayerModule extends Module {

    private final KeybindSetting playPause = add(new KeybindSetting("Play/Pause", "Wiedergabe starten/pausieren."));
    private final KeybindSetting nextKey = add(new KeybindSetting("Nächster Titel", "Zum nächsten Titel springen."));
    private final KeybindSetting prevKey = add(new KeybindSetting("Vorheriger Titel", "Zum vorherigen Titel springen."));
    private final BooleanSetting autoplay = add(new BooleanSetting("Autostart", "Beim Beitreten einer Welt Musik starten.", false));
    private final BooleanSetting showNowPlaying = add(new BooleanSetting("„Jetzt läuft“-Hinweis", "Notification beim Titelwechsel.", true));

    private boolean pp, nx, pv;

    public MusicPlayerModule() {
        super("Music Player", "Eigene Musik (WAV/OGG) aus dem Chaos-Musikordner abspielen.", Category.AUDIO, "♪");
        getKeybindSetting().set(GLFW.GLFW_KEY_M);
        alwaysOn();
        tags("music", "musik", "player", "playlist");
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled && isEnabled() && mc.currentScreen == null && mc.player != null) { mc.setScreen(new MusicScreen(null)); return; }
        super.setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc.currentScreen != null) return;
        var m = ChaosClient.get().getMusic();
        boolean a = KeyManager.isKeyDown(playPause.getKey()), b = KeyManager.isKeyDown(nextKey.getKey()), c = KeyManager.isKeyDown(prevKey.getKey());
        if (a && !pp) m.togglePlay();
        if (b && !nx) m.next();
        if (c && !pv) m.previous();
        pp = a; nx = b; pv = c;
    }
}
