package com.soul.goe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Configuration handler for the GOE (Game Of Elements) mod.
 * This class manages all mod settings and configurations using NeoForge's config system.
 * It handles aspect definitions, game mechanics settings, and various mod parameters.
 *
 * @since 1.0
 */
@EventBusSubscriber(modid = Goe.MODID)
public final class Config {

    /**
     * Logger instance for configuration-related logging
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("GOE/Config");

    /**
     * Separator used for mapping strings in configuration
     */
    private static final String MAPPING_SEPARATOR = "->";

    /**
     * Storage for wand catalyst mappings
     */
    private static final Map<Block, ItemStack> wandCatalysts = new HashMap<>();

    /**
     * Storage for pedestal recipes
     */
    private static final Map<String, PedestalRecipeData> pedestalRecipes = new HashMap<>();

    /**
     * Defines the radius of warding block effects
     */
    public static final ModConfigSpec.DoubleValue WARDING_BLOCK_RADIUS;

    /**
     * Controls whether entities can be ignited
     */
    public static final ModConfigSpec.BooleanValue IGNITE_ENTITIES;

    /**
     * Determines the speed of particles in the mod
     */
    public static final ModConfigSpec.DoubleValue PARTICLE_SPEED;

    /**
     * Sets the duration of fire effects in ticks
     */
    public static final ModConfigSpec.IntValue FIRE_DURATION;

    /**
     * Maximum number of particles that can be sent in a single packet
     */
    public static final ModConfigSpec.IntValue MAX_PARTICLES_PER_PACKET;

    /**
     * Maximum total number of particles allowed
     */
    public static final ModConfigSpec.IntValue MAX_PARTICLES;

    /**
     * Strength of push effects
     */
    public static final ModConfigSpec.DoubleValue PUSH_STRENGTH;

    /**
     * List of entities that are whitelisted for warding effects
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WARDING_WHITELISTED_ENTITIES;

    /**
     * Pedestal crafting search radius
     */
    public static final ModConfigSpec.IntValue PEDESTAL_SEARCH_RADIUS;

    /**
     * Enable pedestal crafting
     */
    public static final ModConfigSpec.BooleanValue ENABLE_PEDESTAL_CRAFTING;

    /**
     * Pedestal recipe configurations
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PEDESTAL_RECIPES;

    /**
     * Default list of whitelisted entities (not affected by warding_glyph)
     */
    private static final List<String> DEFAULT_WHITELISTED_ENTITIES = List.of(
            "minecraft:player",
            "minecraft:villager",
            "minecraft:wandering_trader",
            "minecraft:iron_golem",
            "minecraft:snow_golem",
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
            "minecraft:wolf",
            "minecraft:bee",
            "minecraft:dolphin",
            "minecraft:llama",
            "minecraft:panda",
            "minecraft:polar_bear",
            "minecraft:trader_llama"
    );

    /**
     * Default pedestal recipes
     */
    private static final List<String> DEFAULT_PEDESTAL_RECIPES = List.of(
            "elandors_charm=minecraft:ender_pearl|goe:salis_mundus,minecraft:gold_ingot,minecraft:gold_ingot,minecraft:gold_ingot,minecraft:gold_ingot->goe:elandors_charm:1",
            "pedestal=minecraft:hopper|goe:salis_mundus,minecraft:iron_ingot,minecraft:iron_ingot,minecraft:iron_ingot,minecraft:spruce_planks->goe:pedestal:1",
            "empowered_lantern=minecraft:lantern|minecraft:glowstone,minecraft:campfire,minecraft:nether_star,goe:salis_mundus->goe:empowered_lantern:1",
            "warding_lantern=minecraft:sea_lantern|minecraft:totem_of_undying,minecraft:blaze_powder,minecraft:ghast_tear,goe:salis_mundus->goe:warding_lantern:1"
    );

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
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

        BUILDER.comment("Pedestal Crafting Settings");

        ENABLE_PEDESTAL_CRAFTING = BUILDER
                .comment("Enable pedestal crafting system")
                .define("enablePedestalCrafting", true);

