package com.soul.goe.items.custom;

import com.soul.goe.Config;
import com.soul.goe.Goe;
import com.soul.goe.spells.SpellData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Wand extends Item {
    private final boolean hasFoilEffect;
    private final int maxSpells;
    private static final double ITEM_SPAWN_OFFSET = 0.5;
    private static final double PARTICLE_Y_OFFSET = 1.0;
    private static final int PARTICLE_COUNT = 30;
    private static final double PARTICLE_SPREAD = 0.5;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final int CAST_TIME_TICKS = 20;
    private static final String CURRENT_SPELL_KEY = "CurrentSpell";
    private static final String BOUND_SPELLS_KEY = "BoundSpells";

    private static Map<Block, ItemStack> WAND_CATALYSTS = new HashMap<>();

    public Wand(Properties properties, boolean hasFoilEffect, int maxSpells) {
        super(properties);
        this.hasFoilEffect = hasFoilEffect;
        this.maxSpells = maxSpells;
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return this.hasFoilEffect;
    }

    public int getMaxSpells() {
        return maxSpells;
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack wandStack = player.getItemInHand(hand);

        // No more shift+right click logic - radial menu is now handled by keybind
        if (getCurrentSpell(wandStack, level.registryAccess()).isEmpty()) {
            showMessage(player, "No spell bound to this wand!");
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    // Public method that can be called from ClientInputHandler
    public void openSpellRadialMenuFromClient(ItemStack wandStack, HolderLookup.Provider registryAccess) {
        openSpellRadialMenu(wandStack, registryAccess);
    }

    // Client-side method to open radial menu
    private void openSpellRadialMenu(ItemStack wandStack, HolderLookup.Provider registryAccess) {
        NonNullList<ItemStack> boundSpells = getBoundSpells(wandStack, registryAccess);

        // Filter out empty spells but keep track of original indices
        List<ItemStack> validSpells = new ArrayList<>();
        List<Integer> originalIndices = new ArrayList<>();

        for (int i = 0; i < boundSpells.size(); i++) {
            ItemStack spell = boundSpells.get(i);
            if (!spell.isEmpty()) {
                validSpells.add(spell);
                originalIndices.add(i);
            }
        }

        if (!validSpells.isEmpty()) {
            int currentSpellIndex = getCurrentSpellIndex(wandStack);
            int displayIndex = originalIndices.indexOf(currentSpellIndex);
            if (displayIndex == -1) displayIndex = 0;

            // Open the radial menu screen
            net.minecraft.client.Minecraft.getInstance().setScreen(new com.soul.goe.client.screens.SpellRadialScreen(wandStack, validSpells, originalIndices, displayIndex));
        }
    }

    private int getCurrentSpellIndex(ItemStack wandStack) {
        CompoundTag tag = getWandTag(wandStack);
        if (tag == null || !tag.contains(CURRENT_SPELL_KEY)) return 0;
        return tag.getInt(CURRENT_SPELL_KEY);
    }

    // Method to be called from the radial menu to set the selected spell
    public void setCurrentSpell(ItemStack wandStack, int spellIndex) {
        CompoundTag tag = getOrCreateWandTag(wandStack);
        tag.putInt(CURRENT_SPELL_KEY, spellIndex);
        setWandTag(wandStack, tag);
    }

    private void cycleCurrentSpell(ItemStack wandStack, boolean forward) {
        CompoundTag tag = getOrCreateWandTag(wandStack);

        if (!tag.contains(BOUND_SPELLS_KEY)) {
            return;
        }

        ListTag spells = tag.getList(BOUND_SPELLS_KEY, Tag.TAG_COMPOUND);
        if (spells.isEmpty()) {
            return;
        }

        int totalSpells = spells.size();
        int current = tag.getInt(CURRENT_SPELL_KEY);
        int newIndex;

        if (forward) {
            newIndex = (current + 1) % totalSpells;
        } else {
            newIndex = (current - 1 + totalSpells) % totalSpells;
        }

        tag.putInt(CURRENT_SPELL_KEY, newIndex);
        setWandTag(wandStack, tag);
    }

    public ItemStack getCurrentSpell(ItemStack wandStack, HolderLookup.Provider registryAccess) {
        CompoundTag tag = getWandTag(wandStack);
        if (tag == null || !tag.contains(CURRENT_SPELL_KEY)) return ItemStack.EMPTY;

        int currentIndex = tag.getInt(CURRENT_SPELL_KEY);
        if (!tag.contains(BOUND_SPELLS_KEY)) return ItemStack.EMPTY;

        ListTag spells = tag.getList(BOUND_SPELLS_KEY, Tag.TAG_COMPOUND);
        if (!spells.isEmpty() && currentIndex >= 0 && currentIndex < spells.size()) {
            CompoundTag spellTag = spells.getCompound(currentIndex);
            return ItemStack.parseOptional(registryAccess, spellTag);
        }
        return ItemStack.EMPTY;
    }

    public boolean addSpell(ItemStack wandStack, ItemStack spellStack, HolderLookup.Provider registryAccess) {
        CompoundTag tag = getOrCreateWandTag(wandStack);
        ListTag spells;

        if (tag.contains(BOUND_SPELLS_KEY)) {
            spells = tag.getList(BOUND_SPELLS_KEY, Tag.TAG_COMPOUND);
        } else {
            spells = new ListTag();
        }

        for (int i = 0; i < spells.size(); i++) {
            if (spells.getCompound(i).isEmpty()) {
                CompoundTag spellTag = (CompoundTag) spellStack.save(registryAccess);
                spells.set(i, spellTag);
                tag.put(BOUND_SPELLS_KEY, spells);
                setWandTag(wandStack, tag);
                return true;
            }
        }

        if (spells.size() < maxSpells) {
            CompoundTag spellTag = (CompoundTag) spellStack.save(registryAccess);
            spells.add(spellTag);
            tag.put(BOUND_SPELLS_KEY, spells);
            setWandTag(wandStack, tag);
            return true;
        }

        return false;
    }

    public NonNullList<ItemStack> getBoundSpells(ItemStack wandStack, HolderLookup.Provider lookupProvider) {
        NonNullList<ItemStack> spells = NonNullList.withSize(maxSpells, ItemStack.EMPTY);
        CompoundTag tag = getWandTag(wandStack);
        if (tag == null || !tag.contains(BOUND_SPELLS_KEY)) return spells;

        ListTag spellList = tag.getList(BOUND_SPELLS_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(spellList.size(), maxSpells); i++) {
            CompoundTag spellTag = spellList.getCompound(i);
            if (lookupProvider != null) {
                ItemStack spell = ItemStack.parseOptional(lookupProvider, spellTag);
                spells.set(i, spell);
            }
        }

        return spells;
    }

    private CompoundTag getOrCreateWandTag(ItemStack wandStack) {
        CompoundTag tag = getWandTag(wandStack);
        if (tag == null || tag.isEmpty()) {
            tag = new CompoundTag();
            tag.putInt(CURRENT_SPELL_KEY, 0);
            setWandTag(wandStack, tag);
        }
        return tag;
    }

    private CompoundTag getWandTag(ItemStack wandStack) {
        CustomData existingData = wandStack.get(DataComponents.CUSTOM_DATA);
        if (existingData != null) {
            return existingData.copyTag();
        }
        return new CompoundTag();
    }

    private void setWandTag(ItemStack wandStack, CompoundTag tag) {
        wandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
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
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.5F);
        }
    }

    private boolean attemptSpellCast(Level level, Player player, ItemStack wandStack) {
        ItemStack spellStack = getCurrentSpell(wandStack, level.registryAccess());

        if (spellStack.isEmpty()) {
            playFailureEffects(level, player, "No spell bound");
            showMessage(player, "No spell bound to this wand!");
            return false;
        }

        if (!(spellStack.getItem() instanceof Spell spellItem)) {
            playFailureEffects(level, player, "Invalid spell item");
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
            // Only show detailed ingredients on server side to prevent duplication
            if (!level.isClientSide()) {
                showDetailedIngredientList(player, spellData);
            }
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
            level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH + 0.2F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }

    private void playFailureEffects(Level level, Player player, String reason) {
        Goe.LOGGER.info("Spell cast failed: {}", reason);

        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, SOUND_VOLUME * 0.7F, SOUND_PITCH - 0.3F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    private void showDetailedIngredientList(Player player, SpellData spellData) {
        ItemStack currentSpell = getCurrentSpell(player.getMainHandItem(), player.level().registryAccess());
        String spellName = currentSpell.isEmpty() ? "Unknown Spell" : currentSpell.getHoverName().getString();

        List<Component> ingredientComponents = new ArrayList<>();

        spellData.getCost().getCosts().forEach((item, requiredAmount) -> {
            int playerAmount = player.getInventory().countItem(item);
            boolean hasEnough = playerAmount >= requiredAmount;

            ChatFormatting numberColor = hasEnough ? ChatFormatting.GREEN : ChatFormatting.RED;

            // Create ItemStack for JEI interaction
            ItemStack displayStack = new ItemStack(item, requiredAmount);

            // Create component with colored number and actual ItemStack component
            MutableComponent numberComponent = Component.literal(String.valueOf(requiredAmount)).withStyle(numberColor);

            // Use the ItemStack's own component which JEI can interact with
            MutableComponent itemComponent = Component.empty().append(displayStack.getHoverName()).withStyle(style -> style.withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM, new net.minecraft.network.chat.HoverEvent.ItemStackInfo(displayStack))).withColor(hasEnough ? ChatFormatting.GREEN : ChatFormatting.RED));

            MutableComponent fullComponent = numberComponent.append(Component.literal(" × ").withStyle(ChatFormatting.WHITE)).append(itemComponent);

            ingredientComponents.add(fullComponent);
        });

        if (!ingredientComponents.isEmpty()) {
            // Start with spell name
            MutableComponent message = Component.literal("\"" + spellName + "\"").withStyle(ChatFormatting.AQUA).append(Component.literal(" requires: ").withStyle(ChatFormatting.YELLOW));

            for (int i = 0; i < ingredientComponents.size(); i++) {
                message = message.append(ingredientComponents.get(i));
                if (i < ingredientComponents.size() - 1) {
                    message = message.append(Component.literal(", ").withStyle(ChatFormatting.WHITE));
                }
            }

            // Send to chat for JEI interaction support
            player.displayClientMessage(message, false);
        }
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
        return Math.round(13.0F - (float) stack.getDamageValue() * 13.0F / (float) stack.getMaxDamage());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float) stack.getMaxDamage() - (float) stack.getDamageValue()) / (float) stack.getMaxDamage());
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
        level.playSound(null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);

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
        level.sendParticles(ParticleTypes.ENCHANT, blockPos.getX() + ITEM_SPAWN_OFFSET, blockPos.getY() + PARTICLE_Y_OFFSET, blockPos.getZ() + ITEM_SPAWN_OFFSET, PARTICLE_COUNT, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD, 0.0);
    }
}