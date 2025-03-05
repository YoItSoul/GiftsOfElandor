package com.soul.goe.items.custom;

import com.soul.goe.Config;
import com.soul.goe.Goe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Wand extends Item {
    private final boolean hasFoilEffect;
    private static final double ITEM_SPAWN_OFFSET = 0.5;
    private static final double PARTICLE_Y_OFFSET = 1.0;
    private static final int PARTICLE_COUNT = 30;
    private static final double PARTICLE_SPREAD = 0.5;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;

    private static Map<Block, ItemStack> WAND_CATALYSTS = new HashMap<>();

    public Wand(Properties properties, boolean hasFoilEffect) {
        super(properties);
        this.hasFoilEffect = hasFoilEffect;
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return this.hasFoilEffect;
    }

    /**
     * Registers catalyst blocks and their corresponding item results
     * To be implemented: Load from configuration file
     */
    public static void registerCatalysts() {
        WAND_CATALYSTS.clear();
        Map<Block, ItemStack> configCatalysts = Config.getWandCatalysts();
        WAND_CATALYSTS.putAll(configCatalysts);
        // Add debug logging
        if (WAND_CATALYSTS.isEmpty()) {
            Goe.LOGGER.warn("No wand catalysts were registered!");
        } else {
            Goe.LOGGER.info("Registered {} wand catalysts", WAND_CATALYSTS.size());
            WAND_CATALYSTS.forEach((block, item) ->
                    Goe.LOGGER.info("Catalyst: {} -> {}", block.getDescriptionId(), item)
            );
        }
    }



    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        return handleWandUse(context, level);
    }

    private InteractionResult handleWandUse(UseOnContext context, Level level) {
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        if (!WAND_CATALYSTS.containsKey(blockState.getBlock())) {
            return InteractionResult.SUCCESS;
        }

        transformBlock(level, blockPos, blockState, player, itemStack);
        return InteractionResult.SUCCESS;
    }

    private void transformBlock(Level level, BlockPos blockPos, BlockState blockState, Player player, ItemStack wandStack) {
        level.removeBlock(blockPos, false);
        playTransformationEffects(level, blockPos);
        spawnResultItems(level, blockPos, blockState);
        wandStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);    }

    private void playTransformationEffects(Level level, BlockPos blockPos) {
        level.playSound(null, blockPos,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.BLOCKS,
                SOUND_VOLUME,
                SOUND_PITCH
        );

        if (level instanceof ServerLevel serverLevel) {
            spawnParticles(serverLevel, blockPos);
        }
    }

    private void spawnResultItems(Level level, BlockPos blockPos, BlockState blockState) {
        ItemStack catalogResult = WAND_CATALYSTS.get(blockState.getBlock());
        ItemStack result = catalogResult.copy();

        double x = blockPos.getX() + ITEM_SPAWN_OFFSET;
        double y = blockPos.getY() + ITEM_SPAWN_OFFSET;
        double z = blockPos.getZ() + ITEM_SPAWN_OFFSET;

        for (int i = 0; i < result.getCount(); i++) {
            ItemStack singleItem = result.copy();
            singleItem.setCount(1);
            level.addFreshEntity(new ItemEntity(level, x, y, z, singleItem));
        }
    }

    private void spawnParticles(ServerLevel level, BlockPos blockPos) {
        level.sendParticles(
                ParticleTypes.ENCHANT,
                blockPos.getX() + ITEM_SPAWN_OFFSET,
                blockPos.getY() + PARTICLE_Y_OFFSET,
                blockPos.getZ() + ITEM_SPAWN_OFFSET,
                PARTICLE_COUNT,
                PARTICLE_SPREAD,
                PARTICLE_SPREAD,
                PARTICLE_SPREAD,
                0.0
        );
    }
}