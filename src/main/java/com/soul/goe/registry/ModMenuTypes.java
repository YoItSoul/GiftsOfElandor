package com.soul.goe.registry;

import com.soul.goe.Goe;
import com.soul.goe.blocks.entity.SpellBinderEntity;
import com.soul.goe.client.menus.SpellBinderMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, Goe.MODID);

    public static final Supplier<MenuType<SpellBinderMenu>> SPELL_BINDER =
            MENU_TYPES.register("spell_binder",
                    () -> IMenuTypeExtension.create(
                            (windowId, playerInventory, extraData) -> {
                                BlockPos pos = extraData.readBlockPos();
                                if (playerInventory.player.level().getBlockEntity(pos) instanceof SpellBinderEntity entity) {
                                    return new SpellBinderMenu(windowId, playerInventory, entity);
                                }
                                return null;
                            }));

    private ModMenuTypes() {}

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}