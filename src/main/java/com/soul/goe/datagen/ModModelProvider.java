package com.soul.goe.datagen;

import com.soul.goe.Goe;
import com.soul.goe.registry.ModBlocks;
import com.soul.goe.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, Goe.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        // Register item models
        itemModels.generateFlatItem(ModItems.SALIS_MUNDUS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.AMETHYST_WAND.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.EMPOWERED_AMETHYST_WAND.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ELANDORS_WAND.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ELANDORS_CHARM.get(), ModelTemplates.FLAT_ITEM);
    }

    @Override
    protected @NotNull Stream<? extends Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks().filter(holder -> {
            Block block = holder.value();
            // Return false for blocks you want to skip
            return block != ModBlocks.FLARE.get() &&
                    block != ModBlocks.EMPOWERED_LANTERN.get() &&
                    block != ModBlocks.WARDING_LANTERN.get();
        });
    }

}
