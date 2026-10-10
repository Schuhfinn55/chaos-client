package com.chaoscraft.client.modules.general;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.settings.BooleanSetting;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.toast.Toast;

/** No Toasts: Fortschritts- und Rezept-Popups oben rechts ausblenden. */
public class NoToastsModule extends Module {

    private final BooleanSetting advancements = add(new BooleanSetting("Fortschritte", "„Fortschritt erreicht“-Popups ausblenden.", true));
    private final BooleanSetting recipes = add(new BooleanSetting("Rezepte", "„Neue Rezepte freigeschaltet“-Popups ausblenden.", true));

    public NoToastsModule() {
        super("No Toasts", "Fortschritts- und Rezept-Popups ausblenden.", Category.GENERAL, "▭");
        tags("toast", "popup", "fortschritt", "advancement", "rezept");
    }

    public boolean shouldHide(Toast t) {
        return (advancements.isEnabled() && t instanceof AdvancementToast) || (recipes.isEnabled() && t instanceof RecipeToast);
    }
}
