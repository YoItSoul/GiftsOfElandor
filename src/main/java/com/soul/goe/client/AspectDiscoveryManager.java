package com.soul.goe.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class AspectDiscoveryManager {
    // Map player UUID to their discovered items
    private static final Map<UUID, Set<ResourceLocation>> discoveredItems = new HashMap<>();

    public static void discoverItemAspects(Player player, Item item) {
        Set<ResourceLocation> playerDiscoveries = discoveredItems
                .computeIfAbsent(player.getUUID(), k -> new HashSet<>());
        playerDiscoveries.add(BuiltInRegistries.ITEM.getKey(item));
    }

    public static boolean hasDiscoveredAspects(Player player, Item item) {
        Set<ResourceLocation> playerDiscoveries = discoveredItems.get(player.getUUID());
        if (playerDiscoveries == null) return false;
        return playerDiscoveries.contains(BuiltInRegistries.ITEM.getKey(item));
    }
}