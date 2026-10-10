package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.player.SlotLocksModule;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Slot Locks: Items aus gesperrten Hotbar-Slots können nicht mit Q weggeworfen werden. */
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void chaosclient$blockDrop(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        SlotLocksModule mod = cc.getModuleManager().get(SlotLocksModule.class);
        if (mod != null && mod.isEnabled() && mod.isSelectedLocked()) {
            mod.notifyBlocked();
            cir.setReturnValue(false);
        }
    }
}
