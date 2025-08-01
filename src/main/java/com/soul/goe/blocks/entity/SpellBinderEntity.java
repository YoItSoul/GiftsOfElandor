package com.soul.goe.blocks.entity;

import com.soul.goe.items.custom.Spell;
import com.soul.goe.items.custom.Wand;
import com.soul.goe.menus.SpellBinderMenu;
import com.soul.goe.registry.ModBlockEntities;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class SpellBinderEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventory;
    private static final int WAND_SLOT = 0;
    private static final int FIRST_SPELL_SLOT = 1;
    private static final int MAX_SPELL_SLOTS = 9;
    private boolean isLoadingSpells = false;

    public SpellBinderEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPELL_BINDER.get(), pos, state);
        this.inventory = new ItemStackHandler(1 + MAX_SPELL_SLOTS) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot == WAND_SLOT) {
                    return stack.getItem() instanceof Wand;
                } else if (slot >= FIRST_SPELL_SLOT && slot < FIRST_SPELL_SLOT + MAX_SPELL_SLOTS) {
                    return stack.getItem() instanceof Spell;
                }
                return false;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (isLoadingSpells) {
                    return;
                }

                if (slot == WAND_SLOT) {
                    handleWandChange();
                } else if (slot >= FIRST_SPELL_SLOT) {
                    updateWandSpells();
                }
            }
        };
    }

    private void handleWandChange() {
        ItemStack wandStack = inventory.getStackInSlot(WAND_SLOT);

        if (!wandStack.isEmpty() && wandStack.getItem() instanceof Wand) {
            loadSpellsFromWand(wandStack);
        } else {
            clearSpellSlots();
        }
    }

    private void loadSpellsFromWand(ItemStack wandStack) {
        isLoadingSpells = true;
        clearSpellSlots();

        CustomData customData = wandStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag wandTag = customData.copyTag();
            if (wandTag.contains("BoundSpells")) {
                ListTag spellList = wandTag.getList("BoundSpells", Tag.TAG_COMPOUND);

                for (int i = 0; i < Math.min(spellList.size(), getMaxAvailableSlots(wandStack)); i++) {
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
        isLoadingSpells = false;
    }

    private void clearSpellSlots() {
        for (int i = 0; i < MAX_SPELL_SLOTS; i++) {
            inventory.setStackInSlot(FIRST_SPELL_SLOT + i, ItemStack.EMPTY);
        }
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
        int maxSlots = getMaxAvailableSlots(wandStack);

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

    public int getMaxAvailableSlots(ItemStack wandStack) {
        if (wandStack.isEmpty() || !(wandStack.getItem() instanceof Wand wand)) {
            return 0;
        }
        return wand.getMaxSpells();
    }

    public int getCurrentAvailableSlots() {
        ItemStack wandStack = inventory.getStackInSlot(WAND_SLOT);
        return getMaxAvailableSlots(wandStack);
    }

    public IItemHandler getInventory() {
        return inventory;
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
        clearSpellSlots();
    }
}