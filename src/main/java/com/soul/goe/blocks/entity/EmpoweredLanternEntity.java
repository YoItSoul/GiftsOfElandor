package com.soul.goe.blocks.entity;

import com.soul.goe.registry.ModBlockEntities;
import com.soul.goe.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class EmpoweredLanternEntity extends BlockEntity {
    private static final int SCAN_RADIUS = 48;
    private static final int TICKS_BETWEEN_SCANS = 100;
    private static final int FLARE_SPACING = 8;
    private static final int MIN_LIGHT_LEVEL = 7;
    private static final int MAX_FLARES_PER_SCAN = 5;
    private static final int FLARE_SPACING_SQUARED = FLARE_SPACING * FLARE_SPACING;

    private final Set<BlockPos> placedFlares = new HashSet<>();
    private final Random random = new Random();

    private int tickCounter;
    private long lastFullScan;
    private boolean scanInProgress;
    private Iterator<BlockPos> scanIterator;
    private int consecutiveEmptyScans;
    private long sleepUntil;
    private int totalFlaresPlacedThisScan;

    public EmpoweredLanternEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EMPOWERED_LANTERN.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EmpoweredLanternEntity entity) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;

        entity.tickCounter++;

        if (entity.scanInProgress) {
            entity.continueScan(serverLevel);
        } else if (entity.shouldStartNewScan()) {
            entity.startNewScan();
        }

        if (entity.tickCounter % 2400 == 0) {
            entity.cleanupInvalidFlares(serverLevel);
        }
    }

    private boolean shouldStartNewScan() {
        long currentTime = System.currentTimeMillis();

        if (currentTime < sleepUntil) {
            return false;
        }

        return tickCounter >= TICKS_BETWEEN_SCANS && !scanInProgress &&
                (currentTime - lastFullScan > 3000);
    }

    private void startNewScan() {
        scanInProgress = true;
        tickCounter = 0;
        lastFullScan = System.currentTimeMillis();
        totalFlaresPlacedThisScan = 0;

        List<BlockPos> scanPositions = generateScanPositions();
        scanIterator = scanPositions.iterator();

        System.out.println("Starting smart flare scan with " + scanPositions.size() + " positions");
    }

    private List<BlockPos> generateScanPositions() {
        List<BlockPos> positions = new ArrayList<>();

        List<Monster> nearbyMobs = getNearbyMobs();

        if (!nearbyMobs.isEmpty()) {
            for (Monster mob : nearbyMobs.subList(0, Math.min(5, nearbyMobs.size()))) {
                BlockPos mobPos = mob.blockPosition();
                addScanArea(positions, mobPos, 8);
            }
        }

        int baseScans = nearbyMobs.isEmpty() ? 16 : 8;
        for (int i = 0; i < baseScans; i++) {
            double angle = (2 * Math.PI * i) / baseScans;
            int distance = 16 + random.nextInt(SCAN_RADIUS - 16);

            int x = worldPosition.getX() + (int)(Math.cos(angle) * distance);
            int z = worldPosition.getZ() + (int)(Math.sin(angle) * distance);

            addScanArea(positions, new BlockPos(x, worldPosition.getY(), z), 4);
        }

        Collections.shuffle(positions, random);
        return positions;
    }

    private List<Monster> getNearbyMobs() {
        if (!(level instanceof ServerLevel serverLevel)) return new ArrayList<>();

        AABB searchArea = new AABB(worldPosition).inflate(SCAN_RADIUS);
        return serverLevel.getEntitiesOfClass(Monster.class, searchArea);
    }

    private void addScanArea(List<BlockPos> positions, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x += 4) {
            for (int z = -radius; z <= radius; z += 4) {
                if (x*x + z*z <= radius*radius) {
                    positions.add(center.offset(x, 0, z));
                }
            }
        }
    }

    private void continueScan(ServerLevel level) {
        int processed = 0;
        int flaresPlaced = 0;

        while (scanIterator.hasNext() && processed < 5 && flaresPlaced < MAX_FLARES_PER_SCAN) {
            BlockPos scanPos = scanIterator.next();
            processed++;

            if (checkAndPlaceFlareAtPosition(level, scanPos)) {
                flaresPlaced++;
                totalFlaresPlacedThisScan++;
            }
        }

        if (!scanIterator.hasNext()) {
            scanInProgress = false;

            if (totalFlaresPlacedThisScan == 0) {
                consecutiveEmptyScans++;
                System.out.println("Empty scan #" + consecutiveEmptyScans + ". No valid flare positions found.");

                if (consecutiveEmptyScans >= 5) {
                    sleepUntil = System.currentTimeMillis() + 30000;
                    consecutiveEmptyScans = 0;
                    System.out.println("Entering sleep mode for 30 seconds - no valid positions found in 5 consecutive scans.");
                }
            } else {
                consecutiveEmptyScans = 0;
                System.out.println("Flare scan complete. Placed " + totalFlaresPlacedThisScan + " flares total.");
            }
        }
    }

    private boolean checkAndPlaceFlareAtPosition(ServerLevel level, BlockPos scanPos) {
        if (!level.isLoaded(scanPos)) return false;

        BlockPos bestSpot = findBestFlareSpot(level, scanPos);
        if (bestSpot != null && canPlaceFlare(level, bestSpot)) {
            level.setBlock(bestSpot, ModBlocks.FLARE.get().defaultBlockState(), 3);
            placedFlares.add(bestSpot);
            System.out.println("Placed smart flare at " + bestSpot);
            return true;
        }
        return false;
    }

    private BlockPos findBestFlareSpot(ServerLevel level, BlockPos center) {
        BlockPos bestPos = null;
        int minLight = 16;

        int maxY = Math.min(320, center.getY() + 64);
        int minY = Math.max(-64, center.getY() - 32);

        for (int y = minY; y < maxY; y += 2) {
            BlockPos checkPos = new BlockPos(center.getX(), y, center.getZ());

            if (!level.getBlockState(checkPos).isAir()) continue;

            BlockState groundState = level.getBlockState(checkPos.below());
            if (!groundState.isSolidRender()) continue;

            int light = Math.max(
                    level.getBrightness(LightLayer.BLOCK, checkPos),
                    level.getBrightness(LightLayer.SKY, checkPos)
            );

            if (light < MIN_LIGHT_LEVEL && light < minLight) {
                minLight = light;
                bestPos = checkPos;

                if (light == 0) break;
            }

            if (light >= MIN_LIGHT_LEVEL && bestPos != null) {
                break;
            }
        }

        return bestPos;
    }

    private boolean canPlaceFlare(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() || state.is(ModBlocks.FLARE.get())) {
            return false;
        }

        for (BlockPos existingFlare : placedFlares) {
            if (existingFlare.distSqr(pos) < FLARE_SPACING_SQUARED) {
                return false;
            }
        }

        BlockPos groundPos = pos.below();
        BlockState groundState = level.getBlockState(groundPos);

        return groundState.isSolidRender() &&
                groundState.isValidSpawn(level, groundPos, net.minecraft.world.entity.EntityType.ZOMBIE);
    }

    private void cleanupInvalidFlares(ServerLevel level) {
        Iterator<BlockPos> iterator = placedFlares.iterator();

        while (iterator.hasNext()) {
            BlockPos flarePos = iterator.next();

            if (!level.isLoaded(flarePos)) continue;

            if (!level.getBlockState(flarePos).is(ModBlocks.FLARE.get())) {
                iterator.remove();
            }
        }
    }

    public void forceRescan() {
        scanInProgress = false;
        lastFullScan = 0;
        tickCounter = TICKS_BETWEEN_SCANS;
        consecutiveEmptyScans = 0;
        sleepUntil = 0;
    }

    public int getPlacedFlareCount() {
        return placedFlares.size();
    }
}