package com.soul.goe.compat.jei.recipes;

import com.soul.goe.Config;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class PedestalCraftingRecipe {
    private final String name;
    private final ItemStack centerItem;
    private final List<Ingredient> inputs;
    private final ItemStack result;

    public PedestalCraftingRecipe(Config.PedestalRecipeData recipeData) {
        this.name = recipeData.name();
        this.centerItem = recipeData.getCenterItemStack();
        this.inputs = recipeData.getInputIngredients();
        this.result = recipeData.getResultItemStack();
    }

    public String getName() {
        return name;
    }

    public ItemStack getCenterItem() {
        return centerItem;
    }

    public List<Ingredient> getInputs() {
        return inputs;
    }

    public ItemStack getResult() {
        return result;
    }
}