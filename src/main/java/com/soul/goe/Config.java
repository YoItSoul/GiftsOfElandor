package com.soul.goe;

import com.soul.goe.util.wands.WandMaterialData;
import com.soul.goe.util.wands.WandStats;
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
    private static final Map<String, WandMaterialData> handleMaterials = new ConcurrentHashMap<>();
    private static final Map<String, WandMaterialData> binderMaterials = new ConcurrentHashMap<>();
    private static final Map<String, WandMaterialData> capMaterials = new ConcurrentHashMap<>();

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_SPELL_CRAFTING;
    public static final ModConfigSpec.BooleanValue ENABLE_PEDESTAL_CRAFTING;

    public static final ModConfigSpec.DoubleValue WARDING_BLOCK_RADIUS;
    public static final ModConfigSpec.BooleanValue IGNITE_ENTITIES;
    public static final ModConfigSpec.DoubleValue PARTICLE_SPEED;
    public static final ModConfigSpec.IntValue FIRE_DURATION;
    public static final ModConfigSpec.IntValue MAX_PARTICLES_PER_PACKET;
    public static final ModConfigSpec.IntValue MAX_PARTICLES;
    public static final ModConfigSpec.DoubleValue PUSH_STRENGTH;
    public static final ModConfigSpec.IntValue PEDESTAL_SEARCH_RADIUS;

    public static final ModConfigSpec.DoubleValue BASE_WAND_POWER;
    public static final ModConfigSpec.DoubleValue BASE_WAND_STABILITY;
    public static final ModConfigSpec.DoubleValue BASE_WAND_DURABILITY;
    public static final ModConfigSpec.DoubleValue BASE_WAND_CRITICAL;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> WARDING_WHITELISTED_ENTITIES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PEDESTAL_RECIPES;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> WAND_CATALYST_MAPPINGS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WAND_HANDLE_MATERIALS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WAND_BINDER_MATERIALS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WAND_CAP_MATERIALS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ENABLE_SPELL_CRAFTING = builder.define("enableSpellCrafting", true);
        ENABLE_PEDESTAL_CRAFTING = builder.define("enablePedestalCrafting", true);

        WARDING_BLOCK_RADIUS = builder.defineInRange("wardingBlockRadius", 5.0, 1.0, 16.0);
        IGNITE_ENTITIES = builder.define("igniteEntities", true);
        PARTICLE_SPEED = builder.defineInRange("particleSpeed", 0.1, 0.0, 1.0);
        FIRE_DURATION = builder.defineInRange("fireDuration", 100, 0, 1000);
        MAX_PARTICLES_PER_PACKET = builder.defineInRange("maxParticlesPerPacket", 2, 1, 10);
        MAX_PARTICLES = builder.defineInRange("maxParticles", 10, 1, 50);
        PUSH_STRENGTH = builder.defineInRange("pushStrength", 0.5, 0.1, 2.0);
        PEDESTAL_SEARCH_RADIUS = builder.defineInRange("pedestalSearchRadius", 10, 1, 20);

        BASE_WAND_POWER = builder.defineInRange("baseWandPower", 1.0, 0.1, 10.0);
        BASE_WAND_STABILITY = builder.defineInRange("baseWandStability", 0.9, 0.1, 1.0);
        BASE_WAND_DURABILITY = builder.defineInRange("baseWandDurability", 1.0, 0.1, 10.0);
        BASE_WAND_CRITICAL = builder.defineInRange("baseWandCritical", 0.1, 0.0, 1.0);

        WARDING_WHITELISTED_ENTITIES = builder.define("wardingWhitelistedEntities", getDefaultWhitelistedEntities(), Config::isValidStringList);
        PEDESTAL_RECIPES = builder.define("pedestalRecipes", getDefaultPedestalRecipes(), Config::isValidStringList);
        WAND_CATALYST_MAPPINGS = builder.define("wandCatalysts", getDefaultWandCatalysts(), Config::isValidStringList);
        WAND_HANDLE_MATERIALS = builder.define("wandHandleMaterials", getDefaultHandleMaterials(), Config::isValidStringList);
        WAND_BINDER_MATERIALS = builder.define("wandBinderMaterials", getDefaultBinderMaterials(), Config::isValidStringList);
        WAND_CAP_MATERIALS = builder.define("wandCapMaterials", getDefaultCapMaterials(), Config::isValidStringList);

        SPEC = builder.build();
    }

    private Config() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        loadWandCatalysts();
        loadPedestalRecipes();
        loadWandMaterials();
    }

    private static boolean isValidStringList(Object obj) {
        return obj instanceof List<?> list && list.stream().allMatch(item -> item instanceof String);
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

    private static void loadWandMaterials() {
        handleMaterials.clear();
        binderMaterials.clear();
        capMaterials.clear();

        loadMaterialMap(WAND_HANDLE_MATERIALS.get(), handleMaterials, "handle");
        loadMaterialMap(WAND_BINDER_MATERIALS.get(), binderMaterials, "binder");
        loadMaterialMap(WAND_CAP_MATERIALS.get(), capMaterials, "cap");
    }

    private static void loadMaterialMap(List<? extends String> materials, Map<String, WandMaterialData> materialMap, String type) {
        materials.forEach(material -> {
            try {
                WandMaterialData parsed = WandMaterialData.parse(material);
                materialMap.put(parsed.materialName(), parsed);
                LOGGER.info("Loaded {} material: {}", type, parsed.materialName());
            } catch (Exception e) {
                LOGGER.error("Failed to load {} material: {}", type, material, e);
            }
        });
    }

    private static void addWandCatalyst(CatalystMapping mapping) {
        ResourceLocation blockLoc = ResourceLocation.tryParse(mapping.blockId());
        ResourceLocation itemLoc = ResourceLocation.tryParse(mapping.itemId());

        if (blockLoc == null || itemLoc == null) {
            LOGGER.error("Invalid resource locations: block={}, item={}", mapping.blockId(), mapping.itemId());
            return;
        }

        Block block = BuiltInRegistries.BLOCK.getValue(blockLoc);
        Item item = BuiltInRegistries.ITEM.getValue(itemLoc);

        if (block != null && item != null) {
            wandCatalysts.put(block, new ItemStack(item, mapping.count()));
            LOGGER.info("Registered wand catalyst: {} -> {} x{}", blockLoc, itemLoc, mapping.count());
        }
    }

    public static void debugConfigForJEI() {
        LOGGER.info("=== CONFIG DEBUG FOR JEI ===");
        LOGGER.info("Pedestal recipes count: {}", pedestalRecipes.size());
        LOGGER.info("Wand catalysts count: {}", wandCatalysts.size());
        LOGGER.info("Handle materials count: {}", handleMaterials.size());
        LOGGER.info("Binder materials count: {}", binderMaterials.size());
        LOGGER.info("Cap materials count: {}", capMaterials.size());
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

    public static Map<String, WandMaterialData> getHandleMaterials() {
        return Collections.unmodifiableMap(handleMaterials);
    }

    public static Map<String, WandMaterialData> getBinderMaterials() {
        return Collections.unmodifiableMap(binderMaterials);
    }

    public static Map<String, WandMaterialData> getCapMaterials() {
        return Collections.unmodifiableMap(capMaterials);
    }

    public static WandStats getBaseWandStats() {
        return new WandStats(BASE_WAND_POWER.get().floatValue(), BASE_WAND_STABILITY.get().floatValue(), BASE_WAND_DURABILITY.get().floatValue(), BASE_WAND_CRITICAL.get().floatValue(), "neutral");
    }

    public static String getMaterialNameFromItem(Item item, String partType) {
        Map<String, WandMaterialData> materials = switch (partType) {
            case "handle" -> handleMaterials;
            case "binder" -> binderMaterials;
            case "cap" -> capMaterials;
            default -> Collections.emptyMap();
        };

        return materials.entrySet().stream().filter(entry -> entry.getValue().item().equals(item)).map(Map.Entry::getKey).findFirst().orElse(null);
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

    private static List<String> getDefaultWhitelistedEntities() {
        return List.of("minecraft:player", "minecraft:villager", "minecraft:wandering_trader", "minecraft:iron_golem", "minecraft:snow_golem", "minecraft:allay", "minecraft:axolotl", "minecraft:bat", "minecraft:camel", "minecraft:cat", "minecraft:chicken", "minecraft:cod", "minecraft:cow", "minecraft:donkey", "minecraft:fox", "minecraft:frog", "minecraft:goat", "minecraft:horse", "minecraft:mooshroom", "minecraft:mule", "minecraft:ocelot", "minecraft:parrot", "minecraft:pig", "minecraft:rabbit", "minecraft:salmon", "minecraft:sheep", "minecraft:sniffer", "minecraft:squid", "minecraft:strider", "minecraft:tadpole", "minecraft:tropical_fish", "minecraft:turtle", "minecraft:wolf", "minecraft:bee", "minecraft:dolphin", "minecraft:llama", "minecraft:panda", "minecraft:polar_bear", "minecraft:trader_llama");
    }

    private static List<String> getDefaultPedestalRecipes() {
        return List.of("elandors_charm=minecraft:ender_pearl|goe:salis_mundus,minecraft:gold_ingot,minecraft:gold_ingot,minecraft:gold_ingot,minecraft:gold_ingot->goe:elandors_charm:1", "empowered_lantern=minecraft:lantern|minecraft:glowstone,minecraft:campfire,minecraft:nether_star,goe:salis_mundus->goe:empowered_lantern:1", "warding_lantern=minecraft:sea_lantern|minecraft:totem_of_undying,minecraft:blaze_powder,minecraft:ghast_tear,goe:salis_mundus->goe:warding_lantern:1", "darkened_copper_block=minecraft:copper_block|goe:ash,goe:ash,goe:ash,goe:ash,goe:ash,goe:ash,goe:ash,goe:ash->goe:darkened_copper_block:1", "firebolt_spell=minecraft:book|goe:salis_mundus,minecraft:blaze_powder,minecraft:coal->goe:firebolt_spell:1", "ray_of_frost_spell=minecraft:book|goe:salis_mundus,minecraft:packed_ice,minecraft:prismarine_crystals->goe:ray_of_frost_spell:1", "shocking_grasp_spell=minecraft:book|goe:salis_mundus,minecraft:copper_ingot,minecraft:redstone->goe:shocking_grasp_spell:1", "magic_missile_spell=minecraft:book|goe:salis_mundus,minecraft:ender_pearl,minecraft:glowstone_dust,minecraft:gold_ingot->goe:magic_missile_spell:1", "fireball_spell=minecraft:book|goe:salis_mundus,minecraft:fire_charge,minecraft:blaze_rod,minecraft:gunpowder,minecraft:gold_ingot->goe:fireball_spell:1", "lightning_bolt_spell=minecraft:book|goe:salis_mundus,minecraft:lightning_rod,minecraft:copper_block,minecraft:redstone_block,minecraft:diamond->goe:lightning_bolt_spell:1", "frost_sprite_spell=minecraft:book|goe:salis_mundus,minecraft:blue_ice,minecraft:soul_sand,minecraft:prismarine_shard,minecraft:lapis_block->goe:frost_sprite_spell:1", "arc_sprite_spell=minecraft:book|goe:salis_mundus,minecraft:lightning_rod,minecraft:copper_ingot,minecraft:redstone_dust,minecraft:gold_ingot->goe:arc_sprite_spell:1", "flame_sprite_spell=minecraft:book|goe:salis_mundus,minecraft:blaze_rod,minecraft:magma_block,minecraft:fire_charge,minecraft:gold_ingot->goe:flame_sprite_spell:1", "cone_of_cold_spell=minecraft:book|goe:salis_mundus,minecraft:blue_ice,minecraft:packed_ice,minecraft:prismarine_crystals,minecraft:diamond,minecraft:lapis_block->goe:cone_of_cold_spell:1", "darkened_copper_block=minecraft:copper_block|goe:ash_block,goe:ash_block,goe:ash_block,goe:ash_block->goe:darkened_copper_block:1"

        );
    }

    private static List<String> getDefaultWandCatalysts() {
        return List.of("minecraft:bone_block->goe:salis_mundus:9", "minecraft:bookshelf->minecraft:written_book:1");
    }

    private static List<String> getDefaultHandleMaterials() {
        return List.of(
                "name=stick,item=minecraft:stick,power=0.8,stability=0.9,durability=0.7,critical=0.1",
                "name=bamboo,item=minecraft:bamboo,power=0.6,stability=0.7,durability=0.5,critical=0.8,affinity=air",
                "name=bone,item=minecraft:bone,power=0.7,stability=1.3,durability=1.1,critical=0.1",
                "name=redstone_torch,item=minecraft:redstone_torch,power=1.2,stability=0.8,durability=0.6,critical=0.4,affinity=arc",
                "name=lightning_rod,item=minecraft:lightning_rod,power=1.6,stability=0.5,durability=0.7,critical=0.6,affinity=arc",
                "name=breeze_rod,item=minecraft:breeze_rod,power=0.8,stability=1.4,durability=0.9,critical=0.4,affinity=air",
                "name=blaze_rod,item=minecraft:blaze_rod,power=1.8,stability=0.6,durability=0.8,critical=0.3,affinity=fire",
                "name=soul_torch,item=minecraft:soul_torch,power=1.1,stability=1.2,durability=0.7,critical=0.7,affinity=void",
                "name=end_rod,item=minecraft:end_rod,power=1.4,stability=1.5,durability=1.2,critical=0.4,affinity=void");
    }

    private static List<String> getDefaultBinderMaterials() {
        return List.of(
                "name=string,item=minecraft:string,power=0.8,stability=0.9,durability=0.6,critical=0.2",
                "name=leather,item=minecraft:leather,power=0.7,stability=1.1,durability=1.3,critical=0.1",
                "name=rabbit_hide,item=minecraft:rabbit_hide,power=0.6,stability=1.4,durability=1.1,critical=0.1",
                "name=resin_clump,item=minecraft:resin_clump,power=0.8,stability=1.2,durability=1.4,critical=0.1",
                "name=slime_ball,item=minecraft:slime_ball,power=0.7,stability=1.6,durability=1.2,critical=0.1",
                "name=phantom_membrane,item=minecraft:phantom_membrane,power=0.9,stability=0.8,durability=0.7,critical=1.4,affinity=void",
                "name=magma_cream,item=minecraft:magma_cream,power=1.3,stability=0.7,durability=0.9,critical=0.3,affinity=fire",
                "name=popped_chorus_fruit,item=minecraft:popped_chorus_fruit,power=1.0,stability=0.8,durability=0.6,critical=1.6,affinity=void",
                "name=ghast_tear,item=minecraft:ghast_tear,power=1.2,stability=0.9,durability=0.7,critical=1.8,affinity=void");
    }

    private static List<String> getDefaultCapMaterials() {
        return List.of(
                "name=iron,item=minecraft:iron_ingot,power=1.0,stability=1.2,durability=1.4,critical=0.1",
                "name=gold,item=minecraft:gold_ingot,power=0.8,stability=0.6,durability=0.5,critical=1.2,affinity=arc",
                "name=copper,item=minecraft:copper_ingot,power=0.7,stability=1.3,durability=1.3,critical=0.2,affinity=arc",
                "name=emerald,item=minecraft:emerald,power=1.1,stability=1.1,durability=1.0,critical=0.4",
                "name=fire_charge,item=minecraft:fire_charge,power=1.9,stability=0.5,durability=0.6,critical=0.7,affinity=fire",
                "name=diamond,item=minecraft:diamond,power=0.8,stability=1.8,durability=1.9,critical=0.2,affinity=frost",
                "name=ender_pearl,item=minecraft:ender_pearl,power=1.0,stability=0.7,durability=0.6,critical=1.4,affinity=void",
                "name=amethyst,item=minecraft:amethyst_shard,power=0.9,stability=1.4,durability=1.2,critical=0.9,affinity=frost",
                "name=netherite,item=minecraft:netherite_ingot,power=1.5,stability=1.4,durability=2.1,critical=0.3,affinity=fire",
                "name=beacon,item=minecraft:beacon,power=1.6,stability=1.7,durability=1.1,critical=0.5",
                "name=eye_of_ender,item=minecraft:ender_eye,power=1.3,stability=1.0,durability=0.8,critical=1.6,affinity=void",
                "name=heavy_core,item=minecraft:heavy_core,power=2.0,stability=0.8,durability=1.5,critical=0.6",
                "name=dragon_head,item=minecraft:dragon_head,power=1.7,stability=1.2,durability=1.0,critical=1.3,affinity=void",
                "name=end_crystal,item=minecraft:end_crystal,power=2.2,stability=0.6,durability=0.7,critical=1.8,affinity=void",
                "name=conduit,item=minecraft:conduit,power=1.0,stability=2.3,durability=1.6,critical=0.2,affinity=frost"
        );
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

    public record PedestalRecipeData(String name, String centerItem, String[] inputItems, String resultItem,
                                     int resultCount) {

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

            String[] inputItems = Arrays.stream(arrowParts[0].trim().split(",")).map(String::trim).toArray(String[]::new);

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
            return Arrays.stream(inputItems).map(this::createIngredient).filter(Objects::nonNull).toList();
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

            String[] ingredients = Arrays.stream(arrowParts[0].trim().split(",")).map(String::trim).toArray(String[]::new);

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
            return Arrays.stream(ingredients).map(this::createIngredient).filter(Objects::nonNull).toList();
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