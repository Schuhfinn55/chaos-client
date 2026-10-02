package com.chaoscraft.client.mixin;

import com.chaoscraft.client.cosmetics.CapeManager;
import com.mojang.authlib.GameProfile;
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
 * Chaos-Cosmetics: ersetzt die Cape-Textur eines Spielers, wenn für seine
 * UUID ein Chaos-Cape bekannt ist (eigenes Cape aus dem Launcher oder Cape
 * eines anderen Chaos-Spielers aus API/Cache). Der Vanilla-CapeFeatureRenderer
 * rendert jedes Cape mit Bewegung/Physik, sobald cape() != null.
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
        Identifier cape;
        try { cape = CapeManager.get().capeFor(profile.id()); } catch (Exception e) { return; }
        if (cape == null) return;
        AssetInfo.TextureAsset asset = new AssetInfo.TextureAssetInfo(cape, cape);
        cir.setReturnValue(new SkinTextures(original.body(), asset, original.elytra(), original.model(), original.secure()));
    }
}
