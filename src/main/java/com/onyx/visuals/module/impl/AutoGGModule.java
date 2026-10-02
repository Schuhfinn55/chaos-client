package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.Category;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.setting.IntSetting;

/**
 * AutoGG — sends "GG" when a game-end message is detected in chat.
 * ChatListenerMixin calls onChatMessage(); the send happens here in onTick
 * after a configurable delay.
 * Yarn mappings 1.21.11.
 */
public class AutoGGModule extends Module {

    private final IntSetting delay = add(new IntSetting("Delay (s)", "Seconds before sending.", 1, 0, 5));

    private long triggerTime = -1;

    public AutoGGModule() {
        super("AutoGG", "Automatically say GG when the round ends.", Category.UTILITY);
    }

    /** Called from ChatListenerMixin whenever a chat message arrives. */
    public void onChatMessage(String raw) {
        String msg = raw.toLowerCase();
        if (msg.contains("game over") || msg.contains("winner") || msg.contains("has won")
                || msg.contains("victory") || msg.contains("gewonnen")) {
            triggerTime = System.currentTimeMillis() + delay.getInt() * 1000L;
        }
    }

    @Override
    public void onTick() {
        if (triggerTime > 0 && System.currentTimeMillis() >= triggerTime) {
            triggerTime = -1;
            if (mc.player != null && mc.player.networkHandler != null) {
                mc.player.networkHandler.sendChatMessage("GG");
            }
        }
    }
}
