package com.onyx.visuals.module;

import com.onyx.visuals.module.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        // render
        modules.add(new FullbrightModule());
        modules.add(new BlockOverlayModule());
        modules.add(new ZoomModule());
        modules.add(new CrosshairModule());
        // hud
        modules.add(new KeystrokesModule());
        modules.add(new CPSModule());
        modules.add(new ArmorStatusModule());
        modules.add(new FpsPingModule());
        // movement
        modules.add(new ToggleSprintModule());
        modules.add(new ToggleSneakModule());
        // utility
        modules.add(new AutoGGModule());
        modules.add(new TimeChangerModule());
        // bot
        modules.add(new AutoFarmModule());
    }

    public void register(Module module) { modules.add(module); }
    public List<Module> getModules() { return modules; }

    public List<Module> getByCategory(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).collect(Collectors.toList());
    }

    public Module getByName(String name) {
        return modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void tickAll() {
        for (Module module : modules) if (module.isEnabled()) module.onTick();
    }
}
