package com.soul.goe;

import com.soul.goe.datagen.ModModelProvider;
import com.soul.goe.items.custom.Wand;
import com.soul.goe.registry.*;
import com.soul.goe.client.rendering.EmptyEntityRenderer;
import com.soul.goe.spells.util.SpellInit;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import com.soul.goe.client.rendering.PedestalRenderer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Goe.MODID)
public class Goe {
    public static final String MODID = "goe";
    public static final Logger LOGGER = LoggerFactory.getLogger("GOE/Main");

    public Goe(IEventBus modEventBus, ModContainer modContainer) {
        registerAll(modEventBus, modContainer);
    }

    private void registerAll(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntityRegistry.ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        modEventBus.addListener(this::onCommonSetup);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Initializing common setup...");
        event.enqueueWork(() -> {
            Wand.registerCatalysts();
            SpellInit.registerSpells();
        });
        LOGGER.info("JEI compatibility layer initialized");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server initialization starting...");
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            LOGGER.info("Registering block entity renderers...");
            event.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL.get(), PedestalRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.FIREBOLT_PROJECTILE.get(), EmptyEntityRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.FIREBALL_PROJECTILE.get(), EmptyEntityRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.RAY_OF_FROST_PROJECTILE.get(), EmptyEntityRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.CONE_OF_COLD_PROJECTILE.get(), EmptyEntityRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.SHOCKING_GRASP_PROJECTILE.get(), EmptyEntityRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.LIGHTNING_BOLT_PROJECTILE.get(), EmptyEntityRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.MAGIC_MISSILE_PROJECTILE.get(), EmptyEntityRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.FROST_SPRITE_PROJECTILE.get(), EmptyEntityRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.FLAME_SPRITE_PROJECTILE.get(), EmptyEntityRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.ARC_SPRITE_PROJECTILE.get(), EmptyEntityRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.DRAGON_SPRITE_PROJECTILE.get(), EmptyEntityRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.MAGNET_PROJECTILE.get(), EmptyEntityRenderer::new);
            event.registerEntityRenderer(ModEntityRegistry.MINING_FORCE_PROJECTILE.get(), EmptyEntityRenderer::new);

        }
    }

    @EventBusSubscriber(modid = Goe.MODID)
    public static class DataGen {
        @SubscribeEvent
        public static void gatherData(GatherDataEvent.Client event) {
            event.createProvider(ModModelProvider::new);
        }
    }
}