package com.chaoscraft.client.screenshot;

import com.chaoscraft.client.ChaosClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Screenshot-System: aufnehmen, Historie, Vorschau-Texturen, Datei/Ordner
 * öffnen. Auto-Upload ist bewusst nicht aktiv (nur wenn der Nutzer es in
 * einer späteren Version explizit einschaltet).
 */
public final class ScreenshotManager {

    public record Shot(Path file, long modified, long size) {
        public String name() { return file.getFileName().toString(); }
    }

    private final Map<Path, Identifier> previews = new HashMap<>();
    private final Map<Path, int[]> previewSizes = new HashMap<>();
    private Path lastShot;

    public Path dir() {
        Path p = MinecraftClient.getInstance().runDirectory.toPath().resolve(ScreenshotRecorder.SCREENSHOTS_DIRECTORY);
        try { Files.createDirectories(p); } catch (IOException ignored) {}
        return p;
    }

    public Path lastShot() { return lastShot; }

    /** Nimmt einen Screenshot auf (asynchron gespeichert) und benachrichtigt. */
    public void take() {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.execute(() -> ScreenshotRecorder.saveScreenshot(mc.runDirectory, mc.getFramebuffer(), text -> {
            String msg = text.getString();
            ChaosClient.get().getNotifications().success("Screenshot gespeichert");
            ChaosClient.LOGGER.info("[ChaosClient] {}", msg);
            refreshLast();
        }));
    }

    private void refreshLast() {
        List<Shot> list = history(1);
        if (!list.isEmpty()) lastShot = list.get(0).file();
    }

    public List<Shot> history(int limit) {
        List<Shot> out = new ArrayList<>();
        try (Stream<Path> s = Files.list(dir())) {
            s.filter(p -> p.toString().toLowerCase().endsWith(".png")).forEach(p -> {
                try {
                    out.add(new Shot(p, Files.getLastModifiedTime(p).toMillis(), Files.size(p)));
                } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
        out.sort(Comparator.comparingLong(Shot::modified).reversed());
        return limit > 0 && out.size() > limit ? new ArrayList<>(out.subList(0, limit)) : out;
    }

    public void openFolder() { Util.getOperatingSystem().open(dir()); }
    public void open(Path file) { Util.getOperatingSystem().open(file); }

    public boolean delete(Path file) {
        try {
            releasePreview(file);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            return false;
        }
    }

    /** Vorschau-Textur (lazy, Render-Thread). Liefert null, solange nicht geladen. */
    public Identifier preview(Path file) {
        Identifier id = previews.get(file);
        if (id != null) return id;
        try (InputStream in = Files.newInputStream(file)) {
            NativeImage img = NativeImage.read(in);
            previewSizes.put(file, new int[]{img.getWidth(), img.getHeight()});
            Identifier tex = Identifier.of("chaosclient", "screenshot/" + Integer.toHexString(file.toString().hashCode()));
            MinecraftClient.getInstance().getTextureManager().registerTexture(tex, new NativeImageBackedTexture(() -> "chaos_screenshot", img));
            previews.put(file, tex);
            return tex;
        } catch (Exception e) {
            return null;
        }
    }

    public int[] previewSize(Path file) { return previewSizes.getOrDefault(file, new int[]{16, 9}); }

    public void releasePreview(Path file) {
        Identifier id = previews.remove(file);
        previewSizes.remove(file);
        if (id != null) MinecraftClient.getInstance().getTextureManager().destroyTexture(id);
    }

    public void releaseAll() {
        for (Identifier id : previews.values()) MinecraftClient.getInstance().getTextureManager().destroyTexture(id);
        previews.clear();
        previewSizes.clear();
    }
}
