package com.rift.client.client.modules;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.OptionalDouble;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Client-side ESP for entities and block states already sent to the client.
 * Scanning is incremental and never requests chunks from the server.
 */
public final class EspRenderer {
    private static final int MAX_MARKERS = 500;
    private static final RenderPipeline THROUGH_WALLS = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("rift-client", "pipeline/esp_through_walls"))
            .withDepthStencilState(Optional.empty())
            .build()
    );
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final org.joml.Matrix4f TEXTURE_MATRIX = new org.joml.Matrix4f();
    private static final StagedVertexBuffer BUFFER =
        new StagedVertexBuffer(() -> "RIFT ESP Buffer", RenderType.SMALL_BUFFER_SIZE);

    private static final List<Marker> SCANNED = new ArrayList<>();
    private static final Set<Long> SCANNED_KEYS = new HashSet<>();
    private static final List<Section> SECTIONS = new ArrayList<>();
    private static List<Marker> frameMarkers = List.of();
    private static ClientLevel lastLevel;
    private static int lastCenterX = Integer.MIN_VALUE;
    private static int lastCenterZ = Integer.MIN_VALUE;
    private static int lastRadius = -1;
    private static int sectionCursor;

    private EspRenderer() {}

    public static void register() {
        LevelExtractionEvents.END_EXTRACTION.register(context -> extract());
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(EspRenderer::render);
    }

    /** Call once per client tick. Each tick scans at most two nearby sections. */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;
        if (level == null || player == null) {
            reset(null);
            return;
        }

        boolean storageOn = enabled("Storage ESP");
        boolean blocksOn = enabled("Block ESP");
        if (!storageOn && !blocksOn) {
            SCANNED.clear();
            SCANNED_KEYS.clear();
            SECTIONS.clear();
            sectionCursor = 0;
            lastLevel = level;
            frameMarkers = List.of();
            return;
        }

        int range = Math.max(
            storageOn ? EspSettings.getRange("Storage ESP") : 0,
            blocksOn ? EspSettings.getRange("Block ESP") : 0
        );
        int centerX = player.getBlockX() >> 4;
        int centerZ = player.getBlockZ() >> 4;
        int radius = (int) Math.ceil(range / 16.0);

        if (level != lastLevel || centerX != lastCenterX || centerZ != lastCenterZ || radius != lastRadius) {
            lastLevel = level;
            lastCenterX = centerX;
            lastCenterZ = centerZ;
            lastRadius = radius;
            rebuildSections(level, centerX, centerZ, radius, player.getBlockY() >> 4);
            SCANNED.clear();
            SCANNED_KEYS.clear();
        }

        if (SECTIONS.isEmpty() || SCANNED.size() >= MAX_MARKERS) return;
        for (int scan = 0; scan < 2 && !SECTIONS.isEmpty(); scan++) {
        Section section = SECTIONS.get(sectionCursor++);
        if (sectionCursor >= SECTIONS.size()) sectionCursor = 0;

        int baseX = section.chunkX << 4;
        int baseY = section.sectionY << 4;
        int baseZ = section.chunkZ << 4;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        if (!level.hasChunkAt(new BlockPos(baseX, baseY, baseZ))) continue;

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    pos.set(baseX + x, baseY + y, baseZ + z);
                    BlockState state = level.getBlockState(pos);
                    Marker marker = markerFor(state, pos.getX(), pos.getY(), pos.getZ());
                    if (marker == null) continue;
                    long key = BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ());
                    if (SCANNED.size() < MAX_MARKERS && SCANNED_KEYS.add(key)) SCANNED.add(marker);
                }
            }
        }
        }
    }

    private static boolean enabled(String name) {
        var module = ModuleManager.getModule(name);
        return module != null && module.isEnabled();
    }

    private static void rebuildSections(ClientLevel level, int centerX, int centerZ, int radius, int centerSectionY) {
        SECTIONS.clear();
        int minSection = level.getMinSectionY();
        int maxSection = level.getMaxSectionY();
        for (int ring = 0; ring <= radius; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                    // Visit the player's current height first, then expand vertically.
                    for (int offset = 0; offset <= Math.max(centerSectionY - minSection, maxSection - centerSectionY); offset++) {
                        int lower = centerSectionY - offset;
                        int upper = centerSectionY + offset;
                        if (lower >= minSection && lower < maxSection) {
                            SECTIONS.add(new Section(centerX + dx, lower, centerZ + dz));
                        }
                        if (offset > 0 && upper >= minSection && upper < maxSection) {
                            SECTIONS.add(new Section(centerX + dx, upper, centerZ + dz));
                        }
                    }
                }
            }
        }
        sectionCursor = 0;
    }

    private static Marker markerFor(BlockState state, int x, int y, int z) {
        boolean storage = state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST)
            || state.is(Blocks.BARREL) || state.is(Blocks.ENDER_CHEST)
            || state.is(Blocks.SHULKER_BOX) || state.is(Blocks.HOPPER)
            || state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE)
            || state.is(Blocks.SMOKER);
        if (storage && enabled("Storage ESP") && storageTypeEnabled(state)) {
            return new Marker(BlockPos.asLong(x, y, z), x, y, z, Kind.STORAGE);
        }

        boolean selectedBlock = (state.is(Blocks.OBSIDIAN) && EspSettings.showBlockObsidian())
            || (state.is(Blocks.BEACON) && EspSettings.showBlockBeacons())
            || ((state.is(Blocks.PISTON) || state.is(Blocks.STICKY_PISTON)) && EspSettings.showBlockPistons())
            || ((state.is(Blocks.ENCHANTING_TABLE) || state.is(Blocks.ANCIENT_DEBRIS)) && EspSettings.showBlockOther());
        if (selectedBlock && enabled("Block ESP")) {
            return new Marker(BlockPos.asLong(x, y, z), x, y, z, Kind.BLOCK);
        }
        return null;
    }

    private static boolean storageTypeEnabled(BlockState state) {
        if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.ENDER_CHEST)) {
            return EspSettings.showStorageChests();
        }
        if (state.is(Blocks.BARREL)) return EspSettings.showStorageBarrels();
        if (state.is(Blocks.SHULKER_BOX)) return EspSettings.showStorageShulkers();
        return EspSettings.showStorageOther();
    }

    private static void extract() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player self = mc.player;
        if (level == null || self == null) {
            frameMarkers = List.of();
            return;
        }

        List<Marker> visible = new ArrayList<>();
        if (enabled("Player ESP")) {
            double max = EspSettings.getRange("Player ESP");
            double maxSq = max * max;
            for (Player target : level.players()) {
                if (target == self || target.isRemoved()) continue;
                if (self.distanceToSqr(target) > maxSq) continue;
                AABB box = target.getBoundingBox();
                visible.add(new Marker(0L, box.minX, box.minY, box.minZ,
                    box.maxX, box.maxY, box.maxZ, Kind.PLAYER));
                if (visible.size() >= MAX_MARKERS) break;
            }
        }

        if (visible.size() < MAX_MARKERS && (enabled("Storage ESP") || enabled("Block ESP"))) {
            for (Marker marker : SCANNED) {
                if (visible.size() >= MAX_MARKERS) break;
                double dx = marker.x + 0.5 - self.getX();
                double dy = marker.y + 0.5 - self.getY();
                double dz = marker.z + 0.5 - self.getZ();
                double distanceSq = dx * dx + dy * dy + dz * dz;
                if (marker.kind == Kind.STORAGE && enabled("Storage ESP")
                    && distanceSq <= square(EspSettings.getRange("Storage ESP"))) visible.add(marker);
                if (marker.kind == Kind.BLOCK && enabled("Block ESP")
                    && distanceSq <= square(EspSettings.getRange("Block ESP"))) visible.add(marker);
            }
        }
        frameMarkers = List.copyOf(visible);
    }

    private static double square(double value) { return value * value; }

    private static void render(LevelRenderContext context) {
        if (frameMarkers.isEmpty()) return;
        RenderPipeline pipeline = EspSettings.isThroughWalls() ? THROUGH_WALLS : RenderPipelines.DEBUG_FILLED_BOX;
        VertexFormat format = pipeline.getVertexFormatBinding(0);
        if (format == null) return;

        PrimitiveTopology topology = pipeline.getPrimitiveTopology();
        StagedVertexBuffer.Draw draw = BUFFER.appendDraw(format, topology,
            topology == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null);

        PoseStack pose = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer builder = BUFFER.getVertexBuilder(draw);

        for (Marker marker : frameMarkers) {
            float r, g, b, a;
            switch (marker.kind) {
                case PLAYER -> { r = 0.30f; g = 0.90f; b = 0.45f; a = 0.22f; }
                case STORAGE -> { r = 0.68f; g = 0.34f; b = 0.95f; a = 0.32f; }
                default -> { r = 0.35f; g = 0.70f; b = 1.00f; a = 0.25f; }
            }
            float minX = (float) marker.x, minY = (float) marker.y, minZ = (float) marker.z;
            float maxX = (float) (marker.kind == Kind.PLAYER ? marker.maxX : marker.x + 1);
            float maxY = (float) (marker.kind == Kind.PLAYER ? marker.maxY : marker.y + 1);
            float maxZ = (float) (marker.kind == Kind.PLAYER ? marker.maxZ : marker.z + 1);
            renderBox(pose.last().pose(), builder, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a);
        }
        pose.popPose();

        BUFFER.upload();
        StagedVertexBuffer.ExecuteInfo info = BUFFER.getExecuteInfo(draw);
        if (info != null) draw(info, pipeline);
        BUFFER.endFrame();
    }

    private static void renderBox(Matrix4fc matrix, VertexConsumer out,
        float x1, float y1, float z1, float x2, float y2, float z2,
        float r, float g, float b, float a) {
        face(matrix, out, x1,y1,z2, x2,y1,z2, x2,y2,z2, x1,y2,z2, r,g,b,a);
        face(matrix, out, x2,y1,z1, x1,y1,z1, x1,y2,z1, x2,y2,z1, r,g,b,a);
        face(matrix, out, x1,y1,z1, x1,y1,z2, x1,y2,z2, x1,y2,z1, r,g,b,a);
        face(matrix, out, x2,y1,z2, x2,y1,z1, x2,y2,z1, x2,y2,z2, r,g,b,a);
        face(matrix, out, x1,y2,z2, x2,y2,z2, x2,y2,z1, x1,y2,z1, r,g,b,a);
        face(matrix, out, x1,y1,z1, x2,y1,z1, x2,y1,z2, x1,y1,z2, r,g,b,a);
    }

    private static void face(Matrix4fc m, VertexConsumer out,
        float ax,float ay,float az, float bx,float by,float bz,
        float cx,float cy,float cz, float dx,float dy,float dz,
        float r,float g,float b,float a) {
        out.addVertex(m, ax, ay, az).setColor(r,g,b,a);
        out.addVertex(m, bx, by, bz).setColor(r,g,b,a);
        out.addVertex(m, cx, cy, cz).setColor(r,g,b,a);
        out.addVertex(m, dx, dy, dz).setColor(r,g,b,a);
    }

    private static void draw(StagedVertexBuffer.ExecuteInfo info, RenderPipeline pipeline) {
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
            .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        GpuTextureView color = target.getColorTextureView();
        if (color == null) return;
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder()
            .createRenderPass(() -> "RIFT ESP", color, Optional.empty(),
                target.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setVertexBuffer(0, info.vertexBuffer().slice());
            pass.setIndexBuffer(info.indexBuffer(), info.indexType());
            pass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }

    private static void reset(ClientLevel level) {
        lastLevel = level;
        SCANNED.clear();
        SCANNED_KEYS.clear();
        SECTIONS.clear();
        frameMarkers = List.of();
        sectionCursor = 0;
        lastCenterX = Integer.MIN_VALUE;
        lastCenterZ = Integer.MIN_VALUE;
        lastRadius = -1;
    }

    private enum Kind { PLAYER, STORAGE, BLOCK }
    private record Section(int chunkX, int sectionY, int chunkZ) {}
    private record Marker(long key, double x, double y, double z,
        double maxX, double maxY, double maxZ, Kind kind) {
        Marker(long key, int x, int y, int z, Kind kind) {
            this(key, x, y, z, x + 1, y + 1, z + 1, kind);
        }
        Marker(long key, double x, double y, double z, Kind kind) {
            this(key, x, y, z, x + 1, y + 1, z + 1, kind);
        }
    }
}
