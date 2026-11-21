package com.rift.client.client.modules;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
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
    private static final Map<Scan, ScanState> STATES = new EnumMap<>(Scan.class);
    private static ClientLevel lastLevel;
    static { for (Scan scan : Scan.values()) STATES.put(scan, new ScanState()); }
    private DonutScanner() {}

    public static void tick(Scan scan) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        if (lastLevel != level) {
            lastLevel = level;
            for (ScanState state : STATES.values()) state.reset();
        }
        ScanState progress = STATES.get(scan);
        if (progress.reportCooldown > 0) progress.reportCooldown--;
        int radius = scan == Scan.DEBRIS ? 3 : 5;
        int diameter = radius * 2 + 1;
        int total = diameter * diameter;
        int index = progress.cursor++ % total;
        int cx = (mc.player.getBlockX() >> 4) + index % diameter - radius;
        int cz = (mc.player.getBlockZ() >> 4) + index / diameter - radius;
        long key = (((long) cx) << 32) ^ (cz & 0xffffffffL);
        if (progress.scanned.contains(key)) {
            if (progress.scanned.size() >= total && progress.reportCooldown == 0) {
                mc.player.displayClientMessage(Component.literal("RIFT " + scan.name()
                    + ": " + progress.matches + " signals in " + progress.scannedChunks + " loaded chunks"), true);
                progress.reportCooldown = 80;
            }
            return;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        if (!level.hasChunk(cx, cz)) return;
        int minY = level.getMinY();
        int maxY = level.getMaxY();
        int storage = 0, utility = 0, amethyst = 0, debris = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = (cx << 4) + x, wz = (cz << 4) + z;
                for (int y = minY; y < maxY; y++) {
                    pos.set(wx, y, wz);
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
        progress.scanned.add(key);
        progress.scannedChunks++;
        boolean match = switch (scan) {
            case SUSPICIOUS -> storage >= 3 || (utility >= 12 && storage >= 1);
            case BASES -> storage >= 2 || utility >= 10;
            case AMETHYST -> amethyst >= 4;
            case DEBRIS -> debris > 0;
        };
        if (match) progress.matches++;
        if (progress.reportCooldown == 0) {
            mc.player.displayClientMessage(Component.literal("RIFT " + scan.name()
                + ": " + progress.matches + " signals / " + progress.scannedChunks + " loaded chunks"), true);
            progress.reportCooldown = 80;
        }
    }

    private static final class ScanState {
        final Set<Long> scanned = new HashSet<>();
        int cursor;
        int reportCooldown;
        int matches;
        int scannedChunks;
        void reset() { scanned.clear(); cursor = 0; reportCooldown = 0; matches = 0; scannedChunks = 0; }
    }
}
