package com.chaoscraft.client.core;

/** Hauptkategorien des Chaos-Client-Menüs. */
public enum Category {
    GENERAL("Allgemein", "⚙", "Grundfunktionen des Clients"),
    HUD("HUD", "▣", "Anzeigen auf dem Bildschirm"),
    GUI("GUI", "◈", "Aussehen des Chaos-Menüs"),
    PLAYER("Spieler", "☺", "Bewegung, Kamera, Perspektive"),
    WORLD("World", "◍", "Welt, Zeit, Waypoints"),
    PVP("PvP", "⚔", "Crosshair, Treffer, Kampf-HUD"),
    PERFORMANCE("Performance", "⚡", "FPS, Speicher, Optimierungen"),
    COSMETICS("Cosmetics", "✦", "Capes, Hüte, Emotes"),
    SOCIAL("Social", "☻", "Freunde, Party"),
    CHAT("Chat", "✉", "Chat-Darstellung, Filter, Highlights"),
    SERVER("Server", "▤", "Server-Infos, Schnellmenü"),
    SCREEN("Screen", "▭", "Fenster, Screenshots, Replay"),
    AUDIO("Audio", "♫", "Lautstärke, Musik"),
    MISC("Misc", "✧", "Sonstiges"),
    CHAOS("Chaos", "✸", "Client, Profile, Keybinds, Info");

    private final String label;
    private final String icon;
    private final String description;

    Category(String label, String icon, String description) {
        this.label = label;
        this.icon = icon;
        this.description = description;
    }

    public String getLabel() { return label; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
    /** Kompatibilität zum alten Namen. */
    public String getName() { return label; }
}
