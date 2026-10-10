package com.chaoscraft.client.modules.pvp;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.mixin.OverlayTextureAccessor;
import com.chaoscraft.client.settings.ColorSetting;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

/** Hit Color: die Rotfärbung getroffener Entities in eigener Farbe. */
public class HitColorModule extends Module {

    private static final int VANILLA = 0xB3FF0000;

    private final ColorSetting color = add(new ColorSetting("Farbe", "Farbe (mit Transparenz) beim Treffer.", 0xB3E11D2E));
    private int applied = VANILLA;

    public HitColorModule() {
        super("Hit Color", "Farbe, in der getroffene Spieler/Mobs aufleuchten.", Category.PVP, "✸");
        tags("hit", "treffer", "farbe", "rot", "damage");
    }

    @Override public void onEnable() { apply(color.argb()); }
    @Override public void onDisable() { apply(VANILLA); }

    @Override
    public void onTick() {
        if (mc.player != null && mc.player.age % 20 == 0 && applied != color.argb()) apply(color.argb());
    }

    private void apply(int argb) {
        try {
            NativeImageBackedTexture tex = ((OverlayTextureAccessor) mc.gameRenderer.getOverlayTexture()).chaos$texture();
            NativeImage img = tex.getImage();
            if (img == null) return;
            for (int i = 0; i < 8; i++) for (int j = 0; j < 16; j++) img.setColorArgb(j, i, argb);
            tex.upload();
            applied = argb;
        } catch (Exception e) {
            ChaosClient.LOGGER.warn("[ChaosClient] Hit Color: {}", e.toString());
        }
    }
}
