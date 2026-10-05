package com.chaoscraft.client.music;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.config.SharedData;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.OggAudioStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Ingame-Music-Player: spielt MP3 (JLayer), WAV (javax.sound) und OGG
 * (Minecraft-Decoder) aus dem Chaos-Musikordner (Launcher-Export) oder
 * {@code <gamedir>/chaos-client/music}.
 *
 * Lautstärke wird in Software auf die PCM-Daten angewendet (16-bit, little
 * endian) – funktioniert damit unabhängig davon, ob die Audio-Leitung einen
 * MASTER_GAIN-Regler anbietet.
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
    private volatile Consumer<Float> volumeListener;

    public List<Track> playlist() { return playlist; }
    public Track current() { return index >= 0 && index < playlist.size() ? playlist.get(index) : null; }
    public boolean isPlaying() { return playing && !paused; }
    public boolean isPaused() { return paused; }
    public float volume() { return volume; }
    public boolean repeat() { return repeat; }
    public boolean shuffle() { return shuffle; }
    public void setRepeat(boolean r) { repeat = r; }
    public void setShuffle(boolean s) { shuffle = s; }
    /** Wird bei jeder Lautstärkeänderung aufgerufen (z.B. zum Speichern in der Modul-Einstellung). */
    public void setVolumeListener(Consumer<Float> l) { volumeListener = l; }

    public List<Path> dirs() {
        List<Path> out = new ArrayList<>();
        String launcherDir = SharedData.get().musicDir;
        if (!launcherDir.isEmpty()) out.add(Path.of(launcherDir));
        Path local = MinecraftClient.getInstance().runDirectory.toPath().resolve("chaos-client").resolve("music");
        try { Files.createDirectories(local); } catch (IOException ignored) {}
        out.add(local);
        return out;
    }

    public static boolean isSupported(String fileName) {
        String n = fileName.toLowerCase(Locale.ROOT);
        return n.endsWith(".mp3") || n.endsWith(".wav") || n.endsWith(".ogg");
    }

    public void scan() {
        playlist.clear();
        for (Path dir : dirs()) {
            if (!Files.isDirectory(dir)) continue;
            try (Stream<Path> s = Files.list(dir)) {
                s.filter(p -> isSupported(p.toString())).sorted().forEach(p -> {
                    String n = p.getFileName().toString();
                    playlist.add(new Track(p, n.substring(0, n.lastIndexOf('.'))));
                });
            } catch (IOException ignored) {}
        }
        if (index >= playlist.size()) index = -1;
    }

    /** Lautstärke 0..1 setzen (ohne Listener-Aufruf, z.B. beim Laden der Einstellung). */
    public void setVolumeSilently(float v) { volume = Math.max(0f, Math.min(1f, v)); }

    public void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        Consumer<Float> l = volumeListener;
        if (l != null) l.accept(volume);
    }

    /** Lautstärke um Schritt ändern (z.B. ±0.05) und kurz anzeigen. */
    public void adjustVolume(float delta) {
        setVolume(volume + delta);
        ChaosClient.get().getNotifications().info("🔊 Lautstärke " + Math.round(volume * 100) + "%");
    }

    public void play(int i) {
        if (playlist.isEmpty()) { scan(); if (playlist.isEmpty()) { ChaosClient.get().getNotifications().warn("Keine Musik gefunden (MP3/WAV/OGG in chaos-client/music oder im Launcher-Musikordner)."); return; } }
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
            else if (name.endsWith(".mp3")) streamMp3(t.file());
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
                    write(buf, n);
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
                write(buf, buf.length);
            }
            drainAndClose();
        }
    }

    /** MP3 über JLayer dekodieren (Frame für Frame → 16-bit PCM). */
    private void streamMp3(Path file) throws Exception {
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file), 64 * 1024)) {
            Bitstream bs = new Bitstream(in);
            Decoder dec = new Decoder();
            byte[] out = new byte[4608 * 2];
            try {
                while (playing) {
                    waitWhilePaused();
                    Header h = bs.readFrame();
                    if (h == null) break;
                    SampleBuffer sb = (SampleBuffer) dec.decodeFrame(h, bs);
                    if (line == null) {
                        AudioFormat fmt = new AudioFormat(dec.getOutputFrequency(), 16, dec.getOutputChannels(), true, false);
                        openLine(fmt);
                    }
                    short[] pcm = sb.getBuffer();
                    int len = sb.getBufferLength();
                    if (out.length < len * 2) out = new byte[len * 2];
                    for (int i = 0; i < len; i++) {
                        out[i * 2] = (byte) (pcm[i] & 0xFF);
                        out[i * 2 + 1] = (byte) ((pcm[i] >> 8) & 0xFF);
                    }
                    write(out, len * 2);
                    bs.closeFrame();
                }
            } finally {
                try { bs.close(); } catch (Exception ignored) {}
            }
            drainAndClose();
        }
    }

    private void openLine(AudioFormat fmt) throws Exception {
        SourceDataLine l = AudioSystem.getSourceDataLine(fmt);
        l.open(fmt);
        l.start();
        line = l;
    }

    /** Schreibt 16-bit-LE-PCM mit Software-Lautstärke auf die Leitung. */
    private void write(byte[] buf, int n) {
        SourceDataLine l = line;
        if (l == null) return;
        float v = volume;
        if (v < 0.999f) {
            float g = v * v; // wahrnehmungsnäher als linear
            for (int i = 0; i + 1 < n; i += 2) {
                int s = (short) ((buf[i] & 0xFF) | (buf[i + 1] << 8));
                s = Math.round(s * g);
                buf[i] = (byte) (s & 0xFF);
                buf[i + 1] = (byte) ((s >> 8) & 0xFF);
            }
        }
        l.write(buf, 0, n);
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
