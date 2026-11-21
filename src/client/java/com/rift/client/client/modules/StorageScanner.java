package com.rift.client.client.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Scans only chunks already loaded by the client. This is a nearby storage
 * locator, not a through-wall renderer and not a way to reveal server-hidden blocks.
 */
public final class StorageScanner {
    private static final int RADIUS = 2;
    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final List<String> FOUND = new ArrayList<>();
    private static ClientLevel lastLevel;
    private static int cursor;
    private static int ticks;
    private static int totalFound;

    private StorageScanner() {}

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            reset(null);
            return;
        }
        if (level != lastLevel) reset(level);
        if (++ticks < SCAN_INTERVAL_TICKS) return;
        ticks = 0;

        int diameter = RADIUS * 2 + 1;
        int offsetX = cursor % diameter - RADIUS;
        int offsetZ = cursor / diameter - RADIUS;
        cursor = (cursor + 1) % (diameter * diameter);

        int centerChunkX = mc.player.getBlockX() >> 4;
        int centerChunkZ = mc.player.getBlockZ() >> 4;
        int chunkX = centerChunkX + offsetX;
        int chunkZ = centerChunkZ + offsetZ;
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int sampleY = mc.player.getBlockY();

        if (!level.hasChunkAt(new BlockPos(baseX, sampleY, baseZ))) return;

        for (int y = level.getMinY(); y < level.getMaxY(); y++) {
            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz < 16; dz++) {
                    BlockPos pos = new BlockPos(baseX + dx, y, baseZ + dz);
                    BlockState state = level.getBlockState(pos);
                    if (!isStorage(state)) continue;
                    totalFound++;
                    if (FOUND.size() < 16) {
                        FOUND.add(label(state) + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
                    }
                }
            }
        }
    }

    private static boolean isStorage(BlockState state) {
        return state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST)
            || state.is(Blocks.BARREL) || state.is(Blocks.ENDER_CHEST)
            || state.is(Blocks.HOPPER) || state.is(Blocks.FURNACE)
            || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER)
            || state.is(Blocks.SHULKER_BOX);
    }

    private static String label(BlockState state) {
        if (state.is(Blocks.CHEST)) return "Chest";
        if (state.is(Blocks.TRAPPED_CHEST)) return "Trapped Chest";
        if (state.is(Blocks.BARREL)) return "Barrel";
        if (state.is(Blocks.ENDER_CHEST)) return "Ender Chest";
        if (state.is(Blocks.HOPPER)) return "Hopper";
        if (state.is(Blocks.FURNACE)) return "Furnace";
        if (state.is(Blocks.BLAST_FURNACE)) return "Blast Furnace";
        if (state.is(Blocks.SMOKER)) return "Smoker";
        return "Shulker Box";
    }

    private static void reset(ClientLevel level) {
        lastLevel = level;
        cursor = 0;
        ticks = 0;
        totalFound = 0;
        FOUND.clear();
    }

    public static int getTotalFound() {
        return totalFound;
    }

    public static List<String> getFound() {
        return List.copyOf(FOUND);
    }

    public static String getSummary() {
        return "STORAGE SCAN  " + totalFound + " FOUND  |  " + FOUND.size() + " LOCATIONS LISTED";
    }
}
