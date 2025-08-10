// Replace your entire PedestalCraftingManager.java with this:

package com.soul.goe.blocks.entity;

import com.soul.goe.Config;
import com.soul.goe.items.custom.Wand;
import com.soul.goe.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PedestalCraftingManager {

    // Updated method signature to include player and wand parameters
    public static boolean attemptCrafting(Level level, BlockPos centerPos, PedestalEntity centerPedestal, Player player, ItemStack wandStack) {
        System.out.println("=== PEDESTAL CRAFTING DEBUG START ===");
        System.out.println("Center position: " + centerPos);

        if (!Config.isPedestalCraftingEnabled()) {
            System.out.println("ERROR: Pedestal crafting is disabled in config");
            return false;
        }
        System.out.println("Pedestal crafting is enabled");

        if (centerPedestal.isEmpty()) {
            System.out.println("ERROR: Center pedestal is empty");
            return false;
        }

        ItemStack centerItem = centerPedestal.getDisplayedItem();
        System.out.println("Center item: " + centerItem);

        List<PedestalEntity> nearbyPedestals = findNearbyPedestals(level, centerPos);
        List<ItemStack> availableItems = new ArrayList<>();

        for (PedestalEntity pedestal : nearbyPedestals) {
            if (!pedestal.isEmpty()) {
                ItemStack item = pedestal.getDisplayedItem();
                availableItems.add(item);
                System.out.println("Found available item: " + item + " at position: " + pedestal.getBlockPos());
            }
        }

        System.out.println("Total available items: " + availableItems.size());
        System.out.println("Checking " + Config.getPedestalRecipes().size() + " recipes...");

        for (Config.PedestalRecipeData recipeData : Config.getPedestalRecipes().values()) {
            System.out.println("Checking recipe: " + recipeData.name());
            System.out.println("  Required center: " + recipeData.centerItem());
            System.out.println("  Required inputs: " + String.join(", ", recipeData.inputItems()));
            System.out.println("  Result: " + recipeData.resultItem() + " x" + recipeData.resultCount());

            if (matchesRecipe(centerItem, availableItems, recipeData)) {
                System.out.println("RECIPE MATCHED! Executing: " + recipeData.name());

                List<BlockPos> usedPedestalPositions = consumeIngredients(nearbyPedestals, recipeData.getInputIngredients());

                centerPedestal.setDisplayedItem(recipeData.getResultItemStack());

                if (level instanceof ServerLevel serverLevel) {
                    spawnCraftingParticles(serverLevel, usedPedestalPositions, centerPos);
                }

                // Apply wand durability damage after successful crafting
                if (wandStack.getItem() instanceof Wand && player != null) {
                    // Determine damage based on recipe complexity
                    int baseDamage = calculateCraftingDamage(recipeData);
                    Wand.applyWandDurabilityDamage(wandStack, level, player, baseDamage);
                    System.out.println("Applied " + baseDamage + " durability damage to wand for recipe: " + recipeData.name());
                }

                System.out.println("Pedestal crafting successful! Recipe: " + recipeData.name());
                System.out.println("=== PEDESTAL CRAFTING DEBUG END ===");
                return true;
            } else {
                System.out.println("Recipe does not match");
            }
        }

        System.out.println("No matching recipe found");
        System.out.println("=== PEDESTAL CRAFTING DEBUG END ===");
        return false;
    }

    // Calculate damage based on recipe complexity
    private static int calculateCraftingDamage(Config.PedestalRecipeData recipe) {
        int ingredientCount = recipe.inputItems().length;

        // Base damage scaling:
        // 1-2 ingredients = 1 damage
        // 3-4 ingredients = 2 damage
        // 5+ ingredients = 3 damage
        if (ingredientCount <= 2) {
            return 1;
        } else if (ingredientCount <= 4) {
            return 2;
        } else {
            return 3;
        }
    }

    // Keep the original method for backward compatibility, but without durability damage
    public static boolean attemptCrafting(Level level, BlockPos centerPos, PedestalEntity centerPedestal) {
        return attemptCrafting(level, centerPos, centerPedestal, null, ItemStack.EMPTY);
    }

    private static boolean matchesRecipe(ItemStack centerItem, List<ItemStack> availableItems, Config.PedestalRecipeData recipe) {
        System.out.println("  Matching recipe details:");

        ItemStack requiredCenter = recipe.getCenterItemStack();
        System.out.println("    Center item check: " + centerItem + " vs " + requiredCenter);
        if (!ItemStack.isSameItem(centerItem, requiredCenter)) {
            System.out.println("    CENTER ITEM MISMATCH");
            return false;
        }
        System.out.println("    Center item matches!");

        List<Ingredient> requiredIngredients = recipe.getInputIngredients();
        System.out.println("    Required ingredients count: " + requiredIngredients.size());
        System.out.println("    Available items count: " + availableItems.size());

        if (availableItems.size() < requiredIngredients.size()) {
            System.out.println("    NOT ENOUGH AVAILABLE ITEMS");
            return false;
        }

        for (int i = 0; i < requiredIngredients.size(); i++) {
            Ingredient ingredient = requiredIngredients.get(i);
            System.out.println("    Checking ingredient " + i + ": " + ingredient);

            boolean found = false;
            for (ItemStack available : availableItems) {
                if (ingredient.test(available)) {
                    System.out.println("      Found matching item: " + available);
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("      INGREDIENT NOT FOUND");
                return false;
            }
        }

        System.out.println("    ALL INGREDIENTS MATCH!");
        return true;
    }

    private static List<BlockPos> consumeIngredients(List<PedestalEntity> pedestals, List<Ingredient> ingredients) {
        System.out.println("Consuming ingredients...");
        List<BlockPos> usedPositions = new ArrayList<>();

        for (Ingredient ingredient : ingredients) {
            for (PedestalEntity pedestal : pedestals) {
                if (!pedestal.isEmpty() && ingredient.test(pedestal.getDisplayedItem())) {
                    ItemStack consumed = pedestal.takeItem();
                    usedPositions.add(pedestal.getBlockPos());
                    System.out.println("  Consumed: " + consumed + " from position: " + pedestal.getBlockPos());
                    break;
                }
            }
        }

        return usedPositions;
    }

    private static void spawnCraftingParticles(ServerLevel level, List<BlockPos> fromPositions, BlockPos toPosition) {
        RandomSource random = level.random;
        Vec3 targetPos = Vec3.atCenterOf(toPosition).add(0, 1.2, 0);

        for (BlockPos fromPos : fromPositions) {
            Vec3 startPos = Vec3.atCenterOf(fromPos).add(0, 1.2, 0);

            int particleCount = 15 + random.nextInt(10);

            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;

                Vec3 currentPos = startPos.lerp(targetPos, progress);

                double offsetX = (random.nextDouble() - 0.5) * 0.3;
                double offsetY = (random.nextDouble() - 0.5) * 0.2;
                double offsetZ = (random.nextDouble() - 0.5) * 0.3;

                currentPos = currentPos.add(offsetX, offsetY, offsetZ);

                Vec3 velocity = targetPos.subtract(startPos).normalize().scale(0.1);
                velocity = velocity.add(
                        (random.nextDouble() - 0.5) * 0.05,
                        (random.nextDouble() - 0.5) * 0.05,
                        (random.nextDouble() - 0.5) * 0.05
                );

                level.sendParticles(
                        ParticleTypes.ENCHANT,
                        currentPos.x, currentPos.y, currentPos.z,
                        1,
                        velocity.x, velocity.y, velocity.z,
                        0.02
                );
            }
        }

        for (int i = 0; i < 30; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.8;
            double offsetY = random.nextDouble() * 0.5;
            double offsetZ = (random.nextDouble() - 0.5) * 0.8;

            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    targetPos.x + offsetX,
                    targetPos.y + offsetY,
                    targetPos.z + offsetZ,
                    1,
                    (random.nextDouble() - 0.5) * 0.1,
                    random.nextDouble() * 0.2,
                    (random.nextDouble() - 0.5) * 0.1,
                    0.05
            );
        }
    }

    private static List<PedestalEntity> findNearbyPedestals(Level level, BlockPos center) {
        List<PedestalEntity> pedestals = new ArrayList<>();
        int searchRadius = Config.getPedestalSearchRadius();
        System.out.println("Searching for pedestals within " + searchRadius + " blocks of " + center);

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = center.offset(x, y, z);

                    if (checkPos.equals(center)) continue;

                    if (level.getBlockState(checkPos).is(ModBlocks.PEDESTAL.get())) {
                        BlockEntity blockEntity = level.getBlockEntity(checkPos);
                        if (blockEntity instanceof PedestalEntity pedestalEntity) {
                            pedestals.add(pedestalEntity);
                            System.out.println("  Found pedestal at: " + checkPos + " (distance: " + Math.sqrt(center.distSqr(checkPos)) + ")");
                        }
                    }
                }
            }
        }

        System.out.println("Found " + pedestals.size() + " nearby pedestals within " + searchRadius + " blocks");
        return pedestals;
    }
}