package com.chaoscraft.client.music;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.config.SharedData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.OggAudioStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Ingame-Music-Player: spielt WAV (javax.sound) und OGG (Minecraft-Decoder)
 * aus dem Chaos-Musikordner (Launcher-Export) oder {@code <gamedir>/chaos-client/music}.
 * MP3 wird vom JDK nicht dekodiert und daher nicht unterstützt.
 */
public final class MusicPlayer {

    public record Track(Path file, String title) {}

    private final List<Track> playlist = new ArrayList<>();
    private int index = -1;
    private volatile boolean playing;
    private volatile boolean paused;
    private volatile float volume = 0.5f;
    private volatile boolean repeat;
    private volatile boolean shuffle;
    private Thread thread;
    private volatile SourceDataLine line;

    public List<Track> playlist() { return playlist; }
    public Track current() { return index >= 0 && index < playlist.size() ? playlist.get(index) : null; }
    public boolean isPlaying() { return playing && !paused; }
    public boolean isPaused() { return paused; }
    public float volume() { return volume; }
    public boolean repeat() { return repeat; }
    public boolean shuffle() { return shuffle; }
    public void setRepeat(boolean r) { repeat = r; }
    public void setShuffle(boolean s) { shuffle = s; }

    public List<Path> dirs() {
        List<Path> out = new ArrayList<>();
        String launcherDir = SharedData.get().musicDir;
        if (!launcherDir.isEmpty()) out.add(Path.of(launcherDir));
        Path local = MinecraftClient.getInstance().runDirectory.toPath().resolve("chaos-client").resolve("music");
        try { Files.createDirectories(local); } catch (IOException ignored) {}
        out.add(local);
        return out;
    }

    public void scan() {
        playlist.clear();
        for (Path dir : dirs()) {
            if (!Files.isDirectory(dir)) continue;
            try (Stream<Path> s = Files.list(dir)) {
                s.filter(p -> {
                    String n = p.toString().toLowerCase(Locale.ROOT);
                    return n.endsWith(".wav") || n.endsWith(".ogg");
                }).sorted().forEach(p -> {
                    String n = p.getFileName().toString();
                    playlist.add(new Track(p, n.substring(0, n.lastIndexOf('.'))));
                });
            } catch (IOException ignored) {}
        }
        if (index >= playlist.size()) index = -1;
    }

    public void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        applyVolume();
    }

    private void applyVolume() {
        SourceDataLine l = line;
        if (l != null && l.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl c = (FloatControl) l.getControl(FloatControl.Type.MASTER_GAIN);
            float db = volume <= 0.001f ? c.getMinimum() : (float) (20 * Math.log10(volume));
            c.setValue(Math.max(c.getMinimum(), Math.min(c.getMaximum(), db)));
        }
    }

    public void play(int i) {
        if (playlist.isEmpty()) { scan(); if (playlist.isEmpty()) { ChaosClient.get().getNotifications().warn("Keine Musik gefunden (WAV/OGG in chaos-client/music)."); return; } }
        stop();
        index = Math.floorMod(i, playlist.size());
        Track t = playlist.get(index);
        playing = true;
        paused = false;
        thread = new Thread(() -> stream(t), "ChaosMusic");
        thread.setDaemon(true);
        thread.start();
        ChaosClient.get().getNotifications().info("♫ " + t.title());
    }

    public void togglePlay() {
        if (!playing) { play(index < 0 ? 0 : index); return; }
        paused = !paused;
    }

    public void next() { if (playlist.isEmpty()) return; play(shuffle ? (int) (Math.random() * playlist.size()) : index + 1); }
    public void previous() { if (playlist.isEmpty()) return; play(index - 1); }

    public void stop() {
        playing = false;
        paused = false;
        Thread t = thread;
        if (t != null) t.interrupt();
        SourceDataLine l = line;
        if (l != null) { try { l.stop(); l.close(); } catch (Exception ignored) {} }
        line = null;
    }

    private void stream(Track t) {
        String name = t.file().toString().toLowerCase(Locale.ROOT);
        try {
            if (name.endsWith(".ogg")) streamOgg(t.file());
            else streamWav(t.file());
            if (playing && !Thread.currentThread().isInterrupted()) {
                playing = false;
                MinecraftClient.getInstance().execute(() -> { if (repeat || index + 1 < playlist.size() || shuffle) next(); });
            }
        } catch (Exception e) {
            if (!(e instanceof InterruptedException)) {
                ChaosClient.LOGGER.warn("[ChaosClient] Musik {}: {}", t.title(), e.toString());
                ChaosClient.get().getNotifications().error("Titel konnte nicht abgespielt werden: " + t.title());
            }
            playing = false;
        }
    }

    private void streamWav(Path file) throws Exception {
        try (AudioInputStream in = AudioSystem.getAudioInputStream(file.toFile())) {
            AudioFormat base = in.getFormat();
            AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, base.getSampleRate(), 16, base.getChannels(), base.getChannels() * 2, base.getSampleRate(), false);
            try (AudioInputStream dec = AudioSystem.getAudioInputStream(pcm, in)) {
                openLine(pcm);
                byte[] buf = new byte[8192];
                int n;
                while (playing && (n = dec.read(buf)) > 0) {
                    waitWhilePaused();
                    line.write(buf, 0, n);
                }
                drainAndClose();
            }
        }
    }

    private void streamOgg(Path file) throws Exception {
        try (OggAudioStream ogg = new OggAudioStream(Files.newInputStream(file))) {
            AudioFormat fmt = ogg.getFormat();
            openLine(fmt);
            while (playing) {
                waitWhilePaused();
                ByteBuffer bb = ogg.read(8192);
                if (bb == null || bb.remaining() == 0) break;
                byte[] buf = new byte[bb.remaining()];
                bb.get(buf);
                line.write(buf, 0, buf.length);
            }
            drainAndClose();
        }
    }

    private void openLine(AudioFormat fmt) throws Exception {
        SourceDataLine l = AudioSystem.getSourceDataLine(fmt);
        l.open(fmt);
        l.start();
        line = l;
        applyVolume();
    }

    private void waitWhilePaused() throws InterruptedException {
        while (paused && playing) Thread.sleep(50);
        if (!playing) throw new InterruptedException();
    }

    private void drainAndClose() {
        SourceDataLine l = line;
        if (l != null) { try { l.drain(); l.stop(); l.close(); } catch (Exception ignored) {} }
        line = null;
    }
}
