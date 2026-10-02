package com.chaoscraft.client.waypoints;

import com.chaoscraft.client.ChaosClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Waypoints: Name, Farbe, Koordinaten, Dimension, aktiv. Gespeichert in
 * config/chaosclient/waypoints.json – bleiben nach Neustart erhalten.
 */
public final class WaypointManager {

    public static final class Waypoint {
        public String id;
        public String name;
        public int color;
        public int x, y, z;
        public String dimension;
        public boolean enabled = true;
        public String server = "";

        public Waypoint(String name, int color, int x, int y, int z, String dimension) {
            this.id = Long.toHexString(System.nanoTime());
            this.name = name; this.color = color; this.x = x; this.y = y; this.z = z; this.dimension = dimension;
        }
        public BlockPos pos() { return new BlockPos(x, y, z); }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final List<Waypoint> waypoints = new ArrayList<>();

    private Path file() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve("chaosclient").resolve("waypoints.json");
    }

    public List<Waypoint> all() { return waypoints; }

    public List<Waypoint> forCurrentDimension() {
        String dim = currentDimension();
        List<Waypoint> out = new ArrayList<>();
        for (Waypoint w : waypoints) if (w.enabled && (w.dimension.isEmpty() || w.dimension.equals(dim))) out.add(w);
        return out;
    }

    public static String currentDimension() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return "";
        return mc.world.getRegistryKey().getValue().toString();
    }

    public static String currentServer() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getCurrentServerEntry() != null) return mc.getCurrentServerEntry().address.toLowerCase(Locale.ROOT);
        return mc.isInSingleplayer() ? "singleplayer" : "";
    }

    public Waypoint add(String name, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockPos p = mc.player != null ? mc.player.getBlockPos() : BlockPos.ORIGIN;
        Waypoint w = new Waypoint(name, color, p.getX(), p.getY(), p.getZ(), currentDimension());
        w.server = currentServer();
        waypoints.add(w);
        save();
        return w;
    }

    public Waypoint add(String name, int color, int x, int y, int z) {
        Waypoint w = new Waypoint(name, color, x, y, z, currentDimension());
        w.server = currentServer();
        waypoints.add(w);
        save();
        return w;
    }

    public boolean remove(String idOrName) {
        boolean r = waypoints.removeIf(w -> w.id.equals(idOrName) || w.name.equalsIgnoreCase(idOrName));
        if (r) save();
        return r;
    }

    public void load() {
        waypoints.clear();
        Path f = file();
        if (!Files.exists(f)) return;
        try {
            JsonArray arr = GSON.fromJson(Files.readString(f), JsonArray.class);
            if (arr == null) return;
            for (JsonElement e : arr) {
                JsonObject o = e.getAsJsonObject();
                Waypoint w = new Waypoint(o.get("name").getAsString(), o.get("color").getAsInt(), o.get("x").getAsInt(), o.get("y").getAsInt(), o.get("z").getAsInt(), o.has("dimension") ? o.get("dimension").getAsString() : "");
                if (o.has("id")) w.id = o.get("id").getAsString();
                if (o.has("enabled")) w.enabled = o.get("enabled").getAsBoolean();
                if (o.has("server")) w.server = o.get("server").getAsString();
                waypoints.add(w);
            }
        } catch (Exception ex) {
            ChaosClient.LOGGER.warn("[ChaosClient] Waypoints laden: {}", ex.toString());
        }
    }

    public void save() {
        JsonArray arr = new JsonArray();
        for (Waypoint w : waypoints) {
            JsonObject o = new JsonObject();
            o.addProperty("id", w.id);
            o.addProperty("name", w.name);
            o.addProperty("color", w.color);
            o.addProperty("x", w.x);
            o.addProperty("y", w.y);
            o.addProperty("z", w.z);
            o.addProperty("dimension", w.dimension);
            o.addProperty("enabled", w.enabled);
            o.addProperty("server", w.server);
            arr.add(o);
        }
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), GSON.toJson(arr));
        } catch (IOException e) {
            ChaosClient.LOGGER.warn("[ChaosClient] Waypoints speichern: {}", e.toString());
        }
    }
}
