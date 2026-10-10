package com.chaoscraft.client.modules.player;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;

/** No Hurt Cam: kein Kamerawackeln bei Schaden (HurtCamMixin). */
public class NoHurtCamModule extends Module {
    public NoHurtCamModule() {
        super("No Hurt Cam", "Kein Kamerawackeln, wenn du Schaden bekommst.", Category.PLAYER, "☄");
        tags("hurt", "kamera", "wackeln", "schaden");
    }
}
