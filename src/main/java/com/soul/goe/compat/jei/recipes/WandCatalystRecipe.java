package com.soul.goe.compat.jei.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class WandCatalystRecipe {
    private final Block inputBlock;
    private final ItemStack result;

    public WandCatalystRecipe(Block inputBlock, ItemStack result) {
        this.inputBlock = inputBlock;
        this.result = result.copy();
    }

    public Block getInputBlock() {
        return inputBlock;
    }

    public ItemStack getResult() {
        return result;
    }
}