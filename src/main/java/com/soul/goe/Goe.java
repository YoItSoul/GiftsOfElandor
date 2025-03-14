package com.soul.goe;

import com.soul.goe.client.ItemTooltipHandler;
import com.soul.goe.commands.AspectCommand;
import com.soul.goe.datagen.ModModelProvider;
import com.soul.goe.items.custom.Wand;
import com.soul.goe.registry.*;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class for the GOE mod.
 */
@Mod(Goe.MODID)
public class Goe {
    public static final String MODID = "goe";
    public static final Logger LOGGER = LoggerFactory.getLogger("GOE/Main");
    public static final AspectRegistry ASPECT_REGISTRY = new AspectRegistry();
    public static final ItemAspectRegistry ITEM_ASPECT_REGISTRY = new ItemAspectRegistry(ASPECT_REGISTRY);

    public Goe(IEventBus modEventBus, ModContainer modContainer) {
        registerAll(modEventBus, modContainer);
    }

    private void registerAll(IEventBus modEventBus, ModContainer modContainer) {

        // Register content
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModBlockEntities.register(modEventBus);


        // Register event listeners
        modEventBus.addListener(this::onCommonSetup);


        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);


    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Initializing common setup...");
        event.enqueueWork(() -> {
            Wand.registerCatalysts();
            ASPECT_REGISTRY.registerAspects();
            ITEM_ASPECT_REGISTRY.loadItemAspects();
        });
    }


    @EventBusSubscriber(modid = MODID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            AspectCommand.register(event.getDispatcher());
        }
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server initialization starting...");
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Initializing client setup... Player: {}", Minecraft.getInstance().getUser().getName());

            ItemTooltipHandler.init(ITEM_ASPECT_REGISTRY);
        }
    }


    @EventBusSubscriber(modid = Goe.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class DataGen {
        @SubscribeEvent
        public static void gatherData(GatherDataEvent.Client event) {
            event.createProvider(ModModelProvider::new);
        }
    }
}