package com.chaoscraft.client.modules.chat;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.ColorSetting;
import com.chaoscraft.client.settings.IntSetting;
import com.chaoscraft.client.settings.StringSetting;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Erweiterter Chat: Zeitstempel, Mention-/Wort-Highlight mit Farbe und Sound,
 * Nachrichtenfilter (Werbung, Spam, System, Join/Quit, Private), eigene
 * Chatfarbe, Position/Größe/Transparenz über Vanilla-Optionen.
 * Verarbeitung im ChatHudMixin.
 */
public class ChatModule extends Module {

    private final BooleanSetting timestamps = add(new BooleanSetting("Zeitstempel", "[HH:mm] vor jeder Nachricht.", false));
    private final BooleanSetting mention = add(new BooleanSetting("Mention Highlight", "Nachrichten mit deinem Namen hervorheben.", true));
    private final StringSetting highlightWords = add(new StringSetting("Highlight-Wörter", "Kommagetrennt, z.B. Chaoscraft,Onyx,Admin,Support.", "Chaoscraft,Admin,Support", 200));
    private final ColorSetting highlightColor = add(new ColorSetting("Highlight-Farbe", "Farbe hervorgehobener Nachrichten.", 0xFFE11D2E, false));
    private final BooleanSetting highlightSound = add(new BooleanSetting("Highlight-Sound", "Ton bei Hervorhebung.", true));
    private final BooleanSetting filterAds = add(new BooleanSetting("Filter: Werbung", "Nachrichten mit Server-Werbung ausblenden.", false));
    private final BooleanSetting filterSpam = add(new BooleanSetting("Filter: Spam", "Identische Nachrichten kurz hintereinander ausblenden.", false));
    private final BooleanSetting filterJoinQuit = add(new BooleanSetting("Filter: Join/Quit", "Beitritts-/Verlassen-Meldungen ausblenden.", false));
    private final BooleanSetting filterSystem = add(new BooleanSetting("Filter: System", "Systemnachrichten (ohne Spielername) ausblenden.", false));
    private final BooleanSetting filterPrivate = add(new BooleanSetting("Filter: Private", "Private Nachrichten (/msg) ausblenden.", false));
    private final StringSetting customFilter = add(new StringSetting("Eigener Filter", "Kommagetrennte Wörter, die ausgeblendet werden.", "", 200));
    private final BooleanSetting customColor = add(new BooleanSetting("Eigene Chatfarbe", "Normale Nachrichten in eigener Farbe.", false));
    private final ColorSetting chatColor = add(new ColorSetting("Chatfarbe", "Farbe normaler Nachrichten.", 0xFFF4F1F2, false));
    private final IntSetting opacity = add(new IntSetting("Transparenz", "Chat-Deckkraft in Prozent (Vanilla-Option).", 100, 10, 100));
    private final IntSetting scale = add(new IntSetting("Größe", "Chat-Skalierung in Prozent (Vanilla-Option).", 100, 50, 100));
    private final IntSetting width = add(new IntSetting("Breite", "Chat-Breite in Prozent (Vanilla-Option).", 100, 40, 100));

    private String lastMessage = "";
    private long lastMessageAt;
    private int lastOpacity = -1, lastScale = -1, lastWidth = -1;

    public ChatModule() {
        super("Chat", "Zeitstempel, Highlights, Filter, Farben, Größe und Transparenz des Chats.", Category.CHAT, "✉");
        tags("chat", "filter", "highlight", "mention", "zeitstempel", "nachrichten");
    }

    @Override
    public void onTick() {
        if (mc.options == null) return;
        if (opacity.getInt() != lastOpacity) { mc.options.getChatOpacity().setValue(opacity.getInt() / 100.0); lastOpacity = opacity.getInt(); }
        if (scale.getInt() != lastScale) { mc.options.getChatScale().setValue(scale.getInt() / 100.0); lastScale = scale.getInt(); }
        if (width.getInt() != lastWidth) { mc.options.getChatWidth().setValue(width.getInt() / 100.0); lastWidth = width.getInt(); }
    }

    /** Liefert null = Nachricht ausblenden, sonst die (ggf. veränderte) Nachricht. */
    public Text process(Text message) {
        if (!isEnabled()) return message;
        String raw = message.getString();
        String lower = raw.toLowerCase(Locale.ROOT);
        // Filter
        if (filterJoinQuit.isEnabled() && (lower.endsWith("joined the game") || lower.endsWith("left the game") || lower.contains("hat das spiel betreten") || lower.contains("hat das spiel verlassen"))) return null;
        if (filterAds.isEnabled() && (lower.matches(".*\\b[a-z0-9-]+\\.(net|de|com|org|eu|gg|io)\\b.*") && !lower.contains("chaoscraft"))) return null;
        if (filterPrivate.isEnabled() && (lower.startsWith("[") && (lower.contains("-> me") || lower.contains("-> dir") || lower.startsWith("[msg")) || lower.contains("whispers to you") || lower.contains("flüstert dir"))) return null;
        if (filterSystem.isEnabled() && !raw.contains(":") && !raw.contains(">")) return null;
        if (filterSpam.isEnabled()) {
            long now = System.currentTimeMillis();
            if (raw.equals(lastMessage) && now - lastMessageAt < 5000) return null;
            lastMessage = raw; lastMessageAt = now;
        }
        for (String w : customFilter.get().toLowerCase(Locale.ROOT).split(",")) { String t = w.trim(); if (!t.isEmpty() && lower.contains(t)) return null; }

        // Highlight
        boolean hl = false;
        if (mention.isEnabled() && mc.player != null && lower.contains(mc.player.getName().getString().toLowerCase(Locale.ROOT)) && !lower.startsWith("<" + mc.player.getName().getString().toLowerCase(Locale.ROOT) + ">")) hl = true;
        if (!hl) for (String w : highlightWords.get().toLowerCase(Locale.ROOT).split(",")) { String t = w.trim(); if (!t.isEmpty() && lower.contains(t)) { hl = true; break; } }

        MutableText out = Text.empty();
        if (timestamps.isEnabled()) out.append(Text.literal("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x8A8A8A))));
        if (hl) {
            out.append(Text.literal("★ ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(highlightColor.rgb()))));
            out.append(message.copy().styled(s -> s.getColor() == null ? s.withColor(TextColor.fromRgb(highlightColor.rgb())) : s));
            if (highlightSound.isEnabled()) {
                float vol = ChaosClient.get().getAudio().notificationVolume();
                if (vol > 0) mc.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK.value(), 1.6f, vol));
            }
        } else if (customColor.isEnabled()) {
            out.append(message.copy().styled(s -> s.getColor() == null ? s.withColor(TextColor.fromRgb(chatColor.rgb())) : s));
        } else {
            if (!timestamps.isEnabled()) return message;
            out.append(message);
        }
        return out;
    }
}
