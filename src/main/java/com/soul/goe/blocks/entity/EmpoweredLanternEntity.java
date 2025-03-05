package com.soul.goe.blocks.entity;

import com.soul.goe.registry.ModBlockEntities;
import com.soul.goe.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;

import java.util.*;

public class EmpoweredLanternEntity extends BlockEntity {
    private static final int DEFAULT_CHUNK_RADIUS = 3;
    private static final int TICKS_BETWEEN_CHECKS = 5;
    private static final int MIN_SPAWN_HEIGHT = -62;
    private static final int MAX_SPAWN_HEIGHT = 300;
    private static final int HEIGHT_CHECK_INTERVAL = 2;
    private static final int BASE_SAMPLES_PER_CHUNK = 32;
    private static final float GOLDEN_RATIO = 1.618033988749895f;

    private final Queue<ChunkPos> priorityChunks = new LinkedList<>();
    private final Map<ChunkPos, Long> lastCheckedChunks = new HashMap<>();

    private int chunkRadius;
    private int tickCounter;
    private int currentSpiralX;
    private int currentSpiralZ;
    private int spiralDx;
    private int spiralDz;
    private int spiralSteps;

    public EmpoweredLanternEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EMPOWERED_LANTERN.get(), pos, state);
        this.chunkRadius = DEFAULT_CHUNK_RADIUS;
        resetSpiralPattern();
    }

    private void resetSpiralPattern() {
        currentSpiralX = 0;
        currentSpiralZ = 0;
        spiralDx = 0;
        spiralDz = -1;
        spiralSteps = 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EmpoweredLanternEntity entity) {
        if (level.isClientSide) return;

        if (++entity.tickCounter >= TICKS_BETWEEN_CHECKS) {
            entity.tickCounter = 0;
            entity.checkAndPlaceFlares(level, pos);
        }
    }

    private void checkAndPlaceFlares(Level level, BlockPos lanternPos) {
        int baseChunkX = lanternPos.getX() >> 4;
        int baseChunkZ = lanternPos.getZ() >> 4;

        // First process any priority chunks
        while (!priorityChunks.isEmpty() && spiralSteps < 3) { // Limit priority checks per tick
            ChunkPos priorityChunk = priorityChunks.poll();
            scanChunkForDarkSpots(level, priorityChunk.x, priorityChunk.z, true);
            spiralSteps++;
        }

        // Then continue with spiral pattern
        int maxSteps = (int)(Math.PI * chunkRadius * chunkRadius);
        while (spiralSteps < maxSteps) {
            if (isInRadius(currentSpiralX, currentSpiralZ)) {
                int targetChunkX = baseChunkX + currentSpiralX;
                int targetChunkZ = baseChunkZ + currentSpiralZ;

                if (shouldScanChunk(new ChunkPos(targetChunkX, targetChunkZ))) {
                    scanChunkForDarkSpots(level, targetChunkX, targetChunkZ, false);
                    break; // Process one chunk per tick for better performance
                }
            }

            advanceSpiralPattern();
        }

        // Reset spiral pattern when complete
        if (spiralSteps >= maxSteps) {
            resetSpiralPattern();
        }
    }

    private void advanceSpiralPattern() {
        if (currentSpiralX == currentSpiralZ ||
                (currentSpiralX < 0 && currentSpiralX == -currentSpiralZ) ||
                (currentSpiralX > 0 && currentSpiralX == 1-currentSpiralZ)) {
            int temp = spiralDx;
            spiralDx = -spiralDz;
            spiralDz = temp;
        }
        currentSpiralX += spiralDx;
        currentSpiralZ += spiralDz;
        spiralSteps++;
    }

    private boolean isInRadius(int x, int z) {
        return Math.abs(x) <= chunkRadius && Math.abs(z) <= chunkRadius;
    }

    private boolean shouldScanChunk(ChunkPos chunk) {
        long currentTime = System.currentTimeMillis();
        Long lastCheck = lastCheckedChunks.get(chunk);
        if (lastCheck == null || currentTime - lastCheck > 60000) { // 1 minute cooldown
            lastCheckedChunks.put(chunk, currentTime);
            return true;
        }
        return false;
    }

    private void scanChunkForDarkSpots(Level level, int chunkX, int chunkZ, boolean isPriority) {
        // Check if this chunk is within our radius from the lantern
        int lanternChunkX = worldPosition.getX() >> 4;
        int lanternChunkZ = worldPosition.getZ() >> 4;
        int chunkDistance = Math.max(
                Math.abs(chunkX - lanternChunkX),
                Math.abs(chunkZ - lanternChunkZ)
        );

        if (chunkDistance > chunkRadius) {
            return; // Skip chunks outside our radius
        }

        // Rest of the existing method remains the same
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        int distanceFromCenter = Math.max(
                Math.abs(chunkX - (worldPosition.getX() >> 4)),
                Math.abs(chunkZ - (worldPosition.getZ() >> 4))
        );

        int samplesPerChunk = isPriority ?
                BASE_SAMPLES_PER_CHUNK :
                Math.max(16, BASE_SAMPLES_PER_CHUNK - (distanceFromCenter * 4));

        for (int i = 0; i < samplesPerChunk; i++) {
            // Use golden ratio for better distribution
            float t = i * GOLDEN_RATIO;
            int x = startX + Math.floorMod((int)(16 * (t - Math.floor(t))), 16);
            int z = startZ + Math.floorMod((int)(16 * ((t * GOLDEN_RATIO) - Math.floor(t * GOLDEN_RATIO))), 16);

            scanColumn(level, x, z);
        }
    }

    private void scanColumn(Level level, int x, int z) {
        for (int y = MIN_SPAWN_HEIGHT; y < MAX_SPAWN_HEIGHT; y += HEIGHT_CHECK_INTERVAL) {
            BlockPos checkPos = new BlockPos(x, y, z);
            if (shouldPlaceFlare(level, checkPos)) {
                level.setBlock(checkPos, ModBlocks.FLARE.get().defaultBlockState(), 3);
                addNeighborChunksToPriority(checkPos);
                break;
            }
        }
    }

    private void addNeighborChunksToPriority(BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                ChunkPos neighborChunk = new ChunkPos(chunkX + dx, chunkZ + dz);
                if (!priorityChunks.contains(neighborChunk)) {
                    priorityChunks.offer(neighborChunk);
                }
            }
        }
    }

    private boolean shouldPlaceFlare(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // Check if the block is air and not already a flare
        if (!state.is(Blocks.AIR) || state.is(ModBlocks.FLARE.get())) {
            return false;
        }

        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        if (blockLight >= 7 || skyLight >= 7) {
            return false;
        }

        // Check nearby positions for existing flares (in a 5x5x5 cube)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    if (level.getBlockState(checkPos).is(ModBlocks.FLARE.get())) {
                        return false;  // Don't place if there's already a flare nearby
                    }
                }
            }
        }

        // Finally check if a zombie could spawn here
        BlockPos spawnPos = pos.below();
        return level.getBlockState(spawnPos).isValidSpawn(level, spawnPos, net.minecraft.world.entity.EntityType.ZOMBIE);
    }

    public int getChunkRadius() {
        return chunkRadius;
    }

    public void setChunkRadius(int radius) {
        this.chunkRadius = Math.max(1, Math.min(radius, 8));
        resetSpiralPattern();
    }
}