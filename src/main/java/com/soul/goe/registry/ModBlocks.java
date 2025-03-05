package com.soul.goe.registry;

import com.soul.goe.Goe;
import com.soul.goe.blocks.custom.EmpoweredLantern;

import com.soul.goe.blocks.custom.WardingLantern;
import com.soul.goe.blocks.custom.Flare;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;


public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Goe.MODID);

    public static final DeferredBlock<Flare> FLARE = BLOCKS.registerBlock("flare",
            props -> new Flare(props.mapColor(MapColor.FIRE)
                    .noCollission()
                    .noOcclusion()
                    .lightLevel(state -> 15)));

    public static final DeferredBlock<EmpoweredLantern> EMPOWERED_LANTERN = BLOCKS.registerBlock("empowered_lantern",
            props -> new EmpoweredLantern(props.mapColor(MapColor.GOLD)
                    .noOcclusion()
                    .lightLevel(state -> 15)));

    public static final DeferredBlock<WardingLantern> WARDING_LANTERN = BLOCKS.registerBlock("warding_lantern",
            props -> new WardingLantern(props.mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noOcclusion()
                    .lightLevel(state -> 15)));



    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }

}