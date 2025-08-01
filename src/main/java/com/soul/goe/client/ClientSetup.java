package com.soul.goe.client;

import com.soul.goe.client.screens.SpellBinderScreen;
import com.soul.goe.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = "goe", value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SPELL_BINDER.get(), SpellBinderScreen::new);
    }
}