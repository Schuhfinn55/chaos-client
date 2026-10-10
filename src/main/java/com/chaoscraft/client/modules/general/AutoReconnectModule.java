package com.chaoscraft.client.modules.general;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.session.SessionHealer;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.IntSetting;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Auto Reconnect: nach einem Verbindungsabbruch automatisch (mit Countdown) neu verbinden. */
public class AutoReconnectModule extends Module {

    private final IntSetting delay = add(new IntSetting("Verzögerung", "Sekunden bis zum Neuverbinden.", 5, 1, 60));
    private final BooleanSetting auto = add(new BooleanSetting("Automatisch", "Ohne Klick neu verbinden, sobald der Countdown abläuft.", true));

    private Screen screen;
    private ButtonWidget button;
    private long endAt;

    public AutoReconnectModule() {
        super("Auto Reconnect", "Nach Verbindungsabbruch automatisch neu verbinden.", Category.GENERAL, "↻");
        tags("reconnect", "verbinden", "disconnect", "kick");
    }

    /** Von ScreenEvents.AFTER_INIT. */
    public void onScreenInit(Screen s) {
        if (!(s instanceof DisconnectedScreen) || !isEnabled() || SessionHealer.lastInfo == null) return;
        screen = s;
        endAt = System.currentTimeMillis() + delay.getInt() * 1000L;
        button = ButtonWidget.builder(label(), b -> reconnect()).dimensions(s.width / 2 - 100, s.height - 28, 200, 20).build();
        Screens.getButtons(s).add(button);
    }

    private Text label() {
        long left = Math.max(0, (endAt - System.currentTimeMillis() + 999) / 1000);
        return Text.literal(auto.isEnabled() ? "§c✸ §fReconnect in " + left + "s" : "§c✸ §fReconnect");
    }

    private void reconnect() {
        screen = null;
        button = null;
        ChaosClient.get().getNotifications().info("Verbinde neu …");
        SessionHealer.reconnectLast(mc);
    }

    @Override
    public void onTick() {
        if (screen == null) return;
        if (mc.currentScreen != screen) { screen = null; button = null; return; }
        if (button != null) button.setMessage(label());
        if (auto.isEnabled() && System.currentTimeMillis() >= endAt) reconnect();
    }
}
