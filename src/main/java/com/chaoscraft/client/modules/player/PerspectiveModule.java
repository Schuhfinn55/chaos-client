package com.chaoscraft.client.modules.player;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.EnumSetting;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;

/**
 * Perspective: eigene Taste (Standard V) wechselt zwischen First Person,
 * Third Person und Front. Umschalten über die Keybind-Logik (Toggle = Wechsel).
 */
public class PerspectiveModule extends Module {

    public enum Cycle { ALL, FIRST_THIRD, FIRST_FRONT }

    private final EnumSetting<Cycle> cycle = add(new EnumSetting<>("Reihenfolge", "Welche Perspektiven durchlaufen werden.", Cycle.ALL));

    public PerspectiveModule() {
        super("Perspective", "Taste zum Wechseln der Kameraperspektive (First/Third/Front).", Category.PLAYER, "👁");
        getKeybindSetting().set(GLFW.GLFW_KEY_V);
        tags("kamera", "camera", "third person", "f5");
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Das Modul ist ein Aktions-Modul: Taste → Perspektive wechseln, Zustand bleibt "an".
        if (!enabled && isEnabled()) { cyclePerspective(); return; }
        super.setEnabled(true);
    }

    public void cyclePerspective() {
        if (mc.options == null) return;
        Perspective p = mc.options.getPerspective();
        Perspective next = switch (cycle.get()) {
            case FIRST_THIRD -> p.isFirstPerson() ? Perspective.THIRD_PERSON_BACK : Perspective.FIRST_PERSON;
            case FIRST_FRONT -> p.isFirstPerson() ? Perspective.THIRD_PERSON_FRONT : Perspective.FIRST_PERSON;
            default -> p == Perspective.FIRST_PERSON ? Perspective.THIRD_PERSON_BACK : p == Perspective.THIRD_PERSON_BACK ? Perspective.THIRD_PERSON_FRONT : Perspective.FIRST_PERSON;
        };
        mc.options.setPerspective(next);
    }
}
