package com.onyx.visuals.module;

public enum Category {
    RENDER("Render",    0xFF55FFFF),
    HUD("HUD",          0xFF55AAFF),
    MOVEMENT("Movement", 0xFFFFAA55),
    UTILITY("Utility",  0xFFAAFF55),
    BOT("Bot",          0xFFFF5577);

    private final String name;
    private final int color;

    Category(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public int getColor() { return color; }
}