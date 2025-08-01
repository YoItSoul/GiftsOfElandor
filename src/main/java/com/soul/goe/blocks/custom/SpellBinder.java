package com.soul.goe.blocks.custom;

import com.mojang.serialization.MapCodec;
import com.soul.goe.blocks.entity.SpellBinderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpellBinder extends BaseEntityBlock {
    public static final MapCodec<SpellBinder> CODEC = simpleCodec(SpellBinder::new);

    private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 2, 16),        // Base platform
            Block.box(0, 14, 0, 16, 16, 16),      // Top platform
            Block.box(0, 2, 0, 2, 14, 2),         // Corner leg 1
            Block.box(0, 2, 14, 2, 14, 16),       // Corner leg 2
            Block.box(14, 2, 0, 16, 14, 2),       // Corner leg 3
            Block.box(14, 2, 14, 16, 14, 16),     // Corner leg 4
            Block.box(6, 2, 6, 10, 10, 10),       // Central pillar
            Block.box(4, 12, 4, 12, 14, 12),      // Upper funnel
            Block.box(5, 10, 5, 11, 12, 11),      // Lower funnel

            Block.box(0, 13, 2, 1, 14, 3),        // Top detail 1
            Block.box(0, 13, 13, 1, 14, 14),      // Top detail 2
            Block.box(15, 13, 2, 16, 14, 3),      // Top detail 3
            Block.box(15, 13, 13, 16, 14, 14),    // Top detail 4
            Block.box(13, 13, 15, 14, 14, 16),    // Top detail 5
            Block.box(2, 13, 15, 3, 14, 16),      // Top detail 6
            Block.box(13, 13, 0, 14, 14, 1),      // Top detail 7
            Block.box(2, 13, 0, 3, 14, 1),        // Top detail 8

            Block.box(2, 2, 0, 3, 3, 1),          // Base detail 1
            Block.box(13, 2, 0, 14, 3, 1),        // Base detail 2
            Block.box(2, 2, 15, 3, 3, 16),        // Base detail 3
            Block.box(13, 2, 15, 14, 3, 16),      // Base detail 4
            Block.box(15, 2, 13, 16, 3, 14),      // Base detail 5
            Block.box(15, 2, 2, 16, 3, 3),        // Base detail 6
            Block.box(0, 2, 13, 1, 3, 14),        // Base detail 7
            Block.box(0, 2, 2, 1, 3, 3),          // Base detail 8

            Block.box(5, 2, 9, 6, 3, 10),         // Center detail 1
            Block.box(10, 2, 6, 11, 3, 7),        // Center detail 2
            Block.box(10, 2, 9, 11, 3, 10),       // Center detail 3
            Block.box(5, 2, 6, 6, 3, 7),          // Center detail 4
            Block.box(6, 2, 5, 7, 3, 6),          // Center detail 5
            Block.box(9, 2, 5, 10, 3, 6),         // Center detail 6
            Block.box(6, 2, 10, 7, 3, 11),        // Center detail 7
            Block.box(9, 2, 10, 10, 3, 11)        // Center detail 8
    );

    public SpellBinder(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SpellBinderEntity spellBinderEntity) {
                player.openMenu(spellBinderEntity, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpellBinderEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SpellBinderEntity spellBinderEntity) {
                spellBinderEntity.onWandRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
}