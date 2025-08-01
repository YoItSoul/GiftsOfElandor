package com.soul.goe.menus;

import com.soul.goe.blocks.entity.SpellBinderEntity;
import com.soul.goe.items.custom.Spell;
import com.soul.goe.items.custom.Wand;
import com.soul.goe.registry.ModBlocks;
import com.soul.goe.registry.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SpellBinderMenu extends AbstractContainerMenu {
    private final SpellBinderEntity blockEntity;
    private final ContainerLevelAccess access;

    public SpellBinderMenu(int windowId, Inventory playerInventory, SpellBinderEntity entity) {
        super(ModMenuTypes.SPELL_BINDER.get(), windowId);
        this.blockEntity = entity;
        this.access = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());

        addSlot(new WandSlot(blockEntity.getInventory(), 0, 81, 27));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col + 1;
                addSlot(new SpellSlot(blockEntity.getInventory(), index, 63 + col * 18, 50 + row * 18, this));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 9 + col * 18, 139 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 9 + col * 18, 197));
        }
    }

    public SpellBinderMenu(int windowId, Inventory playerInventory, BlockEntity entity) {
        this(windowId, playerInventory, (SpellBinderEntity) entity);
    }

    private static class WandSlot extends SlotItemHandler {
        public WandSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof Wand;
        }
    }

    private static class SpellSlot extends SlotItemHandler {
        private final SpellBinderMenu menu;

        public SpellSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, SpellBinderMenu menu) {
            super(itemHandler, index, xPosition, yPosition);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof Spell && isActive();
        }

        @Override
        public boolean isActive() {
            ItemStack wandStack = menu.blockEntity.getInventory().getStackInSlot(0);
            if (wandStack.getItem() instanceof Wand) {
                int spellSlotIndex = getSlotIndex() - 1;
                return spellSlotIndex < menu.blockEntity.getCurrentAvailableSlots();
            }
            return false;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < 10) {
                if (!moveItemStackTo(slotStack, 10, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotStack.getItem() instanceof Wand) {
                    if (!moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotStack.getItem() instanceof Spell) {
                    ItemStack wandStack = this.blockEntity.getInventory().getStackInSlot(0);
                    if (wandStack.getItem() instanceof Wand) {
                        boolean moved = false;
                        for (int i = 1; i < 10; i++) {
                            Slot spellSlot = slots.get(i);
                            if (spellSlot.isActive() && !spellSlot.hasItem()) {
                                if (moveItemStackTo(slotStack, i, i + 1, false)) {
                                    moved = true;
                                    break;
                                }
                            }
                        }
                        if (!moved) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.SPELL_BINDER.get());
    }
}