        PEDESTAL_SEARCH_RADIUS = BUILDER
                .comment("Search radius for pedestal crafting (in blocks)")
                .defineInRange("pedestalSearchRadius", 10, 1, 20);

        PEDESTAL_RECIPES = BUILDER
                .comment("Pedestal recipes in format: name=center_item|input1,input2,input3->result_item:count")
                .define("pedestalRecipes",
                        DEFAULT_PEDESTAL_RECIPES,
                        obj -> obj instanceof List<?> list &&
                                list.stream().allMatch(Config::validatePedestalRecipe));

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

    /**
     * Private constructor to prevent instantiation of utility class
     */
    private Config() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    private static boolean validatePedestalRecipe(final Object obj) {
        if (!(obj instanceof String recipe)) {
            return false;
        }

        try {
            PedestalRecipeData parsedRecipe = parsePedestalRecipe(recipe);
            return validatePedestalRecipeItems(parsedRecipe);
        } catch (Exception e) {
            LOGGER.error("Error validating pedestal recipe: {}", obj, e);
            return false;
        }
    }

    private static PedestalRecipeData parsePedestalRecipe(String recipe) {
        // Find the first equals sign to separate name from recipe
        int firstEquals = recipe.indexOf('=');
        if (firstEquals == -1) {
            throw new IllegalArgumentException("Invalid recipe format - missing name separator");
        }

        String name = recipe.substring(0, firstEquals).trim();
        String recipePart = recipe.substring(firstEquals + 1);

        String[] parts = recipePart.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid recipe format - missing center item separator");
        }

        String centerItem = parts[0].trim();
        String remainder = parts[1];

        String[] arrowParts = remainder.split(MAPPING_SEPARATOR);
        if (arrowParts.length != 2) {
            throw new IllegalArgumentException("Invalid recipe format - missing arrow separator");
        }

        String[] inputItems = arrowParts[0].trim().split(",");
        for (int i = 0; i < inputItems.length; i++) {
            inputItems[i] = inputItems[i].trim();
        }

        String resultPart = arrowParts[1].trim();
        int lastColon = resultPart.lastIndexOf(':');
        if (lastColon == -1) {
            throw new IllegalArgumentException("Invalid result format");
        }

        String resultItem = resultPart.substring(0, lastColon);
        int resultCount = Integer.parseInt(resultPart.substring(lastColon + 1).trim());

        if (resultCount <= 0) {
            throw new IllegalArgumentException("Invalid result count");
        }

