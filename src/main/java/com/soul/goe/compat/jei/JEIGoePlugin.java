package com.soul.goe.compat.jei;

import com.soul.goe.Config;
import com.soul.goe.Goe;
import com.soul.goe.compat.jei.categories.PedestalCraftingCategory;
import com.soul.goe.compat.jei.categories.WandCatalystCategory;
import com.soul.goe.compat.jei.recipes.PedestalCraftingRecipe;
import com.soul.goe.compat.jei.recipes.WandCatalystRecipe;
import com.soul.goe.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class JEIGoePlugin implements IModPlugin {
    public static final IRecipeType<PedestalCraftingRecipe> PEDESTAL_CRAFTING =
            IRecipeType.create(Goe.MODID, "pedestal_crafting", PedestalCraftingRecipe.class);

    public static final IRecipeType<WandCatalystRecipe> WAND_CATALYST =
            IRecipeType.create(Goe.MODID, "wand_catalyst", WandCatalystRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Goe.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PedestalCraftingCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new WandCatalystCategory(registration.getJeiHelpers().getGuiHelper()));
        Goe.LOGGER.info("Registered JEI recipe categories");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registerWandCatalystRecipes(registration);
        registerPedestalRecipes(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(PEDESTAL_CRAFTING, new ItemStack(ModBlocks.PEDESTAL.get()));
        registration.addCraftingStation(WAND_CATALYST,
                new ItemStack(com.soul.goe.registry.ModItems.WAND.get()));

    }

    private void registerWandCatalystRecipes(IRecipeRegistration registration) {
        Map<Block, ItemStack> wandCatalysts = Config.getWandCatalysts();
        if (wandCatalysts.isEmpty()) return;

        List<WandCatalystRecipe> recipes = new ArrayList<>();
        for (Map.Entry<Block, ItemStack> entry : wandCatalysts.entrySet()) {
            recipes.add(new WandCatalystRecipe(entry.getKey(), entry.getValue()));
        }

        registration.addRecipes(WAND_CATALYST, recipes);
        Goe.LOGGER.info("Registered {} wand catalyst recipes with JEI", recipes.size());
    }

    private void registerPedestalRecipes(IRecipeRegistration registration) {
        Map<String, Config.PedestalRecipeData> pedestalRecipes = Config.getPedestalRecipes();
        if (pedestalRecipes.isEmpty()) return;

        List<PedestalCraftingRecipe> recipes = new ArrayList<>();
        for (Config.PedestalRecipeData recipeData : pedestalRecipes.values()) {
            try {
                PedestalCraftingRecipe recipe = new PedestalCraftingRecipe(recipeData);
                if (!recipe.getCenterItem().isEmpty() && !recipe.getResult().isEmpty() && !recipe.getInputs().isEmpty()) {
                    recipes.add(recipe);
                }
            } catch (Exception e) {
                Goe.LOGGER.warn("Failed to create JEI recipe for pedestal recipe: {}", recipeData.name(), e);
            }
        }

        registration.addRecipes(PEDESTAL_CRAFTING, recipes);
        Goe.LOGGER.info("Registered {} pedestal crafting recipes with JEI", recipes.size());
    }
}