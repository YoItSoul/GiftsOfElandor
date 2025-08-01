package com.soul.goe.registry;

import com.soul.goe.Goe;
import com.soul.goe.items.custom.SalisMundus;
import com.soul.goe.items.custom.Spell;
import com.soul.goe.items.custom.Wand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Goe.MODID);

    public static final DeferredItem<BlockItem> FLARE =
            ITEMS.registerSimpleBlockItem("flare", ModBlocks.FLARE);

    public static final DeferredItem<BlockItem> EMPOWERED_LANTERN =
            ITEMS.registerSimpleBlockItem("empowered_lantern", ModBlocks.EMPOWERED_LANTERN);

    public static final DeferredItem<BlockItem> PEDESTAL =
            ITEMS.registerSimpleBlockItem("pedestal", ModBlocks.PEDESTAL);

    public static final DeferredItem<BlockItem> WARDING_LANTERN =
            ITEMS.registerSimpleBlockItem("warding_lantern", ModBlocks.WARDING_LANTERN);

    public static final DeferredItem<BlockItem> SPELL_BINDER =
            ITEMS.registerSimpleBlockItem("spell_binder", ModBlocks.SPELL_BINDER);

    public static final DeferredItem<SalisMundus> SALIS_MUNDUS = ITEMS.registerItem("salis_mundus",
            props -> new SalisMundus(props.stacksTo(64)));

    public static final DeferredItem<Wand> AMETHYST_WAND = ITEMS.registerItem("amethyst_wand",
            props -> new Wand(props.stacksTo(1).durability(100).repairable(SALIS_MUNDUS.get()), false,1));

    public static final DeferredItem<Wand> EMPOWERED_AMETHYST_WAND = ITEMS.registerItem("empowered_amethyst_wand",
            props -> new Wand(props.stacksTo(1).durability(500), true,4));

    public static final DeferredItem<Wand> ELANDORS_WAND = ITEMS.registerItem("elandors_wand",
            props -> new Wand(props.stacksTo(1), true,9));

    public static final DeferredItem<Item> ELANDORS_CHARM = ITEMS.registerItem("elandors_charm",
            props -> new Item(props.stacksTo(1)));

    public static final DeferredItem<Spell> FIREBOLT_SPELL = ITEMS.registerItem("firebolt_spell",
            props -> new Spell(props.stacksTo(1), "fire_bolt", 10));

    public static final DeferredItem<Spell> FIREBALL_SPELL = ITEMS.registerItem("fireball_spell",
            props -> new Spell(props.stacksTo(1), "fireball", 10, true));

    public static final DeferredItem<Spell> RAY_OF_FROST_SPELL = ITEMS.registerItem("ray_of_frost_spell",
            props -> new Spell(props.stacksTo(1), "ray_of_frost", 10, false));

    public static final DeferredItem<Spell> CONE_OF_COLD_SPELL = ITEMS.registerItem("cone_of_cold_spell",
            props -> new Spell(props.stacksTo(1), "cone_of_cold", 10, true));

    public static final DeferredItem<Spell> SHOCKING_GRASP_SPELL = ITEMS.registerItem("shocking_grasp_spell",
            props -> new Spell(props.stacksTo(1), "shocking_grasp", 10, false));

    public static final DeferredItem<Spell> LIGHTNING_BOLT_SPELL = ITEMS.registerItem("lightning_bolt_spell",
            props -> new Spell(props.stacksTo(1), "lightning_bolt", 10, true));

    public static final DeferredItem<Spell> LEVITATE_SPELL = ITEMS.registerItem("levitate_spell",
            props -> new Spell(props.stacksTo(1), "levitate", 10, false));

    public static final DeferredItem<Spell> FLY_SPELL = ITEMS.registerItem("fly_spell",
            props -> new Spell(props.stacksTo(1), "fly", 10, true));

    public static final DeferredItem<Spell> MAGIC_MISSILE_SPELL = ITEMS.registerItem("magic_missile_spell",
            props -> new Spell(props.stacksTo(1), "magic_missile", 10));

    public static final DeferredItem<Spell> LUNGE_SPELL = ITEMS.registerItem("lunge_spell",
            props -> new Spell(props.stacksTo(1), "lunge", 10));

    public static final DeferredItem<Spell> FROST_SPRITE_SPELL = ITEMS.registerItem("frost_sprite_spell",
            props -> new Spell(props.stacksTo(1), "frost_sprite", 10, true));

    public static final DeferredItem<Spell> ARC_SPRITE_SPELL = ITEMS.registerItem("arc_sprite_spell",
            props -> new Spell(props.stacksTo(1), "arc_sprite", 10, true));

    public static final DeferredItem<Spell> FLAME_SPRITE_SPELL = ITEMS.registerItem("flame_sprite_spell",
            props -> new Spell(props.stacksTo(1), "flame_sprite", 10, true));

    public static final DeferredItem<Spell> DRAGON_SPRITE_SPELL = ITEMS.registerItem("dragon_sprite_spell",
            props -> new Spell(props.stacksTo(1), "dragon_sprite", 10, true));


    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}