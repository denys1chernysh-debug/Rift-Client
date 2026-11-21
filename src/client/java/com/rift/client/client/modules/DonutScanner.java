package com.rift.client.client.modules;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client-side world scanner. It only examines chunks already present in the
 * local ClientLevel; it cannot reveal unloaded or server-hidden data.
 */
public final class DonutScanner {
    public enum Scan { SUSPICIOUS, BASES, AMETHYST, DEBRIS }

    private static final Map<Scan, ScanState> STATES = new EnumMap<>(Scan.class);
    private static ClientLevel lastLevel;

    static {
        for (Scan scan : Scan.values()) STATES.put(scan, new ScanState());
    }

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
            reportIfReady(mc, scan, progress, total);
            return;
        }

        if (!level.hasChunk(cx, cz)) return;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = level.getMinY();
        int maxY = level.getMaxY();
        int storage = 0, utility = 0, amethyst = 0, debris = 0;
        int firstAmethystY = Integer.MIN_VALUE;
        int firstDebrisY = Integer.MIN_VALUE;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = (cx << 4) + x;
                int wz = (cz << 4) + z;
                for (int y = minY; y < maxY; y++) {
                    pos.set(wx, y, wz);
                    BlockState state = level.getBlockState(pos);

                    if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST)
                        || state.is(Blocks.BARREL) || state.is(Blocks.ENDER_CHEST)
                        || state.is(Blocks.SHULKER_BOX)) {
                        storage++;
                    }
                    if (state.is(Blocks.CRAFTING_TABLE) || state.is(Blocks.FURNACE)
                        || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER)
                        || state.is(Blocks.BED) || state.is(Blocks.BREWING_STAND)) {
                        utility++;
                    }
                    if (state.is(Blocks.AMETHYST_BLOCK) || state.is(Blocks.BUDDING_AMETHYST)
                        || state.is(Blocks.AMETHYST_CLUSTER) || state.is(Blocks.LARGE_AMETHYST_BUD)
                        || state.is(Blocks.MEDIUM_AMETHYST_BUD) || state.is(Blocks.SMALL_AMETHYST_BUD)) {
                        amethyst++;
                        if (firstAmethystY == Integer.MIN_VALUE) firstAmethystY = y;
                    }
                    if (state.is(Blocks.ANCIENT_DEBRIS)) {
                        debris++;
                        if (firstDebrisY == Integer.MIN_VALUE) firstDebrisY = y;
                    }
                }
            }
        }

        progress.scanned.add(key);
        progress.scannedChunks++;

        int score = switch (scan) {
            case SUSPICIOUS -> storage * 3 + (utility >= 12 ? utility : 0);
            case BASES -> storage * 4 + utility;
            case AMETHYST -> amethyst;
            case DEBRIS -> debris;
        };
        boolean match = switch (scan) {
            case SUSPICIOUS -> storage >= 3 || (utility >= 12 && storage >= 1);
            case BASES -> storage >= 2 || utility >= 10;
            case AMETHYST -> amethyst >= 4;
            case DEBRIS -> debris > 0;
        };

        if (match) {
            int y = scan == Scan.AMETHYST ? firstAmethystY
                : scan == Scan.DEBRIS ? firstDebrisY : Integer.MIN_VALUE;
            progress.matches++;
            progress.candidates.add(new Candidate(cx, cz, score, y));
            if (progress.candidates.size() > 12) progress.candidates.remove(0);
            mc.player.displayClientMessage(candidateMessage(scan, cx, cz, score, y), true);
            progress.reportCooldown = 80;
        } else {
            reportIfReady(mc, scan, progress, total);
        }
    }

    private static void reportIfReady(Minecraft mc, Scan scan, ScanState state, int total) {
        if (state.reportCooldown > 0 || mc.player == null) return;
        if (!state.candidates.isEmpty()) {
            Candidate best = state.candidates.get(0);
            double bestDistance = Double.MAX_VALUE;
            for (Candidate candidate : state.candidates) {
                double x = (candidate.chunkX << 4) + 8.0 - mc.player.getX();
                double z = (candidate.chunkZ << 4) + 8.0 - mc.player.getZ();
                double distance = x * x + z * z;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = candidate;
                }
            }
            mc.player.displayClientMessage(candidateMessage(
                scan, best.chunkX, best.chunkZ, best.score, best.y), true);
        } else {
            mc.player.displayClientMessage(Component.literal(
                "RIFT " + scan.name() + ": " + state.scannedChunks + "/" + total
                    + " loaded chunks checked; no matches yet"), true);
        }
        state.reportCooldown = 80;
    }

    private static Component candidateMessage(Scan scan, int chunkX, int chunkZ, int score, int y) {
        String label = switch (scan) {
            case SUSPICIOUS -> "Suspicious chunk";
            case BASES -> "Possible base";
            case AMETHYST -> "Amethyst signal";
            case DEBRIS -> "Ancient debris";
        };
        String location = "chunk " + chunkX + ", " + chunkZ
            + " (block " + (chunkX << 4) + ", " + (chunkZ << 4) + ")";
        String height = y == Integer.MIN_VALUE ? "" : " | approx Y " + y;
        return Component.literal("RIFT " + label + ": " + location + " | score " + score + height);
    }

    private static final class ScanState {
        final Set<Long> scanned = new HashSet<>();
        final List<Candidate> candidates = new ArrayList<>();
        int cursor;
        int reportCooldown;
        int matches;
        int scannedChunks;

        void reset() {
            scanned.clear();
            candidates.clear();
            cursor = 0;
            reportCooldown = 0;
            matches = 0;
            scannedChunks = 0;
        }
    }

    private record Candidate(int chunkX, int chunkZ, int score, int y) {}
}
