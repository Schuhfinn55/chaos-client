package com.onyx.visuals.setting;

/** A keybind setting — stores a GLFW keycode. Default is -1 (unbound). */
public class KeybindSetting extends Setting<Integer> {

    private boolean listening;

    public KeybindSetting(String name, String description) {
        super(name, description, -1);
    }

    public int getKey() { return value; }
    public boolean isBound() { return value != -1; }
    public boolean isListening() { return listening; }

    public void startListening() { listening = true; }
    public void stopListening() { listening = false; }

    public void setKey(int key) {
        value = key;
        listening = false;
    }

    public String getDisplayName() {
        if (listening) return "...";
        if (!isBound()) return "None";
        return keyName(value);
    }

    /** Human-readable name for common GLFW keys. */
    public static String keyName(int key) {
        if (key == -1) return "None";
        if (key >= 48 && key <= 57) return String.valueOf((char) key);
        if (key >= 65 && key <= 90) return String.valueOf((char) key);
        return switch (key) {
            case 32  -> "Space";
            case 256 -> "ESC";
            case 257 -> "Enter";
            case 258 -> "Tab";
            case 340 -> "L-Shift";
            case 341 -> "L-Ctrl";
            case 342 -> "R-Shift";
            case 343 -> "R-Ctrl";
            case 344 -> "L-Alt";
            case 262 -> "Right";
            case 263 -> "Left";
            case 264 -> "Down";
            case 265 -> "Up";
            case 290 -> "F1"; case 291 -> "F2"; case 292 -> "F3"; case 293 -> "F4";
            case 294 -> "F5"; case 295 -> "F6"; case 296 -> "F7"; case 297 -> "F8";
            case 298 -> "F9"; case 299 -> "F10"; case 300 -> "F11"; case 301 -> "F12";
            default  -> "Key " + key;
        };
    }

    @Override
    public int getHeight() { return 14; } // Original size
}