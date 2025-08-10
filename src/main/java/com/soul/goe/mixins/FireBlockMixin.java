package com.soul.goe.mixins;

import com.soul.goe.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for FireBlock that adds ash dropping functionality when blocks burn.
 */
@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
    @Unique
    private static final int ASH_DROP_CHANCE = 3;
    @Unique
    private static final int MAX_ASH_PER_BURN = 5;
    @Unique
    private static final int MIN_ASH_PER_BURN = 1;
    @Unique
    private static final double ITEM_SPAWN_OFFSET = 0.5D;
    @Unique
    private static final double ITEM_SPAWN_RANDOMNESS = 0.8D;

    /**
     * Handles fire tick events to potentially drop ash when nearby blocks burn.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onFireTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (level.isClientSide || random.nextInt(ASH_DROP_CHANCE) != 0) {
            return;
        }

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int attempts = random.nextInt(4) + 1; // 1-4 attempts per tick

        for (int i = 0; i < attempts; i++) {
            mutablePos.setWithOffset(
                    pos,
                    random.nextInt(3) - 1, // -1 to 1
                    random.nextInt(5) - 1, // -1 to 3
                    random.nextInt(3) - 1   // -1 to 1
            );

            if (((FireBlock)(Object)this).canCatchFire(level, mutablePos, Direction.UP)
                    && level.setBlock(mutablePos, Blocks.FIRE.defaultBlockState(), 3)) {
                giftsOfElandor$dropAshItems(level, mutablePos, random);
            }
        }
    }

    /**
     * Handles fire placement to drop ash when replacing existing blocks.
     */
    @Inject(method = "onPlace", at = @At("TAIL"))
    private void onFirePlace(BlockState state, Level level, BlockPos pos, BlockState oldState,
                             boolean isMoving, CallbackInfo ci) {
        if (!level.isClientSide && !oldState.isAir()) {
            ((FireBlock) (Object) this).canCatchFire(level, pos, Direction.UP);
        }
        return;
    }

    /**
     * Drops ash items at the specified position.
     */
    @Unique
    private void giftsOfElandor$dropAshItems(Level level, BlockPos pos, RandomSource random) {
        int ashCount = random.nextInt(MAX_ASH_PER_BURN - MIN_ASH_PER_BURN + 1) + MIN_ASH_PER_BURN;
        ItemStack ashStack = new ItemStack(ModItems.ASH.get(), ashCount);

        double x = pos.getX() + ITEM_SPAWN_OFFSET + (random.nextDouble() - 0.5D) * ITEM_SPAWN_RANDOMNESS;
        double y = pos.getY() + ITEM_SPAWN_OFFSET;
        double z = pos.getZ() + ITEM_SPAWN_OFFSET + (random.nextDouble() - 0.5D) * ITEM_SPAWN_RANDOMNESS;

        ItemEntity itemEntity = new ItemEntity(level, x, y, z, ashStack);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }
}