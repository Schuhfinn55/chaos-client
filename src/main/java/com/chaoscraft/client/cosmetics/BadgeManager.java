package com.chaoscraft.client.cosmetics;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.modules.cosmetics.CosmeticsModule;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * Chaos-Namens-Badge: kleines Pixel-Icon vor dem Spielernamen (Nametag über dem
 * Kopf und Tab-Liste) für alle Chaos-Client-Spieler – wie das Logo bei
 * NoRisk/LabyMod. Das Icon ist ein Glyph der Bitmap-Schrift
 * {@code chaosclient:badge} (assets/chaosclient/font/badge.json).
 */
public final class BadgeManager {
    private BadgeManager() {}

    public static final Identifier FONT = Identifier.of(ChaosClient.MOD_ID, "badge");
    public static final String CHAOS = "";
    public static final String FLAME = "";
    public static final String CROWN = "";
    public static final String SKULL = "";

    private static final Style BADGE_STYLE = Style.EMPTY.withFont(new StyleSpriteSource.Font(FONT)).withColor(0xFFFFFF).withItalic(false).withBold(false);

    /**
     * Badge-Text (Icon + Leerzeichen). Wurzel ist ein leerer Text, damit angehängte
     * Geschwister (der Name) NICHT die Badge-Schrift erben – sonst würde jeder
     * Buchstabe als fehlendes Glyph-Kästchen gezeichnet.
     */
    public static MutableText badge(String glyph) {
        return Text.empty().append(Text.literal(glyph).setStyle(BADGE_STYLE)).append(Text.literal(" "));
    }

    /** Ob dieser Spieler den Chaos Client nutzt (eigener Account oder in der Cosmetics-API bekannt). */
    public static boolean isChaosPlayer(UUID uuid) {
        if (uuid == null) return false;
        CosmeticsManager cm = CosmeticsManager.get();
        return cm.isOwner(uuid) || cm.isChaosPlayer(uuid);
    }

    /** Ob das Badge aktiv ist (Modul-Einstellung). */
    public static boolean enabled() {
        ChaosClient cc = ChaosClient.get();
        if (cc == null) return false;
        CosmeticsModule mod = cc.getModuleManager().get(CosmeticsModule.class);
        return mod == null || mod.nameBadge().isEnabled();
    }

    /** Stellt dem Namen das Chaos-Badge voran (null-sicher, nur wenn aktiv und Chaos-Spieler). */
    public static Text decorate(UUID uuid, Text name) {
        if (name == null || !enabled() || !isChaosPlayer(uuid)) return name;
        return badge(CHAOS).append(name);
    }
}
