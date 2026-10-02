package com.chaoscraft.client.modules.gui;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.ui.screens.ChaosMenuScreen;
import com.chaoscraft.client.ui.screens.CosmeticsScreen;
import com.chaoscraft.client.ui.screens.HudEditorScreen;
import com.chaoscraft.client.ui.screens.ProfilesScreen;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * ESC-Menü-Integration: ergänzt das normale Minecraft-Pausenmenü um eine
 * Zeile Chaos-Buttons (CHAOS, SETTINGS, COSMETICS, MODS, HUD EDITOR, PROFILE),
 * ohne das Vanilla-Menü zu verändern.
 */
public class EscMenuModule extends Module {

    private final BooleanSetting compact = add(new BooleanSetting("Kompakt", "Nur den CHAOS-Button statt aller Buttons anzeigen.", false));

    public EscMenuModule() {
        super("ESC-Menü", "Chaos-Buttons im Pausenmenü (ESC).", Category.GUI, "▣");
        setEnabled(true);
        tags("pause", "esc", "menu", "buttons");
    }

    public static void resetHint() {}

    /** Von ScreenEvents.AFTER_INIT aufgerufen. */
    public static void onScreenInit(Screen screen) {
        if (!(screen instanceof GameMenuScreen gm) || !gm.shouldShowMenu()) return;
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        EscMenuModule mod = cc.getModuleManager().get(EscMenuModule.class);
        if (mod == null || !mod.isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        int y = screen.height / 4 + 120 + 28;
        int cx = screen.width / 2;
        var buttons = Screens.getButtons(screen);
        if (mod.compact.isEnabled()) {
            buttons.add(ButtonWidget.builder(Text.literal("§c✸ CHAOS"), b -> mc.setScreen(new ChaosMenuScreen(screen))).dimensions(cx - 102, y, 204, 20).build());
            return;
        }
        String[] labels = {"§cCHAOS", "SETTINGS", "COSMETICS", "MODS", "HUD EDITOR", "PROFILE"};
        Runnable[] actions = {
            () -> mc.setScreen(new ChaosMenuScreen(screen)),
            () -> mc.setScreen(new ChaosMenuScreen(com.chaoscraft.client.core.Category.GUI)),
            () -> mc.setScreen(new CosmeticsScreen(screen)),
            () -> mc.setScreen(new ChaosMenuScreen(com.chaoscraft.client.core.Category.HUD)),
            () -> mc.setScreen(new HudEditorScreen(screen)),
            () -> mc.setScreen(new ProfilesScreen(screen)),
        };
        int bw = 66, gap = 3;
        int total = labels.length * bw + (labels.length - 1) * gap;
        int x = cx - total / 2;
        for (int i = 0; i < labels.length; i++) {
            final Runnable a = actions[i];
            buttons.add(ButtonWidget.builder(Text.literal(labels[i]), b -> a.run()).dimensions(x, y, bw, 20).build());
            x += bw + gap;
        }
    }
}
