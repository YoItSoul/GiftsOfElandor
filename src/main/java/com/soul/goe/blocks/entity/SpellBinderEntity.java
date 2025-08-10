package com.soul.goe.blocks.entity;

import com.soul.goe.Config;
import com.soul.goe.items.custom.Spell;
import com.soul.goe.items.custom.Wand;
import com.soul.goe.client.menus.SpellBinderMenu;
import com.soul.goe.registry.ModBlockEntities;
import com.soul.goe.registry.ModItems;
import com.soul.goe.util.wands.WandMaterialData;
import com.soul.goe.util.wands.WandMaterialStats;
import com.soul.goe.util.wands.WandParts;
import com.soul.goe.util.wands.WandStats;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.Map;

public class SpellBinderEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventory;

    private static final int WAND_SLOT = 0;
    private static final int HANDLE_SLOT = 1;
    private static final int BINDER_SLOT = 2;
    private static final int CAP_SLOT = 3;
    private static final int FIRST_SPELL_SLOT = 4;
    private static final int MAX_SPELL_SLOTS = 9;
    private static final int TOTAL_SLOTS = 1 + 3 + MAX_SPELL_SLOTS;

    private boolean isLoadingFromWand = false;

    public SpellBinderEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPELL_BINDER.get(), pos, state);
        this.inventory = new ItemStackHandler(TOTAL_SLOTS) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot == WAND_SLOT) {
                    return stack.getItem() instanceof Wand;
                } else if (slot == HANDLE_SLOT) {
                    return isValidWandPart(stack, "handle");
                } else if (slot == BINDER_SLOT) {
                    return isValidWandPart(stack, "binder");
                } else if (slot == CAP_SLOT) {
                    return isValidWandPart(stack, "cap");
                } else if (slot >= FIRST_SPELL_SLOT && slot < FIRST_SPELL_SLOT + MAX_SPELL_SLOTS) {
                    return stack.getItem() instanceof Spell;
                }
                return false;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (isLoadingFromWand) {
                    return;
                }

                if (slot == WAND_SLOT) {
                    handleWandChange();
                } else if (slot >= HANDLE_SLOT && slot <= CAP_SLOT) {
                    updateWandParts();
                } else if (slot >= FIRST_SPELL_SLOT) {
                    updateWandSpells();
                }
            }
        };
    }

    private boolean isValidWandPart(ItemStack stack, String partType) {
        if (stack.isEmpty()) return true;

        Item item = stack.getItem();
        String materialName = Config.getMaterialNameFromItem(item, partType);
        return materialName != null;
    }

    private void handleWandChange() {
        ItemStack wandStack = inventory.getStackInSlot(WAND_SLOT);

        if (!wandStack.isEmpty() && wandStack.getItem() instanceof Wand) {
            Wand.ensureWandHasDefaultParts(wandStack);
            loadFromWand(wandStack);
        } else {
            clearAllSlots();
        }
    }

    private void loadFromWand(ItemStack wandStack) {
        isLoadingFromWand = true;
        clearAllSlots();

        WandParts parts = Wand.getWandParts(wandStack);
        if (parts != null) {
            setPartFromMaterialName(HANDLE_SLOT, parts.handle(), "handle");
            setPartFromMaterialName(BINDER_SLOT, parts.binder(), "binder");
            setPartFromMaterialName(CAP_SLOT, parts.cap(), "cap");
        }

        CustomData customData = wandStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag wandTag = customData.copyTag();
            if (wandTag.contains("BoundSpells")) {
                ListTag spellList = wandTag.getList("BoundSpells", Tag.TAG_COMPOUND);

                for (int i = 0; i < Math.min(spellList.size(), getMaxAvailableSpells(wandStack)); i++) {
                    CompoundTag spellTag = spellList.getCompound(i);
                    if (!spellTag.isEmpty()) {
                        ItemStack spellStack = ItemStack.parseOptional(level.registryAccess(), spellTag);
                        if (!spellStack.isEmpty()) {
                            inventory.setStackInSlot(FIRST_SPELL_SLOT + i, spellStack);
                        }
                    }
                }
            }
        }
        isLoadingFromWand = false;
    }

    private void setPartFromMaterialName(int slot, String materialName, String partType) {
        if (materialName == null || materialName.isEmpty()) {
            return;
        }

        var materials = switch (partType) {
            case "handle" -> Config.getHandleMaterials();
            case "binder" -> Config.getBinderMaterials();
            case "cap" -> Config.getCapMaterials();
            default -> throw new IllegalArgumentException("Invalid part type: " + partType);
        };

        var materialData = materials.get(materialName);
        if (materialData != null) {
            inventory.setStackInSlot(slot, new ItemStack(materialData.item()));
        }
    }

    private void clearAllSlots() {
        inventory.setStackInSlot(HANDLE_SLOT, ItemStack.EMPTY);
        inventory.setStackInSlot(BINDER_SLOT, ItemStack.EMPTY);
        inventory.setStackInSlot(CAP_SLOT, ItemStack.EMPTY);
        for (int i = 0; i < MAX_SPELL_SLOTS; i++) {
            inventory.setStackInSlot(FIRST_SPELL_SLOT + i, ItemStack.EMPTY);
        }
    }

    private void updateWandParts() {
        ItemStack wandStack = inventory.getStackInSlot(WAND_SLOT);

        String handleMaterial = getMaterialFromSlot(HANDLE_SLOT, "handle");
        String binderMaterial = getMaterialFromSlot(BINDER_SLOT, "binder");
        String capMaterial = getMaterialFromSlot(CAP_SLOT, "cap");

        if (wandStack.isEmpty()) {
            if (handleMaterial != null && binderMaterial != null && capMaterial != null) {
                createWandFromParts(handleMaterial, binderMaterial, capMaterial);
            }
            return;
        }

        if (!(wandStack.getItem() instanceof Wand) || isWandDamaged(wandStack)) {
            return;
        }

        if (handleMaterial != null && binderMaterial != null && capMaterial != null) {
            Wand.setWandParts(wandStack, handleMaterial, binderMaterial, capMaterial);
        } else {
            clearWandParts(wandStack);
        }
    }

    private void createWandFromParts(String handleMaterial, String binderMaterial, String capMaterial) {
        if (level == null || level.isClientSide()) {
            return;
        }

        ItemStack newWand = createDefaultWand();

        if (!newWand.isEmpty()) {
            Wand.setWandParts(newWand, handleMaterial, binderMaterial, capMaterial);
            inventory.setStackInSlot(WAND_SLOT, newWand);
        }
    }

    private ItemStack createDefaultWand() {
        return new ItemStack(ModItems.WAND.get());
    }

    private void dropItemAtBlock(ItemStack stack) {
        if (level == null || level.isClientSide() || stack.isEmpty()) {
            return;
        }

        double x = getBlockPos().getX() + 0.5;
        double y = getBlockPos().getY() + 0.5;
        double z = getBlockPos().getZ() + 0.5;

        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(level, x, y, z, stack);
        level.addFreshEntity(itemEntity);
    }

    private void clearWandParts(ItemStack wandStack) {
        CustomData existingData = wandStack.get(DataComponents.CUSTOM_DATA);
        CompoundTag wandTag = new CompoundTag();

        if (existingData != null) {
            wandTag = existingData.copyTag();
        }

        wandTag.remove("WandParts");
        wandTag.remove("WandStats");

        wandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(wandTag));
    }

    private String getMaterialFromSlot(int slot, String partType) {
        ItemStack partStack = inventory.getStackInSlot(slot);
        if (partStack.isEmpty()) {
            return null;
        }

        return Config.getMaterialNameFromItem(partStack.getItem(), partType);
    }

    private void updateWandSpells() {
        ItemStack wandStack = inventory.getStackInSlot(WAND_SLOT);
        if (wandStack.isEmpty() || !(wandStack.getItem() instanceof Wand wand)) {
            return;
        }

        CompoundTag wandTag = new CompoundTag();
        CustomData existingData = wandStack.get(DataComponents.CUSTOM_DATA);
        if (existingData != null) {
            wandTag = existingData.copyTag();
        }

        ListTag spellList = new ListTag();
        int maxSlots = getMaxAvailableSpells(wandStack);

        for (int i = 0; i < maxSlots; i++) {
            ItemStack spellStack = inventory.getStackInSlot(FIRST_SPELL_SLOT + i);
            if (!spellStack.isEmpty()) {
                CompoundTag spellTag = (CompoundTag) spellStack.save(level.registryAccess());
                spellList.add(spellTag);
            }
        }

        wandTag.put("BoundSpells", spellList);
        wandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(wandTag));
    }

    public boolean isWandDamaged() {
        ItemStack wandStack = inventory.getStackInSlot(WAND_SLOT);
        return isWandDamaged(wandStack);
    }

    private boolean isWandDamaged(ItemStack wandStack) {
        return wandStack.isDamaged();
    }

    public boolean canModifyParts() {
        ItemStack wandStack = inventory.getStackInSlot(WAND_SLOT);

        if (wandStack.isEmpty()) {
            return true;
        }

        if (!(wandStack.getItem() instanceof Wand) || isWandDamaged(wandStack)) {
            return false;
        }

        return !wandHasSpells(wandStack);
    }

    public boolean wandHasSpells() {
        ItemStack wandStack = inventory.getStackInSlot(WAND_SLOT);
        return wandHasSpells(wandStack);
    }

    private boolean wandHasSpells(ItemStack wandStack) {
        CustomData customData = wandStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }

        CompoundTag wandTag = customData.copyTag();
        if (!wandTag.contains("BoundSpells")) {
            return false;
        }

        ListTag spellList = wandTag.getList("BoundSpells", Tag.TAG_COMPOUND);

        for (int i = 0; i < spellList.size(); i++) {
            CompoundTag spellTag = spellList.getCompound(i);
            if (!spellTag.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public int getMaxAvailableSpells(ItemStack wandStack) {
        if (wandStack.isEmpty() || !(wandStack.getItem() instanceof Wand wand)) {
            return 0;
        }
        return wand.getMaxSpells(wandStack); // Pass the wandStack argument
    }

    public int getCurrentAvailableSpells() {
        ItemStack wandStack = inventory.getStackInSlot(WAND_SLOT);
        return getMaxAvailableSpells(wandStack);
    }

    public boolean hasWand() {
        return !inventory.getStackInSlot(WAND_SLOT).isEmpty();
    }

    public WandStats getCurrentWandStats() {
        ItemStack wandStack = inventory.getStackInSlot(WAND_SLOT);
        if (!wandStack.isEmpty() && wandStack.getItem() instanceof Wand) {
            return Wand.getWandStats(wandStack);
        }

        String handleMaterial = getMaterialFromSlot(HANDLE_SLOT, "handle");
        String binderMaterial = getMaterialFromSlot(BINDER_SLOT, "binder");
        String capMaterial = getMaterialFromSlot(CAP_SLOT, "cap");

        if (handleMaterial != null && binderMaterial != null && capMaterial != null) {
            return calculatePreviewStats(handleMaterial, binderMaterial, capMaterial);
        }

        return null;
    }

    private WandStats calculatePreviewStats(String handleMaterial, String binderMaterial, String capMaterial) {
        WandMaterialStats handleStats = getMaterialStats(handleMaterial, "handle");
        WandMaterialStats binderStats = getMaterialStats(binderMaterial, "binder");
        WandMaterialStats capStats = getMaterialStats(capMaterial, "cap");

        if (handleStats == null || binderStats == null || capStats == null) {
            return null;
        }

        WandStats baseStats = Config.getBaseWandStats();

        // Use additive modifiers with weighted contributions by part type
        // Convert multipliers to additive bonuses: 1.2 becomes +0.2, 0.8 becomes -0.2

        float powerBonus = calculateAdditiveBonus(handleStats.power(), 0.1f) +        // Handles: 10%
                calculateAdditiveBonus(binderStats.power(), 0.6f) +        // Binders: 60%
                calculateAdditiveBonus(capStats.power(), 0.3f);            // Caps: 30%

        float stabilityBonus = calculateAdditiveBonus(handleStats.stability(), 0.4f) +  // Handles: 40%
                calculateAdditiveBonus(binderStats.stability(), 0.3f) +  // Binders: 30%
                calculateAdditiveBonus(capStats.stability(), 0.3f);      // Caps: 30%

        float durabilityBonus = calculateAdditiveBonus(handleStats.durability(), 0.5f) + // Handles: 50%
                calculateAdditiveBonus(binderStats.durability(), 0.2f) +  // Binders: 20%
                calculateAdditiveBonus(capStats.durability(), 0.3f);      // Caps: 30%

        float criticalBonus = calculateAdditiveBonus(handleStats.critical(), 0.2f) +     // Handles: 20%
                calculateAdditiveBonus(binderStats.critical(), 0.5f) +     // Binders: 50%
                calculateAdditiveBonus(capStats.critical(), 0.3f);         // Caps: 30%

        // Apply bonuses to base stats with reasonable limits
        return new WandStats(Math.max(0.1f, baseStats.power() * (1.0f + powerBonus)), Math.max(0.1f, baseStats.stability() * (1.0f + stabilityBonus)), Math.max(0.1f, baseStats.durability() * (1.0f + durabilityBonus)), Math.max(0.0f, baseStats.critical() * (1.0f + criticalBonus)), determineAffinity(handleStats, binderStats, capStats));
    }

    private float calculateAdditiveBonus(float multiplier, float weight) {
        return (multiplier - 1.0f) * weight;
    }

    private String determineAffinity(WandMaterialStats handleStats, WandMaterialStats binderStats, WandMaterialStats capStats) {
        String primaryAffinity = binderStats.affinity();

        if (primaryAffinity.equals("neutral")) {
            if (!handleStats.affinity().equals("neutral")) {
                return handleStats.affinity();
            }
            if (!capStats.affinity().equals("neutral")) {
                return capStats.affinity();
            }
        }

        return primaryAffinity;
    }

    private WandMaterialStats getMaterialStats(String materialName, String partType) {
        Map<String, WandMaterialData> materials = switch (partType) {
            case "handle" -> Config.getHandleMaterials();
            case "binder" -> Config.getBinderMaterials();
            case "cap" -> Config.getCapMaterials();
            default -> new HashMap<>();
        };

        WandMaterialData materialData = materials.get(materialName);
        if (materialData != null) {
            return materialData.stats();
        }

        return null;
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    public boolean isWandSlot(int slot) {
        return slot == WAND_SLOT;
    }

    public boolean isPartSlot(int slot) {
        return slot >= HANDLE_SLOT && slot <= CAP_SLOT;
    }

    public boolean isSpellSlot(int slot) {
        return slot >= FIRST_SPELL_SLOT && slot < FIRST_SPELL_SLOT + MAX_SPELL_SLOTS;
    }

    public String getPartType(int slot) {
        return switch (slot) {
            case HANDLE_SLOT -> "handle";
            case BINDER_SLOT -> "binder";
            case CAP_SLOT -> "cap";
            default -> "unknown";
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.goe.spell_binder");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return new SpellBinderMenu(windowId, playerInv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
    }

    public void onWandRemoved() {
        updateWandSpells();
        updateWandParts();

        ItemStack wandStack = inventory.getStackInSlot(WAND_SLOT);
        if (!wandStack.isEmpty() && level != null && !level.isClientSide()) {
            String handleMaterial = getMaterialFromSlot(HANDLE_SLOT, "handle");
            String binderMaterial = getMaterialFromSlot(BINDER_SLOT, "binder");
            String capMaterial = getMaterialFromSlot(CAP_SLOT, "cap");

            if (handleMaterial != null && binderMaterial != null && capMaterial != null) {
                Wand.setWandParts(wandStack, handleMaterial, binderMaterial, capMaterial);
                dropItemAtBlock(wandStack);
            } else {
                if (handleMaterial != null) {
                    dropItemAtBlock(inventory.getStackInSlot(HANDLE_SLOT));
                }
                if (binderMaterial != null) {
                    dropItemAtBlock(inventory.getStackInSlot(BINDER_SLOT));
                }
                if (capMaterial != null) {
                    dropItemAtBlock(inventory.getStackInSlot(CAP_SLOT));
                }
            }

            inventory.setStackInSlot(WAND_SLOT, ItemStack.EMPTY);
        }

        clearAllSlots();
    }
}