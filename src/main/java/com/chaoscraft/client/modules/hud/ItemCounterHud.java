package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.StringSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Item Counter: Anzahl bestimmter Items im Inventar (gehaltenes Item + eigene Liste). */
public class ItemCounterHud extends HudModule {

    private final BooleanSetting held = add(new BooleanSetting("Gehaltenes Item", "Anzahl des Items in der Hand zählen.", true));
    private final StringSetting items = add(new StringSetting("Items", "Kommagetrennte IDs, z.B. arrow,ender_pearl,golden_apple.", "arrow,ender_pearl", 300));
    private final BooleanSetting hideZero = add(new BooleanSetting("Null ausblenden", "Items mit Anzahl 0 nicht anzeigen.", true));

    private record Entry(ItemStack icon, int count) {}

    public ItemCounterHud() {
        super("Item Counter", "Zählt Items im Inventar: Pfeile, Perlen, Äpfel …", Category.HUD, "#", 8, 240);
        tags("zähler", "counter", "pfeile", "perlen", "items");
    }

    private int count(Item item) {
        int n = 0;
        var inv = mc.player.getInventory();
        for (int i = 0; i < inv.size(); i++) { ItemStack s = inv.getStack(i); if (s.getItem() == item) n += s.getCount(); }
        return n;
    }

    private List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        if (mc.player == null) return out;
        if (held.isEnabled()) {
            ItemStack h = mc.player.getMainHandStack();
            if (!h.isEmpty()) out.add(new Entry(h, count(h.getItem())));
        }
        for (String id : items.get().split(",")) {
            String t = id.trim().toLowerCase(java.util.Locale.ROOT);
            if (t.isEmpty()) continue;
            Identifier ident = Identifier.tryParse(t.contains(":") ? t : "minecraft:" + t);
            if (ident == null) continue;
            Item it = Registries.ITEM.get(ident);
            if (it == null || it == Items.AIR) continue;
            if (held.isEnabled() && mc.player.getMainHandStack().getItem() == it) continue;
            int c = count(it);
            if (c == 0 && hideZero.isEnabled()) continue;
            out.add(new Entry(new ItemStack(it), c));
        }
        return out;
    }

    @Override public int getContentWidth() { return 70; }
    @Override public int getContentHeight() { int n = entries().size(); return n == 0 ? 10 : n * 18; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        List<Entry> list = entries();
        if (list.isEmpty()) { text(ctx, "Keine Items", x, y, getContentWidth(), textColor.withAlpha(0.6)); return; }
        int cy = y;
        for (Entry e : list) {
            ctx.drawItem(e.icon(), x, cy + 1);
            text(ctx, "× " + e.count(), x + 20, cy + 5, getContentWidth() - 20);
            cy += 18;
        }
    }
}
