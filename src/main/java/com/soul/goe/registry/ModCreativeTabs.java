package com.soul.goe.registry;

import com.soul.goe.Goe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Goe.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GOE_TAB =
            CREATIVE_MODE_TABS.register("gifts_of_elandor", ModCreativeTabs::createGiftsOfElandorTab);

    private ModCreativeTabs() {
    }

    private static CreativeModeTab createGiftsOfElandorTab() {
        return CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.goe.gifts_of_elandor"))
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon(() -> ModItems.WAND.get().getDefaultInstance())
                .displayItems((parameters, output) -> {
                    // Wands
                    output.accept(ModItems.WAND.get());


                    // Magical Items
                    output.accept(ModItems.SALIS_MUNDUS.get());
                    output.accept(ModItems.ASH.get());
                    output.accept(ModItems.ELANDORS_CHARM.get());
                    output.accept(ModItems.DARKENED_COPPER_INGOT.get());

                    // Magical Blocks
                    output.accept(ModBlocks.FLARE.get());
                    output.accept(ModBlocks.EMPOWERED_LANTERN.get());
                    output.accept(ModBlocks.WARDING_LANTERN.get());
                    output.accept(ModBlocks.PEDESTAL.get());
                    output.accept(ModBlocks.SPELL_BINDER.get());
                    output.accept(ModBlocks.DARKENED_COPPER_BLOCK.get());
                    output.accept(ModBlocks.ASH_BLOCK.get());

                    // Spells
                    output.accept(ModItems.FIREBOLT_SPELL.get());
                    output.accept(ModItems.FIREBALL_SPELL.get());
                    output.accept(ModItems.RAY_OF_FROST_SPELL.get());
                    output.accept(ModItems.CONE_OF_COLD_SPELL.get());
                    output.accept(ModItems.LIGHTNING_BOLT_SPELL.get());
                    output.accept(ModItems.SHOCKING_GRASP_SPELL.get());
                    //output.accept(ModItems.LEVITATE_SPELL.get());
                    //output.accept(ModItems.FLY_SPELL.get());
                    output.accept(ModItems.MAGIC_MISSILE_SPELL.get());
                    //output.accept(ModItems.LUNGE_SPELL.get());
                    output.accept(ModItems.FROST_SPRITE_SPELL.get());
                    output.accept(ModItems.FLAME_SPRITE_SPELL.get());
                    output.accept(ModItems.ARC_SPRITE_SPELL.get());
                    output.accept(ModItems.DRAGON_SPRITE_SPELL.get());
                    //output.accept(ModItems.MAGNET_SPELL.get());
                    output.accept(ModItems.MINING_FORCE_SPELL.get());



                })
                .build();
    }


    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}