package com.chaoscraft.client.mixin;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Zugriff auf die Overlay-Textur (Hit Color: Rotfärbung bei Schaden anpassen). */
@Mixin(OverlayTexture.class)
public interface OverlayTextureAccessor {
    @Accessor("texture") NativeImageBackedTexture chaos$texture();
}
