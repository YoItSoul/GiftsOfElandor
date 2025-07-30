package com.soul.goe.datagen;

import com.soul.goe.Goe;
import com.soul.goe.registry.ModBlocks;
import com.soul.goe.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;

import java.util.Set;

public class ModModelProvider extends ModelProvider {

    private static final Set<Item> EXCLUDED_ITEMS = Set.of(
            ModItems.FLARE.get(),
            ModItems.EMPOWERED_LANTERN.get(),
            ModItems.WARDING_LANTERN.get(),
            ModItems.PEDESTAL.get()
    );

    public ModModelProvider(PackOutput output) {
        super(output, Goe.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModBlocks.FLARE.get()));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModBlocks.EMPOWERED_LANTERN.get()));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModBlocks.WARDING_LANTERN.get()));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModBlocks.PEDESTAL.get()));

        itemModels.generateFlatItem(ModItems.SALIS_MUNDUS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.AMETHYST_WAND.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.EMPOWERED_AMETHYST_WAND.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ELANDORS_WAND.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ELANDORS_CHARM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FIREBOLT_SPELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FIREBALL_SPELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RAY_OF_FROST_SPELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.CONE_OF_COLD_SPELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.LIGHTNING_BOLT_SPELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SHOCKING_GRASP_SPELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.LEVITATE_SPELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FLY_SPELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.MAGIC_MISSILE_SPELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.LUNGE_SPELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FROST_SPRITE_SPELL.get(), ModelTemplates.FLAT_ITEM);


    }
}