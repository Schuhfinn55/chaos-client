package com.chaoscraft.client.modules.general;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.IntSetting;
import net.minecraft.client.particle.Particle;

import java.util.Random;

/** Particles: Partikelmenge begrenzen (Performance) – Chaos-Effekte bleiben erhalten. */
public class ParticlesModule extends Module {

    private final IntSetting amount = add(new IntSetting("Menge", "Prozent der Partikel, die angezeigt werden.", 50, 0, 100));

    private final Random random = new Random();

    public ParticlesModule() {
        super("Particles", "Weniger Partikel – mehr FPS bei großen Farmen und Kämpfen.", Category.GENERAL, "⁂");
        tags("partikel", "particles", "fps", "performance");
    }

    public boolean shouldDrop(Particle p) {
        int pct = amount.getInt();
        if (pct >= 100) return false;
        if (pct <= 0) return true;
        return random.nextInt(100) >= pct;
    }
}
