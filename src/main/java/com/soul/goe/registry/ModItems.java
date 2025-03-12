package com.soul.goe.registry;

import com.soul.goe.Goe;
import com.soul.goe.items.custom.ElandorsSpyGlass;
import com.soul.goe.items.custom.SalisMundus;
import com.soul.goe.items.custom.Wand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Goe.MODID);

    //Block Items

    public static final DeferredItem<BlockItem> FLARE =
            ITEMS.registerSimpleBlockItem("flare", ModBlocks.FLARE);

    public static final DeferredItem<BlockItem> EMPOWERED_LANTERN =
            ITEMS.registerSimpleBlockItem("empowered_lantern", ModBlocks.EMPOWERED_LANTERN);

    public static final DeferredItem<BlockItem> WARDING_LANTERN =
            ITEMS.registerSimpleBlockItem("warding_lantern", ModBlocks.WARDING_LANTERN);
    // Items
    public static final DeferredItem<SalisMundus> SALIS_MUNDUS = ITEMS.registerItem("salis_mundus",
            props -> new SalisMundus(props.stacksTo(64)));

    public static final DeferredItem<Wand> AMETHYST_WAND = ITEMS.registerItem("amethyst_wand",
            props -> new Wand(props.stacksTo(1).durability(100), false));

    public static final DeferredItem<Wand> EMPOWERED_AMETHYST_WAND = ITEMS.registerItem("empowered_amethyst_wand",
            props -> new Wand(props.stacksTo(1).durability(500), true));

    public static final DeferredItem<Wand> ELANDORS_WAND = ITEMS.registerItem("elandors_wand",
            props -> new Wand(props.stacksTo(1), true));

    public static final DeferredItem<Item> ELANDORS_CHARM = ITEMS.registerItem("elandors_charm",
            props -> new Item(props.stacksTo(1)));

    public static final DeferredItem<ElandorsSpyGlass> ELANDORS_SPY_GLASS = ITEMS.registerItem("elandors_spy_glass",
            props -> new ElandorsSpyGlass(props.stacksTo(1)));


    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}