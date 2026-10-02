package com.onyx.visuals.mixin;

import com.mojang.authlib.GameProfile;
import com.onyx.visuals.cosmetics.CapeManager;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Chaos-Cosmetics: ersetzt die Cape-Textur eines Spielers, wenn für
 * seine UUID ein Chaos-Cape bekannt ist. {@code PlayerListEntry.getSkinTextures()}
 * ist die zentrale Quelle für Spieler-Modell und Tab-Liste; der
 * {@code CapeFeatureRenderer} rendert jedes Cape, sobald
 * {@code SkinTextures.cape()} nicht null ist – inklusive Bewegung,
 * Sprinten, Sneaken und Fliegen (Vanilla-Physik).
 * Yarn mappings 1.21.11.
 */
@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Shadow public abstract GameProfile getProfile();

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void chaosclient$applyCape(CallbackInfoReturnable<SkinTextures> cir) {
        SkinTextures original = cir.getReturnValue();
        if (original == null) return;
        GameProfile profile = getProfile();
        if (profile == null) return;
        Identifier cape = CapeManager.get().capeFor(profile.id());
        if (cape == null) return;
        AssetInfo.TextureAsset asset = new AssetInfo.TextureAssetInfo(cape, cape);
        cir.setReturnValue(new SkinTextures(
                original.body(),
                asset,
                original.elytra(),
                original.model(),
                original.secure()
        ));
    }
}
