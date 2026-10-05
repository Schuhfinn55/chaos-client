package com.chaoscraft.client.modules.audio;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.KeyManager;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.DoubleSetting;
import com.chaoscraft.client.settings.KeybindSetting;
import com.chaoscraft.client.ui.screens.MusicScreen;
import org.lwjgl.glfw.GLFW;

/** Music Player: Menü-Taste, Play/Pause-, Weiter-/Zurück-Tasten, Autostart. */
public class MusicPlayerModule extends Module {

    private final KeybindSetting playPause = add(new KeybindSetting("Play/Pause", "Wiedergabe starten/pausieren."));
    private final KeybindSetting nextKey = add(new KeybindSetting("Nächster Titel", "Zum nächsten Titel springen."));
    private final KeybindSetting prevKey = add(new KeybindSetting("Vorheriger Titel", "Zum vorherigen Titel springen."));
    private final DoubleSetting volume = add(new DoubleSetting("Lautstärke", "Lautstärke des Music Players.", 50, 0, 100, 5).suffix("%"));
    private final KeybindSetting volUp = add(new KeybindSetting("Lauter", "Lautstärke +5 %."));
    private final KeybindSetting volDown = add(new KeybindSetting("Leiser", "Lautstärke −5 %."));
    private final BooleanSetting autoplay = add(new BooleanSetting("Autostart", "Beim Beitreten einer Welt Musik starten.", false));
    private final BooleanSetting showNowPlaying = add(new BooleanSetting("„Jetzt läuft“-Hinweis", "Notification beim Titelwechsel.", true));

    private boolean pp, nx, pv, vu, vd;
    private boolean bound;

    public DoubleSetting volumeSetting() { return volume; }

    public MusicPlayerModule() {
        super("Music Player", "Eigene Musik (MP3/WAV/OGG) aus dem Chaos-Musikordner abspielen.", Category.AUDIO, "♪");
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
        var m = ChaosClient.get().getMusic();
        if (!bound) {
            // Einstellung ↔ Player koppeln (Einstellung ist die gespeicherte Quelle)
            m.setVolumeSilently((float) (volume.get() / 100.0));
            m.setVolumeListener(v -> volume.set((double) Math.round(v * 100)));
            bound = true;
        } else if (Math.abs(m.volume() - volume.get() / 100.0) > 0.011) {
            m.setVolumeSilently((float) (volume.get() / 100.0)); // Slider im Menü bewegt
        }
        if (mc.currentScreen != null) return;
        boolean a = KeyManager.isKeyDown(playPause.getKey()), b = KeyManager.isKeyDown(nextKey.getKey()), c = KeyManager.isKeyDown(prevKey.getKey());
        boolean u = KeyManager.isKeyDown(volUp.getKey()), d = KeyManager.isKeyDown(volDown.getKey());
        if (a && !pp) m.togglePlay();
        if (b && !nx) m.next();
        if (c && !pv) m.previous();
        if (u && !vu) m.adjustVolume(0.05f);
        if (d && !vd) m.adjustVolume(-0.05f);
        pp = a; nx = b; pv = c; vu = u; vd = d;
    }
}
