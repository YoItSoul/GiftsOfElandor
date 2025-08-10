package com.soul.goe.client.menus;

import com.soul.goe.blocks.entity.SpellBinderEntity;
import com.soul.goe.items.custom.Spell;
import com.soul.goe.items.custom.Wand;
import com.soul.goe.registry.ModBlocks;
import com.soul.goe.registry.ModMenuTypes;
import com.soul.goe.util.wands.WandStats;
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

        addSlot(new WandSlot(blockEntity.getInventory(), 0, 80, 18, this));

        addSlot(new WandPartSlot(blockEntity.getInventory(), 1, 58, 40, "handle"));
        addSlot(new WandPartSlot(blockEntity.getInventory(), 2, 80, 40, "binder"));
        addSlot(new WandPartSlot(blockEntity.getInventory(), 3, 102, 40, "cap"));

        for (int col = 0; col < 7; col++) {
            addSlot(new SpellSlot(blockEntity.getInventory(), col + 4, 26 + col * 18, 62, this));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    public SpellBinderMenu(int windowId, Inventory playerInventory, BlockEntity entity) {
        this(windowId, playerInventory, (SpellBinderEntity) entity);
    }

    private static class WandSlot extends SlotItemHandler {
        private final SpellBinderMenu menu;

        public WandSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, SpellBinderMenu menu) {
            super(itemHandler, index, xPosition, yPosition);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof Wand;
        }

        @Override
        public boolean mayPickup(Player player) {
            return !hasIncompleteParts();
        }

        private boolean hasIncompleteParts() {
            for (int i = 1; i <= 3; i++) {
                if (menu.getSlot(i).getItem().isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    private class WandPartSlot extends SlotItemHandler {
        private final String partType;

        public WandPartSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, String partType) {
            super(itemHandler, index, xPosition, yPosition);
            this.partType = partType;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return blockEntity.canModifyParts() && isValidWandPart(stack) && super.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return blockEntity.canModifyParts() && super.mayPickup(player);
        }

        @Override
        public boolean isActive() {
            return blockEntity.canModifyParts();
        }

        private boolean isValidWandPart(ItemStack stack) {
            if (stack.isEmpty()) return true;

            var materials = switch (partType) {
                case "handle" -> com.soul.goe.Config.getHandleMaterials();
                case "binder" -> com.soul.goe.Config.getBinderMaterials();
                case "cap" -> com.soul.goe.Config.getCapMaterials();
                default -> java.util.Map.<String, com.soul.goe.util.wands.WandMaterialData>of();
            };

            return materials.values().stream().anyMatch(data -> data.item().equals(stack.getItem()));
        }

        public String getPartType() {
            return partType;
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
                int spellSlotIndex = getSlotIndex() - 4;
                return spellSlotIndex < menu.blockEntity.getCurrentAvailableSpells();
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

            if (index < 13) {
                if (!moveItemStackTo(slotStack, 13, slots.size(), true)) {
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
                        for (int i = 4; i < 13; i++) {
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
                    if (blockEntity.canModifyParts()) {
                        String partType = getPartTypeForItem(slotStack);
                        if (partType != null) {
                            int targetSlot = switch (partType) {
                                case "handle" -> 1;
                                case "binder" -> 2;
                                case "cap" -> 3;
                                default -> -1;
                            };

                            if (targetSlot != -1 && !slots.get(targetSlot).hasItem()) {
                                if (!moveItemStackTo(slotStack, targetSlot, targetSlot + 1, false)) {
                                    return ItemStack.EMPTY;
                                }
                            } else {
                                return ItemStack.EMPTY;
                            }
                        } else {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        return ItemStack.EMPTY;
                    }
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

    private String getPartTypeForItem(ItemStack stack) {
        var handleMaterials = com.soul.goe.Config.getHandleMaterials();
        var binderMaterials = com.soul.goe.Config.getBinderMaterials();
        var capMaterials = com.soul.goe.Config.getCapMaterials();

        if (handleMaterials.values().stream().anyMatch(data -> data.item().equals(stack.getItem()))) {
            return "handle";
        }
        if (binderMaterials.values().stream().anyMatch(data -> data.item().equals(stack.getItem()))) {
            return "binder";
        }
        if (capMaterials.values().stream().anyMatch(data -> data.item().equals(stack.getItem()))) {
            return "cap";
        }

        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.SPELL_BINDER.get());
    }

    public boolean isWandDamaged() {
        return blockEntity != null && blockEntity.isWandDamaged();
    }

    public boolean canModifyParts() {
        return blockEntity != null && blockEntity.canModifyParts();
    }

    public boolean hasWand() {
        return blockEntity != null && blockEntity.hasWand();
    }

    public boolean wandHasSpells() {
        return blockEntity != null && blockEntity.wandHasSpells();
    }

    public WandStats getCurrentWandStats() {
        return blockEntity != null ? blockEntity.getCurrentWandStats() : null;
    }

    public boolean isPartSlot(int slotIndex) {
        return slotIndex >= 1 && slotIndex <= 3;
    }

    public String getPartType(int slotIndex) {
        return switch (slotIndex) {
            case 1 -> "handle";
            case 2 -> "binder";
            case 3 -> "cap";
            default -> "unknown";
        };
    }

    public boolean isSpellSlot(int slotIndex) {
        return slotIndex >= 4 && slotIndex <= 12;
    }

    public int getCurrentAvailableSpells() {
        return blockEntity != null ? blockEntity.getCurrentAvailableSpells() : 0;
    }
}