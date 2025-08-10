package com.soul.goe.spells.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class SpellCost {
    private final Map<Item, Integer> costs;

    public SpellCost() {
        this.costs = new HashMap<>();
    }

    public SpellCost(Item item, int amount) {
        this.costs = new HashMap<>();
        this.costs.put(item, amount);
    }

    public SpellCost(Map<Item, Integer> costs) {
        this.costs = new HashMap<>(costs);
    }

    public SpellCost addCost(Item item, int amount) {
        this.costs.put(item, amount);
        return this;
    }

    public Map<Item, Integer> getCosts() {
        return new HashMap<>(costs);
    }

    public boolean isEmpty() {
        return costs.isEmpty();
    }

    public boolean canAfford(Player player) {
        if (player.isCreative()) {
            return true;
        }

        for (Map.Entry<Item, Integer> entry : costs.entrySet()) {
            if (player.getInventory().countItem(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public void consume(Player player) {
        if (!canAfford(player)) return;

        for (Map.Entry<Item, Integer> entry : costs.entrySet()) {
            Item item = entry.getKey();
            int needed = entry.getValue();

            for (int i = 0; i < player.getInventory().getContainerSize() && needed > 0; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() == item) {
                    int toRemove = Math.min(needed, stack.getCount());
                    stack.shrink(toRemove);
                    needed -= toRemove;
                }
            }
        }
    }

    public static SpellCost none() {
        return new SpellCost();
    }

    public static SpellCost of(Item item, int amount) {
        return new SpellCost(item, amount);
    }

    public static SpellCost builder() {
        return new SpellCost();
    }
}