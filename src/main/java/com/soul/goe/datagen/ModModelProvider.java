package com.soul.goe.datagen;

import com.soul.goe.Goe;
import com.soul.goe.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;


public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, Goe.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        itemModels.generateFlatItem(ModItems.SALIS_MUNDUS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.AMETHYST_WAND.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.EMPOWERED_AMETHYST_WAND.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ELANDORS_WAND.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ELANDORS_CHARM.get(), ModelTemplates.FLAT_ITEM);
    }

}
