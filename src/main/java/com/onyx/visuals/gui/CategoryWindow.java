package com.onyx.visuals.gui;

import com.onyx.visuals.module.Category;

/** A draggable window in the ClickGUI, one per category. */
public class CategoryWindow {

    // Original dimensions
    private static final int PANEL_WIDTH = 150;
    private static final int HEADER_HEIGHT = 20;
    private static final int MODULE_HEIGHT = 16;
    private static final int PADDING = 4;

    private final Category category;
    private int x;
    private int y;
    private String expandedModule;

    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public CategoryWindow(Category category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;
    }

    public Category getCategory() { return category; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return PANEL_WIDTH; }
    public int getHeaderHeight() { return HEADER_HEIGHT; }
    public int getModuleHeight() { return MODULE_HEIGHT; }
    public int getPadding() { return PADDING; }

    public boolean isDragging() { return dragging; }
    public void startDrag(int mouseX, int mouseY) {
        dragging = true;
        dragOffsetX = mouseX - x;
        dragOffsetY = mouseY - y;
    }
    public void drag(int mouseX, int mouseY) {
        x = mouseX - dragOffsetX;
        y = mouseY - dragOffsetY;
    }
    public void stopDrag() { dragging = false; }

    public boolean isModuleExpanded(String moduleName) {
        return moduleName.equals(expandedModule);
    }
    public void toggleExpand(String moduleName) {
        if (moduleName.equals(expandedModule)) expandedModule = null;
        else expandedModule = moduleName;
    }

    public boolean isHeaderHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + PANEL_WIDTH
                && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
    }
}