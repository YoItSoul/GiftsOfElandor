package com.soul.goe.client;

import com.soul.goe.items.custom.Wand;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "goe", value = Dist.CLIENT)
public class ClientInputHandler {

    private static boolean wasKeyPressed = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        Player player = minecraft.player;
        ItemStack mainHandItem = player.getMainHandItem();

        if (!(mainHandItem.getItem() instanceof Wand wand)) {
            wasKeyPressed = false;
            return;
        }

        boolean isRadialMenuOpen = minecraft.screen instanceof com.soul.goe.client.screens.SpellRadialScreen;

        if (isRadialMenuOpen) {
            wasKeyPressed = ModKeybinds.SPELL_RADIAL_MENU.isDown();
            return;
        }

        boolean isKeyPressed = ModKeybinds.SPELL_RADIAL_MENU.isDown();

        if (isKeyPressed && !wasKeyPressed) {
            openSpellRadialMenu(wand, mainHandItem, player);
        }

        wasKeyPressed = isKeyPressed;
    }

    private static void openSpellRadialMenu(Wand wand, ItemStack wandStack, Player player) {
        wand.openSpellRadialMenuFromClient(wandStack, player.level().registryAccess());
    }

    public static void setRadialMenuOpen(boolean open) {
    }
}