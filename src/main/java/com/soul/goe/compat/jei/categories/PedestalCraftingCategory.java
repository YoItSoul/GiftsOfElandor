package com.soul.goe.compat.jei.categories;

import com.soul.goe.Goe;
import com.soul.goe.compat.jei.JEIGoePlugin;
import com.soul.goe.compat.jei.recipes.PedestalCraftingRecipe;
import com.soul.goe.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class PedestalCraftingCategory implements IRecipeCategory<PedestalCraftingRecipe> {
    private final IDrawable icon;

    public PedestalCraftingCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.PEDESTAL.get()));
    }

    @Override
    public IRecipeType<PedestalCraftingRecipe> getRecipeType() {
        return JEIGoePlugin.PEDESTAL_CRAFTING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.goe.category.pedestal_crafting");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 150;
    }

    @Override
    public int getHeight() {
        return 80;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PedestalCraftingRecipe recipe, IFocusGroup focuses) {
        // Center pedestal item - always at the center of the left side
        int centerX = 40;
        int centerY = 31;

        builder.addSlot(RecipeIngredientRole.INPUT, centerX, centerY)
                .add(recipe.getCenterItem());

        List<Ingredient> inputs = recipe.getInputs();

        // Calculate positions in a perfect circle around the center
        int radius = 22; // Distance from center

        for (int i = 0; i < inputs.size(); i++) {
            // Calculate angle for even distribution around circle
            double angle = (2 * Math.PI * i) / inputs.size();

            // Calculate x, y coordinates on circle
            int x = centerX + (int)(radius * Math.cos(angle));
            int y = centerY + (int)(radius * Math.sin(angle));

            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .add(inputs.get(i));
        }

        // Result slot - positioned to the right
        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, centerY)
                .add(recipe.getResult());
    }

    @Override
    public void draw(PedestalCraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Clean interface - no text labels, just a simple arrow
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        guiGraphics.drawString(minecraft.font, "→", 95, 34, 0x808080, false);
    }
}