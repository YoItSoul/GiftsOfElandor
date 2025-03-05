package com.soul.goe.blocks.custom;

import com.mojang.serialization.MapCodec;
import com.soul.goe.blocks.entity.EmpoweredLanternEntity;
import com.soul.goe.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EmpoweredLantern extends BaseEntityBlock {
    public EmpoweredLantern(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EmpoweredLanternEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return BaseEntityBlock.createTickerHelper(type, ModBlockEntities.EMPOWERED_LANTERN.get(),
                EmpoweredLanternEntity::tick);
    }
}