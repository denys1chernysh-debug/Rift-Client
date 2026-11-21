package com.rift.client.client.modules;

public abstract class Module {
    private final String name;
    private final String description;
    private final String category;
    private final boolean implemented;
    private boolean enabled;
    private boolean pinned;

    public Module(String name, String description, String category) {
        this(name, description, category, true);
    }

    public Module(String name, String description, String category, boolean implemented) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.implemented = implemented;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public boolean isImplemented() { return implemented; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public void toggle() {
        if (implemented) setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (!implemented || this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}
