package com.chaoscraft.client.modules.general;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.mixin.FogRendererAccessor;

/** No Fog: Nebel komplett abschalten – weite Sicht, auch im Nether und unter Wasser. */
public class NoFogModule extends Module {

    public NoFogModule() {
        super("No Fog", "Nebel komplett abschalten – klare Sicht bis zur Renderdistanz.", Category.GENERAL, "☁");
        tags("nebel", "fog", "sicht", "weit");
    }

    @Override public void onEnable() { try { FogRendererAccessor.chaos$setFogEnabled(false); } catch (Throwable ignored) {} }
    @Override public void onDisable() { try { FogRendererAccessor.chaos$setFogEnabled(true); } catch (Throwable ignored) {} }

    @Override
    public void onTick() {
        // Falls der Debug-Toggle (F3+F) dazwischenfunkt: Zustand erzwingen
        try { if (FogRendererAccessor.chaos$isFogEnabled()) FogRendererAccessor.chaos$setFogEnabled(false); } catch (Throwable ignored) {}
    }
}
