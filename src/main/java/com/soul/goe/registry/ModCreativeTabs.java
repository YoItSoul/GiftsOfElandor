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
                .icon(() -> ModItems.ELANDORS_WAND.get().getDefaultInstance())
                .displayItems((parameters, output) -> {
                    // Wands
                    output.accept(ModItems.AMETHYST_WAND.get());
                    output.accept(ModItems.EMPOWERED_AMETHYST_WAND.get());
                    output.accept(ModItems.ELANDORS_WAND.get());

                    // Magical Items
                    output.accept(ModItems.SALIS_MUNDUS.get());
                    output.accept(ModItems.ELANDORS_CHARM.get());

                    // Magical Blocks
                    output.accept(ModBlocks.FLARE.get());
                    output.accept(ModBlocks.EMPOWERED_LANTERN.get());
                    output.accept(ModBlocks.WARDING_LANTERN.get());
                    output.accept(ModBlocks.PEDESTAL.get());
                })
                .build();
    }


    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}