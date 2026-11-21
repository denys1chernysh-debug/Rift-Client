package com.rift.client.client.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    private ModuleManager() {}

    public static void register(Module module) {
        if (getModule(module.getName()) == null) MODULES.add(module);
    }

    public static List<Module> getModules() {
        return Collections.unmodifiableList(MODULES);
    }

    public static List<Module> getModulesByCategory(String category) {
        List<Module> result = new ArrayList<>();
        for (Module module : MODULES) {
            if (module.getCategory().equalsIgnoreCase(category)) result.add(module);
        }
        return result;
    }

    public static List<Module> getFavorites() {
        List<Module> result = new ArrayList<>();
        for (Module module : MODULES) if (module.isPinned()) result.add(module);
        return result;
    }

    public static Module getModule(String name) {
        for (Module module : MODULES) {
            if (module.getName().equalsIgnoreCase(name)) return module;
        }
        return null;
    }

    public static void tick() {
        for (Module module : MODULES) if (module.isEnabled()) module.onTick();
    }
}
