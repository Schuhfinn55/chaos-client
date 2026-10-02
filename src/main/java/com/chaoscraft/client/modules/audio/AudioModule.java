package com.chaoscraft.client.modules.audio;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.IntSetting;

/** Audio: eigene Lautstärkeregler für Music, Effects, UI, Emotes, Cosmetics, Notifications. */
public class AudioModule extends Module {

    private final IntSetting music = add(new IntSetting("Music", "Lautstärke des Chaos-Music-Players.", 50, 0, 100).suffix("%"));
    private final IntSetting effects = add(new IntSetting("Effects", "Lautstärke von Client-Effekten.", 80, 0, 100));
    private final IntSetting ui = add(new IntSetting("UI", "Lautstärke von Menü-Klicks.", 60, 0, 100));
    private final IntSetting emotes = add(new IntSetting("Emotes", "Lautstärke von Emote-Sounds.", 80, 0, 100));
    private final IntSetting cosmetics = add(new IntSetting("Cosmetics", "Lautstärke von Cosmetic-Effekten.", 80, 0, 100));
    private final IntSetting notifications = add(new IntSetting("Notifications", "Lautstärke der Chaos-Notifications.", 70, 0, 100));

    public AudioModule() {
        super("Audio", "Lautstärkeregler für Music, Effects, UI, Emotes, Cosmetics und Notifications.", Category.AUDIO, "♫");
        alwaysOn();
        tags("sound", "lautstärke", "volume", "audio");
    }

    public float musicVolume() { return music.getInt() / 100f; }
    public float effectsVolume() { return effects.getInt() / 100f; }
    public float uiVolume() { return ui.getInt() / 100f; }
    public float emoteVolume() { return emotes.getInt() / 100f; }
    public float cosmeticsVolume() { return cosmetics.getInt() / 100f; }
    public float notificationVolume() { return notifications.getInt() / 100f; }

    @Override
    public void onTick() {
        var cc = com.chaoscraft.client.ChaosClient.get();
        if (cc != null && cc.getMusic() != null && Math.abs(cc.getMusic().volume() - musicVolume()) > 0.001f) cc.getMusic().setVolume(musicVolume());
    }
}
