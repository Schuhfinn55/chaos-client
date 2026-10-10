package com.chaoscraft.client.core;

import net.minecraft.client.gui.DrawContext;

/**
 * Module, die frei über den ganzen Bildschirm zeichnen (nicht verschiebbare HUD-Kästen):
 * z.B. Lebensbalken über Entities, TNT-Timer, Lichtlevel, Slot-Sperren. Wird vom
 * {@link com.chaoscraft.client.hud.HudRenderer} nach den HUD-Modulen aufgerufen.
 */
public interface HudOverlay {
    void renderOverlay(DrawContext ctx);
}
