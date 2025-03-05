package com.soul.goe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Configuration handler for the GOE mod.
 * Manages mod settings and configurations using NeoForge's config system.
 */
@EventBusSubscriber(modid = Goe.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger("GOE/Config");
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final Map<Block, ItemStack> wandCatalysts = new HashMap<>();
    private static final String MAPPING_SEPARATOR = "->";
    private static final List<String> DEFAULT_WHITELISTED_ENTITIES = List.of(
            // Players and friendly humanoids
            "minecraft:player",
            "minecraft:villager",
            "minecraft:wandering_trader",
            "minecraft:iron_golem",
            "minecraft:snow_golem",

            // Passive animals
            "minecraft:allay",
            "minecraft:axolotl",
            "minecraft:bat",
            "minecraft:camel",
            "minecraft:cat",
            "minecraft:chicken",
            "minecraft:cod",
            "minecraft:cow",
            "minecraft:donkey",
            "minecraft:fox",
            "minecraft:frog",
            "minecraft:goat",
            "minecraft:horse",
            "minecraft:mooshroom",
            "minecraft:mule",
            "minecraft:ocelot",
            "minecraft:parrot",
            "minecraft:pig",
            "minecraft:rabbit",
            "minecraft:salmon",
            "minecraft:sheep",
            "minecraft:sniffer",
            "minecraft:squid",
            "minecraft:strider",
            "minecraft:tadpole",
            "minecraft:tropical_fish",
            "minecraft:turtle",
            "minecraft:wolf", // When tamed

            // Neutral mobs (only attack when provoked)
            "minecraft:bee",
            "minecraft:dolphin",
            "minecraft:llama",
            "minecraft:panda",
            "minecraft:polar_bear",
            "minecraft:trader_llama"
    );

    // Warding Lantern Configuration
    public static final ModConfigSpec.DoubleValue WARDING_BLOCK_RADIUS;
    public static final ModConfigSpec.BooleanValue IGNITE_ENTITIES;
    public static final ModConfigSpec.DoubleValue PARTICLE_SPEED;
    public static final ModConfigSpec.IntValue FIRE_DURATION;
    public static final ModConfigSpec.IntValue MAX_PARTICLES_PER_PACKET;
    public static final ModConfigSpec.IntValue MAX_PARTICLES;
    public static final ModConfigSpec.DoubleValue PUSH_STRENGTH;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WARDING_WHITELISTED_ENTITIES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WAND_CATALYST_MAPPINGS;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("Warding Lantern Settings");

        // Initialize Warding Lantern Configuration
        WARDING_BLOCK_RADIUS = BUILDER
                .comment("The radius in blocks that the warding lantern will affect entities")
                .defineInRange("wardingBlockRadius", 5.0, 1.0, 16.0);

        IGNITE_ENTITIES = BUILDER
                .comment("Whether the warding lantern should set entities on fire")
                .define("igniteEntities", true);

        PARTICLE_SPEED = BUILDER
                .comment("The speed of the warding lantern particles")
                .defineInRange("particleSpeed", 0.1F, 0.0F, 1.0F);

        FIRE_DURATION = BUILDER
                .comment("How long entities should burn when hit by the warding lantern (in ticks)")
                .defineInRange("fireDuration", 100, 0, 1000);

        MAX_PARTICLES_PER_PACKET = BUILDER
                .comment("Maximum number of particles per packet for the warding lantern")
                .defineInRange("maxParticlesPerPacket", 2, 1, 10);

        MAX_PARTICLES = BUILDER
                .comment("Maximum total number of particles for the warding lantern")
                .defineInRange("maxParticles", 10, 1, 50);

        PUSH_STRENGTH = BUILDER
                .comment("The strength with which entities are pushed back by the warding lantern")
                .defineInRange("pushStrength", 0.5D, 0.1D, 2.0D);

        WARDING_WHITELISTED_ENTITIES = BUILDER
                .comment("List of entity types that won't be affected by the warding lantern")
                .define("wardingWhitelistedEntities",
                        DEFAULT_WHITELISTED_ENTITIES,
                        obj -> obj instanceof List<?> list &&
                                list.stream().allMatch(item -> item instanceof String));

        WAND_CATALYST_MAPPINGS = BUILDER
                .comment("List of block-to-item conversion mappings for the wand in format: blockid->itemid:count")
                .define("wandCatalysts",
                        List.of(
                                "minecraft:bone_block->goe:salis_mundus:9",
                                "minecraft:bookshelf->minecraft:written_book:1"
                        ),
                        obj -> obj instanceof List<?> list &&
                                list.stream().allMatch(Config::validateCatalystMapping));

        SPEC = BUILDER.build();
    }

    private Config() {
        // Private constructor to prevent instantiation
    }

    private static boolean validateCatalystMapping(final Object obj) {
        if (!(obj instanceof String mapping)) {
            return false;
        }

        try {
            CatalystMapping parsedMapping = parseCatalystMapping(mapping);
            return validateResourceLocations(parsedMapping);
        } catch (Exception e) {
            LOGGER.error("Error validating catalyst mapping: {}", obj, e);
            return false;
        }
    }

    private static CatalystMapping parseCatalystMapping(String mapping) {
        String[] parts = mapping.split(MAPPING_SEPARATOR);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid mapping format");
        }

        String blockId = parts[0].trim();
        String itemPart = parts[1].trim();

        int lastColon = itemPart.lastIndexOf(':');
        if (lastColon == -1) {
            throw new IllegalArgumentException("Invalid item format");
        }

        String itemId = itemPart.substring(0, lastColon);
        int count = Integer.parseInt(itemPart.substring(lastColon + 1).trim());

        if (count <= 0) {
            throw new IllegalArgumentException("Invalid item count");
        }

        return new CatalystMapping(blockId, itemId, count);
    }

    private static boolean validateResourceLocations(CatalystMapping mapping) {
        ResourceLocation blockLoc = ResourceLocation.tryParse(mapping.blockId());
        ResourceLocation itemLoc = ResourceLocation.tryParse(mapping.itemId());

        if (blockLoc == null || !BuiltInRegistries.BLOCK.containsKey(blockLoc)) {
            LOGGER.error("Invalid block ID: {}", mapping.blockId());
            return false;
        }

        if (itemLoc == null || !BuiltInRegistries.ITEM.containsKey(itemLoc)) {
            LOGGER.error("Invalid item ID: {}", mapping.itemId());
            return false;
        }

        return true;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        loadWandCatalysts();
    }

    private static void loadWandCatalysts() {
        wandCatalysts.clear();
        for (String mapping : WAND_CATALYST_MAPPINGS.get()) {
            try {
                CatalystMapping parsedMapping = parseCatalystMapping(mapping);
                addWandCatalyst(parsedMapping);
            } catch (Exception e) {
                LOGGER.error("Failed to load catalyst mapping: {}", mapping, e);
            }
        }
    }

    private static void addWandCatalyst(CatalystMapping mapping) {
        ResourceLocation blockLoc = ResourceLocation.tryParse(mapping.blockId());
        ResourceLocation itemLoc = ResourceLocation.tryParse(mapping.itemId());

        if (blockLoc != null && itemLoc != null) {
            Block block = BuiltInRegistries.BLOCK.getValue(blockLoc);
            Item item = BuiltInRegistries.ITEM.getValue(itemLoc);
            wandCatalysts.put(block, new ItemStack(item, mapping.count()));
            LOGGER.info("Registered wand catalyst: {} -> {} x{}", blockLoc, itemLoc, mapping.count());
        }
    }

    public static Map<Block, ItemStack> getWandCatalysts() {
        return Collections.unmodifiableMap(wandCatalysts);
    }

    private record CatalystMapping(String blockId, String itemId, int count) {}
}