package com.soul.goe.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "goe", value = Dist.CLIENT)
public class ModKeybinds {

    public static final String KEYBIND_CATEGORY = "key.categories.goe";

    public static final KeyMapping SPELL_RADIAL_MENU = new KeyMapping(
            "key.goe.spell_radial",                    // Translation key
            KeyConflictContext.IN_GAME,                // Only active in game
            KeyModifier.NONE,                          // No modifier required
            InputConstants.Type.KEYSYM,                // Keyboard key
            GLFW.GLFW_KEY_R,                          // Default to 'R' key
            KEYBIND_CATEGORY                           // Category
    );

    @SubscribeEvent
    public static void registerKeybinds(RegisterKeyMappingsEvent event) {
        event.register(SPELL_RADIAL_MENU);
    }
}