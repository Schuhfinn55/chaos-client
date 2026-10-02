package com.chaoscraft.client.settings;

import org.lwjgl.glfw.GLFW;

/** Tastenbelegung (GLFW-Keycode, -1 = keine). Maustasten: 1000 + Button. */
public class KeybindSetting extends Setting<Integer> {

    public static final int NONE = -1;
    public static final int MOUSE_OFFSET = 1000;

    private boolean listening;

    public KeybindSetting(String name, String description) { this(name, description, NONE); }
    public KeybindSetting(String name, String description, int defaultKey) { super(name, description, defaultKey); }

    public int getKey() { return value; }
    public boolean isBound() { return value != NONE; }
    public boolean isListening() { return listening; }
    public void startListening() { listening = true; }
    public void stopListening() { listening = false; }
    public void setKey(int key) { value = key; listening = false; }
    public boolean isMouse() { return value >= MOUSE_OFFSET; }

    public String getDisplayName() {
        if (listening) return "…";
        return keyName(value);
    }

    /** Lesbarer Name für GLFW-Keycodes und Maustasten. */
    public static String keyName(int key) {
        if (key == NONE) return "NONE";
        if (key >= MOUSE_OFFSET) {
            int b = key - MOUSE_OFFSET;
            return switch (b) { case 0 -> "LMB"; case 1 -> "RMB"; case 2 -> "MMB"; default -> "Mouse " + (b + 1); };
        }
        if (key >= 48 && key <= 57) return String.valueOf((char) key);
        if (key >= 65 && key <= 90) return String.valueOf((char) key);
        if (key >= GLFW.GLFW_KEY_KP_0 && key <= GLFW.GLFW_KEY_KP_9) return "NUM " + (key - GLFW.GLFW_KEY_KP_0);
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F25) return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "L-SHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RIGHT SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "CTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "R-CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "ALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "R-ALT";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> "`";
            case GLFW.GLFW_KEY_MINUS -> "-";
            case GLFW.GLFW_KEY_EQUAL -> "=";
            case GLFW.GLFW_KEY_LEFT_BRACKET -> "[";
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> "]";
            case GLFW.GLFW_KEY_SEMICOLON -> ";";
            case GLFW.GLFW_KEY_APOSTROPHE -> "'";
            case GLFW.GLFW_KEY_COMMA -> ",";
            case GLFW.GLFW_KEY_PERIOD -> ".";
            case GLFW.GLFW_KEY_SLASH -> "/";
            case GLFW.GLFW_KEY_BACKSLASH -> "\\";
            case GLFW.GLFW_KEY_INSERT -> "INS";
            case GLFW.GLFW_KEY_DELETE -> "DEL";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_PAGE_UP -> "PG UP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PG DN";
            default -> "KEY " + key;
        };
    }

    @Override public Object toJson() { return value; }
    @Override public void fromJson(Object json) {
        if (json instanceof Number n) value = n.intValue();
        else if (json instanceof String s) { try { value = Integer.parseInt(s); } catch (NumberFormatException ignored) {} }
    }
}
