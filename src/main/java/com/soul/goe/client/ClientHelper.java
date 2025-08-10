package com.soul.goe.client;

import com.soul.goe.client.screens.SpellRadialScreen;
import com.soul.goe.items.custom.Wand;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientHelper {

    public static void openSpellRadialMenu(Wand wand, ItemStack wandStack, HolderLookup.Provider registryAccess) {
        NonNullList<ItemStack> boundSpells = wand.getBoundSpells(wandStack, registryAccess);

        List<ItemStack> validSpells = new ArrayList<>();
        List<Integer> originalIndices = new ArrayList<>();

        for (int i = 0; i < boundSpells.size(); i++) {
            ItemStack spell = boundSpells.get(i);
            if (!spell.isEmpty()) {
                validSpells.add(spell);
                originalIndices.add(i);
            }
        }

        if (!validSpells.isEmpty()) {
            int currentSpellIndex = wand.getCurrentSpellIndex(wandStack);
            int displayIndex = originalIndices.indexOf(currentSpellIndex);
            if (displayIndex == -1) displayIndex = 0;

            Minecraft.getInstance().setScreen(new SpellRadialScreen(wandStack, validSpells, originalIndices, displayIndex));
        }
    }
}

// You'll also need to make getCurrentSpellIndex public in Wand.java:
