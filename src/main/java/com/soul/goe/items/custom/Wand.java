package com.soul.goe.items.custom;

import com.soul.goe.Config;
import com.soul.goe.Goe;
import com.soul.goe.spells.util.SpellData;
import com.soul.goe.util.wands.WandMaterialData;
import com.soul.goe.util.wands.WandMaterialStats;
import com.soul.goe.util.wands.WandParts;
import com.soul.goe.util.wands.WandStats;
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
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
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

public final class Wand extends Item {
    private static final Map<Block, ItemStack> WAND_CATALYSTS = new HashMap<>();

    private static final String CURRENT_SPELL_KEY = "CurrentSpell";
    private static final String BOUND_SPELLS_KEY = "BoundSpells";
    private static final String WAND_PARTS_KEY = "WandParts";
    private static final String WAND_STATS_KEY = "WandStats";

    private static final double ITEM_SPAWN_OFFSET = 0.5;
    private static final double PARTICLE_Y_OFFSET = 1.0;
    private static final int PARTICLE_COUNT = 30;
    private static final double PARTICLE_SPREAD = 0.5;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;

    private static final float HANDLE_WEIGHT = 0.5f;
    private static final float CAP_WEIGHT = 0.3f;
    private static final float BINDER_WEIGHT = 0.2f;

    private static final float MIN_STAT_VALUE = 0.1f;
    private static final float MIN_CRITICAL_VALUE = 0.0f;
    private static final float MAX_ADJUSTMENT = 1.5f;
    private static final float MIN_ADJUSTMENT = -0.8f;

    private final boolean hasFoilEffect;


    public Wand(Properties properties, boolean hasFoilEffect) {
        super(properties);
        this.hasFoilEffect = hasFoilEffect;

    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasFoilEffect;
    }

    public int getMaxSpells(ItemStack wandStack) {
        WandStats stats = getWandStats(wandStack);
        if (stats == null) {
            return 1;
        }

        float combinedStats = stats.stability() + stats.durability();
        int spellSlots = 1 + Math.round(combinedStats / 0.5f);
        return Math.min(7, spellSlots);
    }

    public static void setWandParts(ItemStack wandStack, String handle, String binder, String cap) {
        CompoundTag tag = getOrCreateWandTag(wandStack);
        CompoundTag partsTag = new CompoundTag();

        partsTag.putString("handle", handle);
        partsTag.putString("binder", binder);
        partsTag.putString("cap", cap);

        tag.put(WAND_PARTS_KEY, partsTag);
        setWandTag(wandStack, tag);
        calculateAndStoreStats(wandStack);
    }

    public static void setDefaultWandParts(ItemStack wandStack) {
        setWandParts(wandStack, "stick", "string", "amethyst");
    }

    public static void ensureWandHasDefaultParts(ItemStack wandStack) {
        if (getWandParts(wandStack) == null) {
            setDefaultWandParts(wandStack);
        }
    }

    public static ItemStack createDefaultWand(Item wandItem) {
        ItemStack wandStack = new ItemStack(wandItem);
        setDefaultWandParts(wandStack);
        return wandStack;
    }

    public static WandParts getWandParts(ItemStack wandStack) {
        CompoundTag tag = getWandTag(wandStack);
        if (tag == null || !tag.contains(WAND_PARTS_KEY)) {
            return null;
        }

        CompoundTag partsTag = tag.getCompound(WAND_PARTS_KEY);
        String handle = partsTag.getString("handle");
        String binder = partsTag.getString("binder");
        String cap = partsTag.getString("cap");

        if (handle.isEmpty() || binder.isEmpty() || cap.isEmpty()) {
            return null;
        }

        return new WandParts(handle, binder, cap);
    }

    public static void calculateAndStoreStats(ItemStack wandStack) {
        WandParts parts = getWandParts(wandStack);
        if (parts == null) {
            clearStats(wandStack);
            return;
        }

        WandStats stats = calculateStats(parts.handle(), parts.binder(), parts.cap());
        if (stats == null) {
            clearStats(wandStack);
            return;
        }

        storeStats(wandStack, stats);
    }

    private static void clearStats(ItemStack wandStack) {
        CompoundTag tag = getOrCreateWandTag(wandStack);
        tag.remove(WAND_STATS_KEY);
        setWandTag(wandStack, tag);
    }

    private static WandStats calculateStats(String handle, String binder, String cap) {
        WandMaterialStats handleStats = getMaterialStats(handle, "handle");
        WandMaterialStats binderStats = getMaterialStats(binder, "binder");
        WandMaterialStats capStats = getMaterialStats(cap, "cap");

        if (handleStats == null || binderStats == null || capStats == null) {
            return null;
        }

        WandStats base = Config.getBaseWandStats();

        float power = calculateFinalStat(base.power(), handleStats.power(), capStats.power(), binderStats.power());
        float stability = calculateFinalStat(base.stability(), handleStats.stability(), capStats.stability(), binderStats.stability());
        float durability = calculateFinalStat(base.durability(), handleStats.durability(), capStats.durability(), binderStats.durability());
        float critical = calculateFinalStat(base.critical(), handleStats.critical(), capStats.critical(), binderStats.critical());

        return new WandStats(Math.max(MIN_STAT_VALUE, power), Math.max(MIN_STAT_VALUE, stability), Math.max(MIN_STAT_VALUE, durability), Math.max(MIN_CRITICAL_VALUE, critical), determineAffinity(handleStats, binderStats, capStats));
    }

    private static float calculateFinalStat(float base, float handle, float cap, float binder) {
        float totalAdjustment = HANDLE_WEIGHT * (handle - 1.0f) + CAP_WEIGHT * (cap - 1.0f) + BINDER_WEIGHT * (binder - 1.0f);

        totalAdjustment = Math.max(MIN_ADJUSTMENT, Math.min(MAX_ADJUSTMENT, totalAdjustment));
        return base * (1.0f + totalAdjustment);
    }

    private static String determineAffinity(WandMaterialStats handleStats, WandMaterialStats binderStats, WandMaterialStats capStats) {
        String primaryAffinity = binderStats.affinity();

        if ("neutral".equals(primaryAffinity)) {
            if (!"neutral".equals(handleStats.affinity())) {
                return handleStats.affinity();
            }
            if (!"neutral".equals(capStats.affinity())) {
                return capStats.affinity();
            }
        }

        return primaryAffinity;
    }

    private static WandMaterialStats getMaterialStats(String materialName, String partType) {
        Map<String, WandMaterialData> materials = switch (partType) {
            case "handle" -> Config.getHandleMaterials();
            case "binder" -> Config.getBinderMaterials();
            case "cap" -> Config.getCapMaterials();
            default -> new HashMap<>();
        };

        WandMaterialData materialData = materials.get(materialName);
        return materialData != null ? materialData.stats() : null;
    }

    private static void storeStats(ItemStack wandStack, WandStats stats) {
        CompoundTag tag = getOrCreateWandTag(wandStack);
        CompoundTag statsTag = new CompoundTag();

        statsTag.putFloat("power", stats.power());
        statsTag.putFloat("stability", stats.stability());
        statsTag.putFloat("durability", stats.durability());
        statsTag.putFloat("critical", stats.critical());
        statsTag.putString("affinity", stats.affinity());

        tag.put(WAND_STATS_KEY, statsTag);
        setWandTag(wandStack, tag);
    }

    public static WandStats getWandStats(ItemStack wandStack) {
        CompoundTag tag = getWandTag(wandStack);
        if (tag == null || !tag.contains(WAND_STATS_KEY)) {
            return null;
        }

        CompoundTag statsTag = tag.getCompound(WAND_STATS_KEY);
        return new WandStats(statsTag.getFloat("power"), statsTag.getFloat("stability"), statsTag.getFloat("durability"), statsTag.getFloat("critical"), statsTag.getString("affinity"));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        ensureWandHasDefaultParts(stack);

        WandParts parts = getWandParts(stack);
        WandStats stats = getWandStats(stack);

        if (parts != null) {
            tooltipComponents.add(Component.literal("Parts:").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.literal("  Handle: " + formatMaterialName(parts.handle())).withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.literal("  Binder: " + formatMaterialName(parts.binder())).withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.literal("  Cap: " + formatMaterialName(parts.cap())).withStyle(ChatFormatting.GRAY));
        }

        if (stats != null) {
            tooltipComponents.add(Component.empty());
            tooltipComponents.add(Component.literal("Stats:").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(formatStatTooltip("Power", stats.power()));
            tooltipComponents.add(formatStatTooltip("Stability", stats.stability()));
            tooltipComponents.add(formatStatTooltip("Durability", stats.durability()));
            tooltipComponents.add(formatStatTooltip("Critical", stats.critical()));

            if (!"neutral".equals(stats.affinity())) {
                tooltipComponents.add(Component.literal("  Affinity: " + formatMaterialName(stats.affinity())).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }

    private String formatMaterialName(String materialName) {
        return materialName.substring(0, 1).toUpperCase() + materialName.substring(1).replace("_", " ");
    }

    private Component formatStatTooltip(String statName, float value) {
        ChatFormatting color;
        String displayValue;

        if ("critical".equals(statName.toLowerCase())) {
            color = value > 0.1f ? ChatFormatting.GREEN : value < 0.1f ? ChatFormatting.RED : ChatFormatting.WHITE;
            displayValue = String.format("%.1f%%", value * 100);
        } else {
            color = value > 1.0f ? ChatFormatting.GREEN : value < 1.0f ? ChatFormatting.RED : ChatFormatting.WHITE;
            displayValue = String.format("%.0f%%", value * 100);
        }

        return Component.literal("  " + statName + ": " + displayValue).withStyle(color);
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack wandStack = player.getItemInHand(hand);
        ensureWandHasDefaultParts(wandStack);

        if (getCurrentSpell(wandStack, level.registryAccess()).isEmpty()) {
            showMessage(player, "No spell bound to this wand!");
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    public void openSpellRadialMenuFromClient(ItemStack wandStack, HolderLookup.Provider registryAccess) {
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            com.soul.goe.client.ClientHelper.openSpellRadialMenu(this, wandStack, registryAccess);
        }
    }

    public int getCurrentSpellIndex(ItemStack wandStack) {
        CompoundTag tag = getWandTag(wandStack);
        return tag != null && tag.contains(CURRENT_SPELL_KEY) ? tag.getInt(CURRENT_SPELL_KEY) : 0;
    }

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
        int newIndex = forward ? (current + 1) % totalSpells : (current - 1 + totalSpells) % totalSpells;

        tag.putInt(CURRENT_SPELL_KEY, newIndex);
        setWandTag(wandStack, tag);
    }

    public ItemStack getCurrentSpell(ItemStack wandStack, HolderLookup.Provider registryAccess) {
        CompoundTag tag = getWandTag(wandStack);
        if (tag == null || !tag.contains(CURRENT_SPELL_KEY)) {
            return ItemStack.EMPTY;
        }

        int currentIndex = tag.getInt(CURRENT_SPELL_KEY);
        if (!tag.contains(BOUND_SPELLS_KEY)) {
            return ItemStack.EMPTY;
        }

        ListTag spells = tag.getList(BOUND_SPELLS_KEY, Tag.TAG_COMPOUND);
        if (!spells.isEmpty() && currentIndex >= 0 && currentIndex < spells.size()) {
            CompoundTag spellTag = spells.getCompound(currentIndex);
            return ItemStack.parseOptional(registryAccess, spellTag);
        }
        return ItemStack.EMPTY;
    }

    public boolean addSpell(ItemStack wandStack, ItemStack spellStack, HolderLookup.Provider registryAccess) {
        CompoundTag tag = getOrCreateWandTag(wandStack);
        ListTag spells = tag.contains(BOUND_SPELLS_KEY) ? tag.getList(BOUND_SPELLS_KEY, Tag.TAG_COMPOUND) : new ListTag();

        for (int i = 0; i < spells.size(); i++) {
            if (spells.getCompound(i).isEmpty()) {
                spells.set(i, (CompoundTag) spellStack.save(registryAccess));
                tag.put(BOUND_SPELLS_KEY, spells);
                setWandTag(wandStack, tag);
                return true;
            }
        }

        if (spells.size() < getMaxSpells(wandStack)) {
            spells.add((CompoundTag) spellStack.save(registryAccess));
            tag.put(BOUND_SPELLS_KEY, spells);
            setWandTag(wandStack, tag);
            return true;
        }

        return false;
    }

    public NonNullList<ItemStack> getBoundSpells(ItemStack wandStack, HolderLookup.Provider lookupProvider) {
        NonNullList<ItemStack> spells = NonNullList.withSize(getMaxSpells(wandStack), ItemStack.EMPTY);
        CompoundTag tag = getWandTag(wandStack);

        if (tag == null || !tag.contains(BOUND_SPELLS_KEY)) {
            return spells;
        }

        ListTag spellList = tag.getList(BOUND_SPELLS_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(spellList.size(), getMaxSpells(wandStack)); i++) {
            CompoundTag spellTag = spellList.getCompound(i);
            if (lookupProvider != null) {
                spells.set(i, ItemStack.parseOptional(lookupProvider, spellTag));
            }
        }

        return spells;
    }

    private static CompoundTag getOrCreateWandTag(ItemStack wandStack) {
        CompoundTag tag = getWandTag(wandStack);
        if (tag == null || tag.isEmpty()) {
            tag = new CompoundTag();
            tag.putInt(CURRENT_SPELL_KEY, 0);
            setWandTag(wandStack, tag);
        }
        return tag;
    }

    private static CompoundTag getWandTag(ItemStack wandStack) {
        CustomData existingData = wandStack.get(DataComponents.CUSTOM_DATA);
        return existingData != null ? existingData.copyTag() : new CompoundTag();
    }

    private static void setWandTag(ItemStack wandStack, CompoundTag tag) {
        wandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        WandStats wandStats = getWandStats(stack);
        if (wandStats == null) {
            playFailureEffects(level, player);
            showMessage(player, "This wand is missing parts!");
            return false;
        }

        float totalStats = wandStats.power() + wandStats.stability() + wandStats.durability() + wandStats.critical();
        int requiredCastTime = Math.round(totalStats * 20);
        int useDuration = getUseDuration(stack, entity) - timeLeft;

        if (useDuration < requiredCastTime) {
            playFailureEffects(level, player);
            return false;
        }

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
            playFailureEffects(level, player);
            showMessage(player, "No spell bound to this wand!");
            return false;
        }

        if (!(spellStack.getItem() instanceof Spell spellItem)) {
            playFailureEffects(level, player);
            return false;
        }

        Optional<SpellData> spellDataOpt = spellItem.getSpellData();
        if (spellDataOpt.isEmpty()) {
            playFailureEffects(level, player);
            return false;
        }

        SpellData spellData = spellDataOpt.get();

        if (!spellData.canCast(player)) {
            playFailureEffects(level, player);
            if (!level.isClientSide()) {
                showDetailedIngredientList(player, spellData);
            }
            return false;
        }

        if (wandStack.getMaxDamage() > 1 && wandStack.getDamageValue() >= wandStack.getMaxDamage()) {
            playFailureEffects(level, player);
            showMessage(player, "Your wand is too damaged to cast spells!");
            return false;
        }

        WandStats wandStats = getWandStats(wandStack);
        if (wandStats == null) {
            playFailureEffects(level, player);
            showMessage(player, "This wand is missing parts!");
            return false;
        }

        float backfireChance;
        if (wandStats.stability() >= 2.0f) {
            backfireChance = 0.0f;
        } else {
            // Scale from 45% backfire at 0.1 stability down to 2.5% at 1.9 stability
            backfireChance = Math.max(0.0f, (2.0f - wandStats.stability()) / 2.0f * 0.5f);
        }

        if (level.random.nextFloat() < backfireChance) {
            return handleSpellBackfire(level, player, wandStack, spellData, wandStats);
        }

        playSuccessEffects(level, player);
        spellData.cast(level, player);
        applyWandDurabilityDamage(wandStack, level, player, 1);

        return true;
    }

    private boolean handleSpellBackfire(Level level, Player player, ItemStack wandStack, SpellData spellData, WandStats wandStats) {
        // Roll for each effect independently
        boolean fizzle = level.random.nextFloat() < 0.8f;  // 80% chance to fizzle
        boolean harmPlayer = level.random.nextFloat() < 0.2f;  // 20% chance to harm player
        boolean damageWand = level.random.nextFloat() < 0.25f; // 25% chance to damage wand

        // Ensure at least one effect happens
        if (!fizzle && !harmPlayer && !damageWand) {
            fizzle = true;
        }

        playFailureEffects(level, player);

        List<String> effects = new ArrayList<>();

        if (fizzle) {
            effects.add("fizzled");
        }

        if (harmPlayer) {
            effects.add("harmed you");
            // Scale damage based on power (1-10 damage range)
            int baseDamage = Math.max(1, Math.min(10, Math.round(wandStats.power() * 3.0f)));

            if (!level.isClientSide()) {
                player.hurt(player.damageSources().magic(), baseDamage);
            }
        }

        if (damageWand) {
            effects.add("damaged your wand");
            // Scale wand damage based on power (2-6 extra damage)
            int extraDamage = Math.max(2, Math.min(6, Math.round(wandStats.power() * 2.0f)));
            applyWandDurabilityDamage(wandStack, level, player, extraDamage);
        }

        String backfireMessage = "Your spell backfired";
        if (!effects.isEmpty()) {
            backfireMessage += " and " + String.join(", ", effects) + "!";
        } else {
            backfireMessage += "!";
        }

        showMessage(player, backfireMessage);

        // Always apply base wand damage for the failed attempt
        if (!damageWand) {
            applyWandDurabilityDamage(wandStack, level, player, 1);
        }

        return false;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) return;

        WandStats wandStats = getWandStats(stack);
        if (wandStats == null) return;

        float totalStats = wandStats.power() + wandStats.stability() + wandStats.durability() + wandStats.critical();
        int requiredCastTime = Math.round(totalStats * 20);
        int useDuration = getUseDuration(stack, entity) - remainingUseDuration;

        // Play sound exactly when we reach required cast time
        if (useDuration == requiredCastTime) {
            playChargeCompleteSound(level, player);
        }
    }

    private void playSuccessEffects(Level level, Player player) {
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH + 0.2F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }

    private void playFailureEffects(Level level, Player player) {
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
            ItemStack displayStack = new ItemStack(item, requiredAmount);
            MutableComponent numberComponent = Component.literal(String.valueOf(requiredAmount)).withStyle(numberColor);
            MutableComponent itemComponent = Component.empty().append(displayStack.getHoverName()).withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(displayStack))).withColor(hasEnough ? ChatFormatting.GREEN : ChatFormatting.RED));

            MutableComponent fullComponent = numberComponent.append(Component.literal(" × ").withStyle(ChatFormatting.WHITE)).append(itemComponent);

            ingredientComponents.add(fullComponent);
        });

        if (!ingredientComponents.isEmpty()) {
            MutableComponent message = Component.literal("\"" + spellName + "\"").withStyle(ChatFormatting.AQUA).append(Component.literal(" requires: ").withStyle(ChatFormatting.YELLOW));

            for (int i = 0; i < ingredientComponents.size(); i++) {
                message = message.append(ingredientComponents.get(i));
                if (i < ingredientComponents.size() - 1) {
                    message = message.append(Component.literal(", ").withStyle(ChatFormatting.WHITE));
                }
            }

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
        float ratio = Math.max(0.0F, ((float) stack.getMaxDamage() - (float) stack.getDamageValue()) / (float) stack.getMaxDamage());
        return net.minecraft.util.Mth.hsvToRgb(ratio / 3.0F, 1.0F, 1.0F);
    }

    public static void registerCatalysts() {
        WAND_CATALYSTS.clear();
        Map<Block, ItemStack> configCatalysts = Config.getWandCatalysts();
        WAND_CATALYSTS.putAll(configCatalysts);

        if (WAND_CATALYSTS.isEmpty()) {
            Goe.LOGGER.warn("No wand catalysts were registered!");
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        ItemStack wandStack = context.getItemInHand();
        ItemStack offHandItem = player.getOffhandItem();

        ensureWandHasDefaultParts(wandStack);

        if (isWandPart(offHandItem)) {
            return handleWandPartAssembly(context, level, player, offHandItem);
        }

        if (offHandItem.getItem() instanceof Spell) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        return handleWandCrafting(context, level);
    }

    private boolean isWandPart(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();

        return Config.getMaterialNameFromItem(item, "handle") != null || Config.getMaterialNameFromItem(item, "binder") != null || Config.getMaterialNameFromItem(item, "cap") != null;
    }

    private InteractionResult handleWandPartAssembly(UseOnContext context, Level level, Player player, ItemStack partStack) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack wandStack = context.getItemInHand();
        Item partItem = partStack.getItem();

        if (wandStack.isDamaged()) {
            showMessage(player, "Wand must be repaired before modifying parts!");
            return InteractionResult.FAIL;
        }

        String partType = getPartType(partItem);
        if (partType == null) {
            showMessage(player, "This item cannot be used as a wand part!");
            return InteractionResult.FAIL;
        }

        String materialName = Config.getMaterialNameFromItem(partItem, partType);
        WandParts currentParts = getWandParts(wandStack);

        WandParts newParts = switch (partType) {
            case "handle" ->
                    new WandParts(materialName, currentParts != null ? currentParts.binder() : "", currentParts != null ? currentParts.cap() : "");
            case "binder" ->
                    new WandParts(currentParts != null ? currentParts.handle() : "", materialName, currentParts != null ? currentParts.cap() : "");
            case "cap" ->
                    new WandParts(currentParts != null ? currentParts.handle() : "", currentParts != null ? currentParts.binder() : "", materialName);
            default -> currentParts;
        };

        if (newParts != null && !newParts.handle().isEmpty() && !newParts.binder().isEmpty() && !newParts.cap().isEmpty()) {
            setWandParts(wandStack, newParts.handle(), newParts.binder(), newParts.cap());
        }

        if (!player.isCreative()) {
            partStack.shrink(1);
        }

        showMessage(player, "Wand " + partType + " replaced with " + formatMaterialName(materialName) + "!");
        playSuccessEffects(level, player);

        return InteractionResult.SUCCESS;
    }

    private String getPartType(Item item) {
        if (Config.getHandleMaterials().values().stream().anyMatch(data -> data.item().equals(item))) {
            return "handle";
        }
        if (Config.getBinderMaterials().values().stream().anyMatch(data -> data.item().equals(item))) {
            return "binder";
        }
        if (Config.getCapMaterials().values().stream().anyMatch(data -> data.item().equals(item))) {
            return "cap";
        }
        return null;
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
        applyWandDurabilityDamage(wandStack, level, player, 1);
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

    public static void applyWandDurabilityDamage(ItemStack wandStack, Level level, Player player, int baseDamage) {
        WandStats stats = getWandStats(wandStack);
        if (stats == null) {
            if (level instanceof ServerLevel serverLevel) {
                wandStack.hurtAndBreak(baseDamage, serverLevel, player, item -> player.displayClientMessage(Component.literal("Your wand has broken!"), true));
            }
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            int adjustedDamage;

            if (stats.durability() < 1.0f) {
                float extraDamageChance = 1.0f - stats.durability();
                if (level.random.nextFloat() < extraDamageChance) {
                    float damageMultiplier = 1.0f + (1.0f - stats.durability());
                    adjustedDamage = Math.max(1, Math.round(baseDamage * damageMultiplier));
                } else {
                    adjustedDamage = baseDamage;
                }
            } else {
                float noDamageChance = (stats.durability() - 1.0f) * 0.5f;
                adjustedDamage = level.random.nextFloat() >= noDamageChance ? baseDamage : 0;
            }

            if (adjustedDamage > 0) {
                wandStack.hurtAndBreak(adjustedDamage, serverLevel, player, item -> player.displayClientMessage(Component.literal("Your wand has broken!"), true));
            }
        }
    }
}