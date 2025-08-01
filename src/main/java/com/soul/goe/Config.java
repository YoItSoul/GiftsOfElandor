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
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Goe.MODID)
public final class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger("GOE/Config");
    private static final String MAPPING_SEPARATOR = "->";

    private static final Map<Block, ItemStack> wandCatalysts = new ConcurrentHashMap<>();
    private static final Map<String, PedestalRecipeData> pedestalRecipes = new ConcurrentHashMap<>();
    private static final Map<String, SpellRecipeData> spellRecipes = new ConcurrentHashMap<>();

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_SPELL_CRAFTING;
    public static final ModConfigSpec.DoubleValue WARDING_BLOCK_RADIUS;
    public static final ModConfigSpec.BooleanValue IGNITE_ENTITIES;
    public static final ModConfigSpec.DoubleValue PARTICLE_SPEED;
    public static final ModConfigSpec.IntValue FIRE_DURATION;
    public static final ModConfigSpec.IntValue MAX_PARTICLES_PER_PACKET;
    public static final ModConfigSpec.IntValue MAX_PARTICLES;
    public static final ModConfigSpec.DoubleValue PUSH_STRENGTH;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WARDING_WHITELISTED_ENTITIES;
    public static final ModConfigSpec.IntValue PEDESTAL_SEARCH_RADIUS;
    public static final ModConfigSpec.BooleanValue ENABLE_PEDESTAL_CRAFTING;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PEDESTAL_RECIPES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WAND_CATALYST_MAPPINGS;

    private static final List<String> DEFAULT_WHITELISTED_ENTITIES = List.of(
            "minecraft:player", "minecraft:villager", "minecraft:wandering_trader",
            "minecraft:iron_golem", "minecraft:snow_golem", "minecraft:allay",
            "minecraft:axolotl", "minecraft:bat", "minecraft:camel", "minecraft:cat",
            "minecraft:chicken", "minecraft:cod", "minecraft:cow", "minecraft:donkey",
            "minecraft:fox", "minecraft:frog", "minecraft:goat", "minecraft:horse",
            "minecraft:mooshroom", "minecraft:mule", "minecraft:ocelot", "minecraft:parrot",
            "minecraft:pig", "minecraft:rabbit", "minecraft:salmon", "minecraft:sheep",
            "minecraft:sniffer", "minecraft:squid", "minecraft:strider", "minecraft:tadpole",
            "minecraft:tropical_fish", "minecraft:turtle", "minecraft:wolf", "minecraft:bee",
            "minecraft:dolphin", "minecraft:llama", "minecraft:panda", "minecraft:polar_bear",
            "minecraft:trader_llama"
    );

    private static final List<String> DEFAULT_PEDESTAL_RECIPES = List.of(
            "elandors_charm=minecraft:ender_pearl|goe:salis_mundus,minecraft:gold_ingot,minecraft:gold_ingot,minecraft:gold_ingot,minecraft:gold_ingot->goe:elandors_charm:1",
            "empowered_lantern=minecraft:lantern|minecraft:glowstone,minecraft:campfire,minecraft:nether_star,goe:salis_mundus->goe:empowered_lantern:1",
            "warding_lantern=minecraft:sea_lantern|minecraft:totem_of_undying,minecraft:blaze_powder,minecraft:ghast_tear,goe:salis_mundus->goe:warding_lantern:1",

            "firebolt_spell=minecraft:book|goe:salis_mundus,minecraft:blaze_powder,minecraft:coal->goe:firebolt_spell:1",
            "ray_of_frost_spell=minecraft:book|goe:salis_mundus,minecraft:packed_ice,minecraft:prismarine_crystals->goe:ray_of_frost_spell:1",
            "shocking_grasp_spell=minecraft:book|goe:salis_mundus,minecraft:copper_ingot,minecraft:redstone->goe:shocking_grasp_spell:1",
            "magic_missile_spell=minecraft:book|goe:salis_mundus,minecraft:ender_pearl,minecraft:glowstone_dust,minecraft:gold_ingot->goe:magic_missile_spell:1",
            "lunge_spell=minecraft:book|goe:salis_mundus,minecraft:rabbit_foot,minecraft:sugar->goe:lunge_spell:1",
            "levitate_spell=minecraft:book|goe:salis_mundus,minecraft:phantom_membrane,minecraft:feather,minecraft:shulker_shell->goe:levitate_spell:1",
            "fireball_spell=minecraft:book|goe:salis_mundus,minecraft:fire_charge,minecraft:blaze_rod,minecraft:gunpowder,minecraft:gold_ingot->goe:fireball_spell:1",
            "lightning_bolt_spell=minecraft:book|goe:salis_mundus,minecraft:lightning_rod,minecraft:copper_block,minecraft:redstone_block,minecraft:diamond->goe:lightning_bolt_spell:1",
            "fly_spell=minecraft:book|goe:salis_mundus,minecraft:elytra,minecraft:phantom_membrane,minecraft:feather,minecraft:nether_star,minecraft:diamond->goe:fly_spell:1",
            "frost_sprite_spell=minecraft:book|goe:salis_mundus,minecraft:blue_ice,minecraft:soul_sand,minecraft:prismarine_shard,minecraft:lapis_block->goe:frost_sprite_spell:1",
            "arc_sprite_spell=minecraft:book|goe:salis_mundus,minecraft:lightning_rod,minecraft:copper_ingot,minecraft:redstone_dust,minecraft:gold_ingot->goe:arc_sprite_spell:1",
            "flame_sprite_spell=minecraft:book|goe:salis_mundus,minecraft:blaze_rod,minecraft:magma_block,minecraft:fire_charge,minecraft:gold_ingot->goe:flame_sprite_spell:1",
            "cone_of_cold_spell=minecraft:book|goe:salis_mundus,minecraft:blue_ice,minecraft:packed_ice,minecraft:prismarine_crystals,minecraft:diamond,minecraft:lapis_block->goe:cone_of_cold_spell:1"
    );

    private static final List<String> DEFAULT_WAND_CATALYSTS = List.of(
            "minecraft:bone_block->goe:salis_mundus:9",
            "minecraft:bookshelf->minecraft:written_book:1"
    );

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Spell Crafting Settings");
        ENABLE_SPELL_CRAFTING = builder
                .comment("Enable spell crafting system")
                .define("enableSpellCrafting", true);

        builder.comment("Warding Lantern Settings");
        WARDING_BLOCK_RADIUS = builder
                .comment("The radius in blocks that the warding lantern will affect entities")
                .defineInRange("wardingBlockRadius", 5.0, 1.0, 16.0);

        IGNITE_ENTITIES = builder
                .comment("Whether the warding lantern should set entities on fire")
                .define("igniteEntities", true);

        PARTICLE_SPEED = builder
                .comment("The speed of the warding lantern particles")
                .defineInRange("particleSpeed", 0.1, 0.0, 1.0);

        FIRE_DURATION = builder
                .comment("How long entities should burn when hit by the warding lantern (in ticks)")
                .defineInRange("fireDuration", 100, 0, 1000);

        MAX_PARTICLES_PER_PACKET = builder
                .comment("Maximum number of particles per packet for the warding lantern")
                .defineInRange("maxParticlesPerPacket", 2, 1, 10);

        MAX_PARTICLES = builder
                .comment("Maximum total number of particles for the warding lantern")
                .defineInRange("maxParticles", 10, 1, 50);

        PUSH_STRENGTH = builder
                .comment("The strength with which entities are pushed back by the warding lantern")
                .defineInRange("pushStrength", 0.5, 0.1, 2.0);

        WARDING_WHITELISTED_ENTITIES = builder
                .comment("List of entity types that won't be affected by the warding lantern")
                .define("wardingWhitelistedEntities", DEFAULT_WHITELISTED_ENTITIES, Config::isValidStringList);

        builder.comment("Pedestal Crafting Settings");
        ENABLE_PEDESTAL_CRAFTING = builder
                .comment("Enable pedestal crafting system")
                .define("enablePedestalCrafting", true);

        PEDESTAL_SEARCH_RADIUS = builder
                .comment("Search radius for pedestal crafting (in blocks)")
                .defineInRange("pedestalSearchRadius", 10, 1, 20);

        PEDESTAL_RECIPES = builder
                .comment("Pedestal recipes in format: name=center_item|input1,input2,input3->result_item:count")
                .define("pedestalRecipes", DEFAULT_PEDESTAL_RECIPES, Config::isValidStringList);

        WAND_CATALYST_MAPPINGS = builder
                .comment("List of block-to-item conversion mappings for the wand in format: blockid->itemid:count")
                .define("wandCatalysts", DEFAULT_WAND_CATALYSTS, Config::isValidStringList);

        SPEC = builder.build();
    }

    private Config() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        loadWandCatalysts();
        loadPedestalRecipes();
    }

    private static boolean isValidStringList(Object obj) {
        return obj instanceof List<?> list &&
                list.stream().allMatch(item -> item instanceof String);
    }

    private static void loadWandCatalysts() {
        wandCatalysts.clear();
        WAND_CATALYST_MAPPINGS.get().forEach(mapping -> {
            try {
                CatalystMapping parsed = CatalystMapping.parse(mapping);
                addWandCatalyst(parsed);
            } catch (Exception e) {
                LOGGER.error("Failed to load catalyst mapping: {}", mapping, e);
            }
        });
    }

    private static void loadPedestalRecipes() {
        pedestalRecipes.clear();
        PEDESTAL_RECIPES.get().forEach(recipe -> {
            try {
                PedestalRecipeData parsed = PedestalRecipeData.parse(recipe);
                pedestalRecipes.put(parsed.name(), parsed);
                LOGGER.info("Loaded pedestal recipe: {}", parsed.name());
            } catch (Exception e) {
                LOGGER.error("Failed to load pedestal recipe: {}", recipe, e);
            }
        });
    }

    private static void addWandCatalyst(CatalystMapping mapping) {
        ResourceLocation blockLoc = ResourceLocation.tryParse(mapping.blockId());
        ResourceLocation itemLoc = ResourceLocation.tryParse(mapping.itemId());

        if (blockLoc == null || itemLoc == null) {
            LOGGER.error("Invalid resource locations: block={}, item={}",
                    mapping.blockId(), mapping.itemId());
            return;
        }

        Block block = BuiltInRegistries.BLOCK.getValue(blockLoc);
        Item item = BuiltInRegistries.ITEM.getValue(itemLoc);

        if (block != null && item != null) {
            wandCatalysts.put(block, new ItemStack(item, mapping.count()));
            LOGGER.info("Registered wand catalyst: {} -> {} x{}",
                    blockLoc, itemLoc, mapping.count());
        }
    }

    public static void debugConfigForJEI() {
        LOGGER.info("=== CONFIG DEBUG FOR JEI ===");

        LOGGER.info("Pedestal recipes count: {}", pedestalRecipes.size());
        if (pedestalRecipes.isEmpty()) {
            LOGGER.warn("PEDESTAL RECIPES ARE EMPTY!");
            LOGGER.info("Raw config pedestal recipes count: {}", PEDESTAL_RECIPES.get().size());
            PEDESTAL_RECIPES.get().forEach(recipe -> LOGGER.info("Raw recipe: {}", recipe));
        } else {
            pedestalRecipes.forEach((key, value) ->
                    LOGGER.info("Pedestal Recipe: {} = {}", key, value));
        }

        LOGGER.info("Wand catalysts count: {}", wandCatalysts.size());
        if (wandCatalysts.isEmpty()) {
            LOGGER.warn("WAND CATALYSTS ARE EMPTY!");
            LOGGER.info("Raw config wand catalyst mappings count: {}", WAND_CATALYST_MAPPINGS.get().size());
            WAND_CATALYST_MAPPINGS.get().forEach(mapping -> LOGGER.info("Raw mapping: {}", mapping));
        } else {
            wandCatalysts.forEach((block, itemStack) ->
                    LOGGER.info("Wand Catalyst: {} -> {}", block.getDescriptionId(), itemStack));
        }

        LOGGER.info("=== CONFIG DEBUG END ===");
    }

    public static Map<Block, ItemStack> getWandCatalysts() {
        return Collections.unmodifiableMap(wandCatalysts);
    }

    public static Map<String, PedestalRecipeData> getPedestalRecipes() {
        return Collections.unmodifiableMap(pedestalRecipes);
    }

    public static Map<String, SpellRecipeData> getSpellRecipes() {
        return Collections.unmodifiableMap(spellRecipes);
    }

    public static boolean isPedestalCraftingEnabled() {
        return ENABLE_PEDESTAL_CRAFTING.get();
    }

    public static boolean isSpellCraftingEnabled() {
        return ENABLE_SPELL_CRAFTING.get();
    }

    public static int getPedestalSearchRadius() {
        return PEDESTAL_SEARCH_RADIUS.get();
    }

    private record CatalystMapping(String blockId, String itemId, int count) {

        public static CatalystMapping parse(String mapping) {
            String[] parts = mapping.split(MAPPING_SEPARATOR);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid mapping format: " + mapping);
            }

            String blockId = parts[0].trim();
            String itemPart = parts[1].trim();

            int lastColon = itemPart.lastIndexOf(':');
            if (lastColon == -1) {
                throw new IllegalArgumentException("Invalid item format: " + itemPart);
            }

            String itemId = itemPart.substring(0, lastColon);
            int count = Integer.parseInt(itemPart.substring(lastColon + 1).trim());

            if (count <= 0) {
                throw new IllegalArgumentException("Invalid item count: " + count);
            }

            return new CatalystMapping(blockId, itemId, count);
        }
    }

    public record PedestalRecipeData(String name, String centerItem, String[] inputItems,
                                     String resultItem, int resultCount) {

        public static PedestalRecipeData parse(String recipe) {
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

            String[] inputItems = Arrays.stream(arrowParts[0].trim().split(","))
                    .map(String::trim)
                    .toArray(String[]::new);

            String resultPart = arrowParts[1].trim();
            int lastColon = resultPart.lastIndexOf(':');
            if (lastColon == -1) {
                throw new IllegalArgumentException("Invalid result format");
            }

            String resultItem = resultPart.substring(0, lastColon);
            int resultCount = Integer.parseInt(resultPart.substring(lastColon + 1).trim());

            if (resultCount <= 0) {
                throw new IllegalArgumentException("Invalid result count: " + resultCount);
            }

            return new PedestalRecipeData(name, centerItem, inputItems, resultItem, resultCount);
        }

        public ItemStack getCenterItemStack() {
            return createItemStack(centerItem, 1);
        }

        public List<Ingredient> getInputIngredients() {
            return Arrays.stream(inputItems)
                    .map(this::createIngredient)
                    .filter(Objects::nonNull)
                    .toList();
        }

        public ItemStack getResultItemStack() {
            return createItemStack(resultItem, resultCount);
        }

        private ItemStack createItemStack(String itemId, int count) {
            ResourceLocation loc = ResourceLocation.tryParse(itemId);
            if (loc != null && BuiltInRegistries.ITEM.containsKey(loc)) {
                return new ItemStack(BuiltInRegistries.ITEM.getValue(loc), count);
            }
            return ItemStack.EMPTY;
        }

        private Ingredient createIngredient(String itemId) {
            ResourceLocation loc = ResourceLocation.tryParse(itemId);
            if (loc != null && BuiltInRegistries.ITEM.containsKey(loc)) {
                return Ingredient.of(BuiltInRegistries.ITEM.getValue(loc));
            }
            return null;
        }
    }

    public record SpellRecipeData(String name, String[] ingredients, String resultSpell, String spellName) {

        public static SpellRecipeData parse(String recipe) {
            int firstEquals = recipe.indexOf('=');
            if (firstEquals == -1) {
                throw new IllegalArgumentException("Invalid spell recipe format - missing name separator");
            }

            String name = recipe.substring(0, firstEquals).trim();
            String recipePart = recipe.substring(firstEquals + 1);

            String[] arrowParts = recipePart.split(MAPPING_SEPARATOR);
            if (arrowParts.length != 2) {
                throw new IllegalArgumentException("Invalid spell recipe format - missing arrow separator");
            }

            String[] ingredients = Arrays.stream(arrowParts[0].trim().split(","))
                    .map(String::trim)
                    .toArray(String[]::new);

            String resultPart = arrowParts[1].trim();
            int lastColon = resultPart.lastIndexOf(':');
            if (lastColon == -1) {
                throw new IllegalArgumentException("Invalid spell result format");
            }

            String resultSpell = resultPart.substring(0, lastColon).trim();
            String spellName = resultPart.substring(lastColon + 1).trim();

            return new SpellRecipeData(name, ingredients, resultSpell, spellName);
        }

        public List<String> getIngredientList() {
            return Arrays.asList(ingredients);
        }

        public List<Ingredient> getIngredientIngredients() {
            return Arrays.stream(ingredients)
                    .map(this::createIngredient)
                    .filter(Objects::nonNull)
                    .toList();
        }

        private Ingredient createIngredient(String itemId) {
            ResourceLocation loc = ResourceLocation.tryParse(itemId);
            if (loc != null && BuiltInRegistries.ITEM.containsKey(loc)) {
                return Ingredient.of(BuiltInRegistries.ITEM.getValue(loc));
            }
            return null;
        }
    }
}