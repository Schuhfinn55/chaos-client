package com.chaoscraft.client.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Mixin;

/** Zugriff auf die (final) Sitzungsfelder, um die Spielsitzung ohne Neustart zu erneuern. */
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("session") @Mutable void chaos$setSession(Session session);
    @Accessor("userApiService") @Mutable void chaos$setUserApiService(UserApiService service);
    @Accessor("profileKeys") @Mutable void chaos$setProfileKeys(ProfileKeys keys);
}
