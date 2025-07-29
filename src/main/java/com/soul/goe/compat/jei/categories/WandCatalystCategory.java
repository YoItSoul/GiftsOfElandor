package com.soul.goe.compat.jei.categories;

import com.soul.goe.Goe;
import com.soul.goe.compat.jei.JEIGoePlugin;
import com.soul.goe.compat.jei.recipes.WandCatalystRecipe;
import com.soul.goe.registry.ModItems;
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

public class WandCatalystCategory implements IRecipeCategory<WandCatalystRecipe> {
    private final IDrawable icon;

    public WandCatalystCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemStack(new ItemStack(ModItems.ELANDORS_WAND.get()));
    }

    @Override
    public IRecipeType<WandCatalystRecipe> getRecipeType() {
        return JEIGoePlugin.WAND_CATALYST;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.goe.category.wand_catalyst");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 116;
    }

    @Override
    public int getHeight() {
        return 54;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WandCatalystRecipe recipe, IFocusGroup focuses) {
        // Clean, evenly spaced layout
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 19)
                .add(new ItemStack(recipe.getInputBlock()));

        builder.addSlot(RecipeIngredientRole.INPUT, 40, 19)
                .add(new ItemStack(ModItems.AMETHYST_WAND.get()))
                .add(new ItemStack(ModItems.EMPOWERED_AMETHYST_WAND.get()))
                .add(new ItemStack(ModItems.ELANDORS_WAND.get()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 19)
                .add(recipe.getResult());
    }

    @Override
    public void draw(WandCatalystRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Clean interface - just symbols, no text labels
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        guiGraphics.drawString(minecraft.font, "+", 30, 22, 0x808080, false);
        guiGraphics.drawString(minecraft.font, "→", 67, 22, 0x808080, false);
    }
}