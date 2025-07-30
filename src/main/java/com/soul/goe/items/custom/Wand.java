package com.soul.goe.items.custom;

import com.soul.goe.Config;
import com.soul.goe.Goe;
import com.soul.goe.spells.SpellData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Wand extends Item {
    private final boolean hasFoilEffect;
    private static final double ITEM_SPAWN_OFFSET = 0.5;
    private static final double PARTICLE_Y_OFFSET = 1.0;
    private static final int PARTICLE_COUNT = 30;
    private static final double PARTICLE_SPREAD = 0.5;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;

    private static final int CAST_TIME_TICKS = 20;

    private static Map<Block, ItemStack> WAND_CATALYSTS = new HashMap<>();

    public Wand(Properties properties, boolean hasFoilEffect) {
        super(properties);
        this.hasFoilEffect = hasFoilEffect;
        Goe.LOGGER.info("Wand created with foil effect: {}", hasFoilEffect);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return this.hasFoilEffect;
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack offHandItem = player.getOffhandItem();

        if (offHandItem.getItem() instanceof Spell spellItem) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        int useDuration = this.getUseDuration(stack, entity) - timeLeft;

        if (useDuration < CAST_TIME_TICKS) {
            playFailureEffects(level, player, "Cast time too short");
            return false;
        }

        playChargeCompleteSound(level, player);
        return attemptSpellCast(level, player, stack);
    }

    private void playChargeCompleteSound(Level level, Player player) {
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.5F,
                    1.5F);
        }
    }

    private boolean attemptSpellCast(Level level, Player player, ItemStack wandStack) {
        ItemStack offHandItem = player.getOffhandItem();

        if (!(offHandItem.getItem() instanceof Spell spellItem)) {
            playFailureEffects(level, player, "No spell in offhand");
            return false;
        }

        if (!spellItem.canCast(offHandItem)) {
            playFailureEffects(level, player, "Spell has no uses remaining");
            showMessage(player, "This spell has no uses remaining!");
            return false;
        }

        Optional<SpellData> spellDataOpt = spellItem.getSpellData();
        if (spellDataOpt.isEmpty()) {
            playFailureEffects(level, player, "No spell data found");
            return false;
        }

        SpellData spellData = spellDataOpt.get();

        if (!spellData.canCast(player)) {
            playFailureEffects(level, player, "Player lacks required ingredients");
            showMissingIngredients(player, spellData);
            return false;
        }

        if (wandStack.getMaxDamage() > 1 && wandStack.getDamageValue() >= wandStack.getMaxDamage()) {
            playFailureEffects(level, player, "Wand is broken");
            showMessage(player, "Your wand is too damaged to cast spells!");
            return false;
        }

        playSuccessEffects(level, player);
        spellData.cast(level, player);

        if (level instanceof ServerLevel serverLevel) {
            wandStack.hurtAndBreak(1, serverLevel, player, item -> {
                showMessage(player, "Your wand has broken!");
            });
        }

        Goe.LOGGER.info("Successfully cast spell: {}", spellData.getSpellId());
        return true;
    }

    private void playSuccessEffects(Level level, Player player) {
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(),
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.PLAYERS,
                    SOUND_VOLUME,
                    SOUND_PITCH + 0.2F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ENCHANT,
                        player.getX(),
                        player.getY() + 1.0,
                        player.getZ(),
                        15,
                        0.5,
                        0.5,
                        0.5,
                        0.1
                );
            }
        }
    }

    private void playFailureEffects(Level level, Player player, String reason) {
        Goe.LOGGER.info("Spell cast failed: {}", reason);

        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(),
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.PLAYERS,
                    SOUND_VOLUME * 0.7F,
                    SOUND_PITCH - 0.3F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        player.getX(),
                        player.getY() + 1.0,
                        player.getZ(),
                        10,
                        0.3,
                        0.3,
                        0.3,
                        0.05
                );
            }
        }
    }

    private void showMissingIngredients(Player player, SpellData spellData) {
        spellData.getCost().getCosts().forEach((item, requiredAmount) -> {
            int playerAmount = player.getInventory().countItem(item);
            if (playerAmount < requiredAmount) {
                int needed = requiredAmount - playerAmount;
                showMessage(player, "Need " + needed + " more " + item.getName().getString());
            }
        });
    }

    private void showMessage(Player player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.isDamaged();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float)stack.getDamageValue() * 13.0F / (float)stack.getMaxDamage());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float)stack.getMaxDamage() - (float)stack.getDamageValue()) / (float)stack.getMaxDamage());
        return net.minecraft.util.Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public static void registerCatalysts() {
        WAND_CATALYSTS.clear();
        Map<Block, ItemStack> configCatalysts = Config.getWandCatalysts();
        WAND_CATALYSTS.putAll(configCatalysts);
        if (WAND_CATALYSTS.isEmpty()) {
            Goe.LOGGER.warn("No wand catalysts were registered!");
        } else {
            Goe.LOGGER.info("Registered {} wand catalysts", WAND_CATALYSTS.size());
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack offHandItem = player.getOffhandItem();

        if (offHandItem.getItem() instanceof Spell) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        return handleWandCrafting(context, level);
    }

    private InteractionResult handleWandCrafting(UseOnContext context, Level level) {
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
        wandStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }

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