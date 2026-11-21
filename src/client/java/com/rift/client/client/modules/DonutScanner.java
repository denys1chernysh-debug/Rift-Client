package com.rift.client.client.modules;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Conservative client-side world scanner. It only examines chunks already
 * present in the local ClientLevel; it cannot reveal unloaded or server-hidden data.
 */
public final class DonutScanner {
    public enum Scan { SUSPICIOUS, BASES, AMETHYST, DEBRIS }
    private static final Set<Long> scanned = new HashSet<>();
    private static ClientLevel lastLevel;
    private static int cursor;
    private static int reportCooldown;
    private static int matches;
    private static int scannedChunks;
    private DonutScanner() {}

    public static void tick(Scan scan) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        if (lastLevel != level) {
            lastLevel = level;
            scanned.clear();
            cursor = 0;
            matches = 0;
            scannedChunks = 0;
        }
        if (reportCooldown > 0) reportCooldown--;
        int radius = scan == Scan.DEBRIS ? 3 : 5;
        int diameter = radius * 2 + 1;
        int total = diameter * diameter;
        int index = cursor++ % total;
        int cx = (mc.player.getBlockX() >> 4) + index % diameter - radius;
        int cz = (mc.player.getBlockZ() >> 4) + index / diameter - radius;
        long key = (((long) cx) << 32) ^ (cz & 0xffffffffL);
        if (scanned.contains(key)) {
            if (scanned.size() >= total) {
                if (reportCooldown == 0) {
                    mc.player.displayClientMessage(Component.literal("RIFT " + scan.name()
                        + ": " + matches + " signals in " + scannedChunks + " loaded chunks"), true);
                    reportCooldown = 80;
                }
            }
            return;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = level.getMinY();
        int maxY = level.getMaxY();
        int storage = 0, utility = 0, amethyst = 0, debris = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = (cx << 4) + x, wz = (cz << 4) + z;
                for (int y = minY; y < maxY; y++) {
                    pos.set(wx, y, wz);
                    if (!level.hasChunkAt(pos)) continue;
                    BlockState state = level.getBlockState(pos);
                    if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST)
                        || state.is(Blocks.BARREL) || state.is(Blocks.ENDER_CHEST)
                        || state.is(Blocks.SHULKER_BOX)) storage++;
                    if (state.is(Blocks.CRAFTING_TABLE) || state.is(Blocks.FURNACE)
                        || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER)
                        || state.is(Blocks.BED) || state.is(Blocks.BREWING_STAND)) utility++;
                    if (state.is(Blocks.AMETHYST_BLOCK) || state.is(Blocks.BUDDING_AMETHYST)
                        || state.is(Blocks.AMETHYST_CLUSTER) || state.is(Blocks.LARGE_AMETHYST_BUD)
                        || state.is(Blocks.MEDIUM_AMETHYST_BUD) || state.is(Blocks.SMALL_AMETHYST_BUD)) amethyst++;
                    if (state.is(Blocks.ANCIENT_DEBRIS)) debris++;
                }
            }
        }
        scanned.add(key);
        scannedChunks++;
        boolean match = switch (scan) {
            case SUSPICIOUS -> storage >= 3 || (utility >= 12 && storage >= 1);
            case BASES -> storage >= 2 || utility >= 10;
            case AMETHYST -> amethyst >= 4;
            case DEBRIS -> debris > 0;
        };
        if (match) matches++;
        if (reportCooldown == 0) {
            mc.player.displayClientMessage(Component.literal("RIFT " + scan.name()
                + ": " + matches + " signals / " + scannedChunks + " loaded chunks"), true);
            reportCooldown = 80;
        }
    }
}
