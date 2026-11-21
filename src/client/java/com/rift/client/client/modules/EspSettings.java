package com.rift.client.client.modules;

import java.util.HashMap;
import java.util.Map;

/** User-configurable options for the three world ESP modules. */
public final class EspSettings {
    public static final int MIN_RANGE = 16;
    public static final int MAX_RANGE = 256;
    private static final Map<String, Integer> RANGES = new HashMap<>();
    private static boolean playerNames = true;
    private static boolean playerDistance = true;
    private static boolean storageChests = true;
    private static boolean storageBarrels = true;
    private static boolean storageShulkers = true;
    private static boolean storageOther = true;
    private static boolean blockObsidian = true;
    private static boolean blockBeacons = true;
    private static boolean blockPistons = true;
    private static boolean blockOther = true;
    private static boolean throughWalls = true;

    private EspSettings() {}

    public static int getRange(String module) {
        return RANGES.getOrDefault(module, 256);
    }

    public static void setRange(String module, int value) {
        RANGES.put(module, Math.max(MIN_RANGE, Math.min(MAX_RANGE, value)));
    }

    public static boolean isThroughWalls() { return throughWalls; }
    public static void setThroughWalls(boolean value) { throughWalls = value; }
    public static boolean showPlayerNames() { return playerNames; }
    public static void setShowPlayerNames(boolean value) { playerNames = value; }
    public static boolean showPlayerDistance() { return playerDistance; }
    public static void setShowPlayerDistance(boolean value) { playerDistance = value; }

    public static boolean showStorageChests() { return storageChests; }
    public static void setShowStorageChests(boolean value) { storageChests = value; }
    public static boolean showStorageBarrels() { return storageBarrels; }
    public static void setShowStorageBarrels(boolean value) { storageBarrels = value; }
    public static boolean showStorageShulkers() { return storageShulkers; }
    public static void setShowStorageShulkers(boolean value) { storageShulkers = value; }
    public static boolean showStorageOther() { return storageOther; }
    public static void setShowStorageOther(boolean value) { storageOther = value; }

    public static boolean showBlockObsidian() { return blockObsidian; }
    public static void setShowBlockObsidian(boolean value) { blockObsidian = value; }
    public static boolean showBlockBeacons() { return blockBeacons; }
    public static void setShowBlockBeacons(boolean value) { blockBeacons = value; }
    public static boolean showBlockPistons() { return blockPistons; }
    public static void setShowBlockPistons(boolean value) { blockPistons = value; }
    public static boolean showBlockOther() { return blockOther; }
    public static void setShowBlockOther(boolean value) { blockOther = value; }
}
