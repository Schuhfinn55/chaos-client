package com.chaoscraft.client.mixin;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.general.ParticlesModule;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Partikel-Multiplikator: nur einen Teil der Partikel durchlassen. */
@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void chaosclient$limitParticles(Particle particle, CallbackInfo ci) {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return;
        ParticlesModule mod = cc.getModuleManager().get(ParticlesModule.class);
        if (mod != null && mod.isEnabled() && mod.shouldDrop(particle)) ci.cancel();
    }
}
