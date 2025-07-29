package com.soul.goe.registry;

import com.soul.goe.Goe;
import com.soul.goe.blocks.entity.EmpoweredLanternEntity;
import com.soul.goe.blocks.entity.WardingLanternEntity;
import com.soul.goe.blocks.entity.PedestalEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Goe.MODID);

    public static final Supplier<BlockEntityType<EmpoweredLanternEntity>> EMPOWERED_LANTERN =
            BLOCK_ENTITIES.register("empowered_lantern",
                    () -> new BlockEntityType<>(
                            EmpoweredLanternEntity::new,
                            ModBlocks.EMPOWERED_LANTERN.get()
                    )
            );

    public static final Supplier<BlockEntityType<WardingLanternEntity>> WARDING_LANTERN =
            BLOCK_ENTITIES.register("warding_lantern",
                    () -> new BlockEntityType<>(
                            WardingLanternEntity::new,
                            ModBlocks.WARDING_LANTERN.get()
                    )
            );

    public static final Supplier<BlockEntityType<PedestalEntity>> PEDESTAL =
            BLOCK_ENTITIES.register("pedestal",
                    () -> new BlockEntityType<>(
                            PedestalEntity::new,
                            ModBlocks.PEDESTAL.get()
                    )
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}