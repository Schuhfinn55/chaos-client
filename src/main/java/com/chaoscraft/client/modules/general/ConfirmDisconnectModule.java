package com.chaoscraft.client.modules.general;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

/** Confirm Disconnect: Sicherheitsabfrage vor „Verbindung trennen“ im ESC-Menü. */
public class ConfirmDisconnectModule extends Module {

    public ConfirmDisconnectModule() {
        super("Confirm Disconnect", "Nachfrage, bevor du den Server oder die Welt verlässt.", Category.GENERAL, "⚠");
        tags("disconnect", "verlassen", "bestätigen", "esc");
    }

    /** Von ScreenEvents.AFTER_INIT. */
    public void onScreenInit(Screen s) {
        if (!(s instanceof GameMenuScreen) || !isEnabled()) return;
        var buttons = Screens.getButtons(s);
        for (int i = 0; i < buttons.size(); i++) {
            ClickableWidget w = buttons.get(i);
            if (!(w instanceof ButtonWidget orig)) continue;
            if (!(orig.getMessage().getContent() instanceof TranslatableTextContent tc)) continue;
            String key = tc.getKey();
            if (!key.equals("menu.disconnect") && !key.equals("menu.returnToMenu")) continue;
            ButtonWidget wrapped = ButtonWidget.builder(orig.getMessage(), b -> mc.setScreen(new ConfirmScreen(yes -> {
                if (yes) { mc.setScreen(s); orig.onPress(null); }
                else mc.setScreen(s);
            }, Text.literal("Wirklich verlassen?"), Text.literal(mc.isInSingleplayer() ? "Die Welt wird gespeichert und geschlossen." : "Du wirst vom Server getrennt."), Text.literal("Verlassen"), Text.literal("Bleiben")))).dimensions(orig.getX(), orig.getY(), orig.getWidth(), orig.getHeight()).build();
            buttons.set(i, wrapped);
            return;
        }
    }
}
