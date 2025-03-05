package com.soul.goe.blocks.custom;

import com.soul.goe.blocks.entity.WardingLanternEntity;
import com.soul.goe.items.custom.Wand;
import com.soul.goe.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class WardingLantern extends Block implements EntityBlock {
    private static final int LIGHT_LEVEL = 15;
    public static final EnumProperty<BlockVariant> VARIANT = EnumProperty.create("variant", BlockVariant.class);

    public WardingLantern(Properties properties) {
        super(properties.lightLevel(state -> LIGHT_LEVEL));
        registerDefaultState(getStateDefinition().any()
                .setValue(VARIANT, BlockVariant.BLANK));
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (!stack.isEmpty() && shouldCycleVariant(level, player, stack)) {
            return cycleVariant(state, level, pos);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit
    ) {
        return InteractionResult.PASS;
    }

    private InteractionResult cycleVariant(BlockState state, Level level, BlockPos pos) {
        BlockVariant nextVariant = state.getValue(VARIANT).next();
        level.setBlock(pos, state.setValue(VARIANT, nextVariant), Block.UPDATE_ALL);
        return InteractionResult.SUCCESS;
    }

    private boolean shouldCycleVariant(Level level, Player player, ItemStack itemStack) {
        return !level.isClientSide() && itemStack.getItem() instanceof Wand;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.WARDING_LANTERN.get() ?
                (level1, pos, state1, blockEntity) -> WardingLanternEntity.tick(level1, pos, state1, (WardingLanternEntity) blockEntity) :
                null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WardingLanternEntity(pos, state);
    }




    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(VARIANT, BlockVariant.BLANK);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 1.0F;
    }
}

enum BlockVariant implements StringRepresentable {
    BLANK, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z;

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    public BlockVariant next() {
        return values()[(ordinal() + 1) % values().length];
    }
}