        return new PedestalRecipeData(name, centerItem, inputItems, resultItem, resultCount);
    }

    private static boolean validatePedestalRecipeItems(PedestalRecipeData recipe) {
        // Validate center item
        ResourceLocation centerLoc = ResourceLocation.tryParse(recipe.centerItem());
        if (centerLoc == null || !BuiltInRegistries.ITEM.containsKey(centerLoc)) {
            LOGGER.error("Invalid center item ID: {}", recipe.centerItem());
            return false;
        }

        // Validate input items
        for (String inputItem : recipe.inputItems()) {
            ResourceLocation inputLoc = ResourceLocation.tryParse(inputItem);
            if (inputLoc == null || !BuiltInRegistries.ITEM.containsKey(inputLoc)) {
                LOGGER.error("Invalid input item ID: {}", inputItem);
                return false;
            }
        }

        // Validate result item
        ResourceLocation resultLoc = ResourceLocation.tryParse(recipe.resultItem());
        if (resultLoc == null || !BuiltInRegistries.ITEM.containsKey(resultLoc)) {
            LOGGER.error("Invalid result item ID: {}", recipe.resultItem());
            return false;
        }

        return true;
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
        loadPedestalRecipes();
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

    private static void loadPedestalRecipes() {
        pedestalRecipes.clear();
        for (String recipe : PEDESTAL_RECIPES.get()) {
            try {
                PedestalRecipeData parsedRecipe = parsePedestalRecipe(recipe);
                pedestalRecipes.put(parsedRecipe.name(), parsedRecipe);
                LOGGER.info("Loaded pedestal recipe: {}", parsedRecipe.name());
            } catch (Exception e) {
                LOGGER.error("Failed to load pedestal recipe: {}", recipe, e);
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

    // Add this debug method to your Config.java class

    public static void debugConfigForJEI() {
        Goe.LOGGER.info("=== CONFIG DEBUG FOR JEI ===");

        // Debug pedestal recipes
        Map<String, PedestalRecipeData> pedestalRecipes = getPedestalRecipes();
        Goe.LOGGER.info("Pedestal recipes count: {}", pedestalRecipes.size());

        if (pedestalRecipes.isEmpty()) {
            Goe.LOGGER.warn("PEDESTAL RECIPES ARE EMPTY!");

            // Check if the config value exists
            List<? extends String> configValues = PEDESTAL_RECIPES.get();
            Goe.LOGGER.info("Raw config pedestal recipes count: {}", configValues.size());

            for (String recipe : configValues) {
                Goe.LOGGER.info("Raw recipe: {}", recipe);
            }
        } else {
            for (Map.Entry<String, PedestalRecipeData> entry : pedestalRecipes.entrySet()) {
                Goe.LOGGER.info("Pedestal Recipe: {} = {}", entry.getKey(), entry.getValue());
            }
        }

        // Debug wand catalysts
        Map<Block, ItemStack> wandCatalysts = getWandCatalysts();
        Goe.LOGGER.info("Wand catalysts count: {}", wandCatalysts.size());

        if (wandCatalysts.isEmpty()) {
            Goe.LOGGER.warn("WAND CATALYSTS ARE EMPTY!");

            // Check if the config value exists
            List<? extends String> configValues = WAND_CATALYST_MAPPINGS.get();
            Goe.LOGGER.info("Raw config wand catalyst mappings count: {}", configValues.size());

            for (String mapping : configValues) {
                Goe.LOGGER.info("Raw mapping: {}", mapping);
            }
        } else {
            for (Map.Entry<Block, ItemStack> entry : wandCatalysts.entrySet()) {
                Goe.LOGGER.info("Wand Catalyst: {} -> {}",
                        entry.getKey().getDescriptionId(), entry.getValue());
            }
        }

        Goe.LOGGER.info("=== CONFIG DEBUG END ===");
    }

    public static Map<Block, ItemStack> getWandCatalysts() {
        return Collections.unmodifiableMap(wandCatalysts);
    }

    public static Map<String, PedestalRecipeData> getPedestalRecipes() {
        return Collections.unmodifiableMap(pedestalRecipes);
    }

    public static boolean isPedestalCraftingEnabled() {
        return ENABLE_PEDESTAL_CRAFTING.get();
    }

    public static int getPedestalSearchRadius() {
        return PEDESTAL_SEARCH_RADIUS.get();
    }

    private record CatalystMapping(String blockId, String itemId, int count) {
    }

    public record PedestalRecipeData(String name, String centerItem, String[] inputItems, String resultItem, int resultCount) {

        public ItemStack getCenterItemStack() {
            ResourceLocation loc = ResourceLocation.tryParse(centerItem);
            if (loc != null && BuiltInRegistries.ITEM.containsKey(loc)) {
                return new ItemStack(BuiltInRegistries.ITEM.getValue(loc));
            }
            return ItemStack.EMPTY;
        }

        public List<Ingredient> getInputIngredients() {
            List<Ingredient> ingredients = new ArrayList<>();
            for (String itemId : inputItems) {
                ResourceLocation loc = ResourceLocation.tryParse(itemId);
                if (loc != null && BuiltInRegistries.ITEM.containsKey(loc)) {
                    ingredients.add(Ingredient.of(BuiltInRegistries.ITEM.getValue(loc)));
                }
            }
            return ingredients;
        }

        public ItemStack getResultItemStack() {
            ResourceLocation loc = ResourceLocation.tryParse(resultItem);
            if (loc != null && BuiltInRegistries.ITEM.containsKey(loc)) {
                return new ItemStack(BuiltInRegistries.ITEM.getValue(loc), resultCount);
            }
            return ItemStack.EMPTY;
        }
    }
}