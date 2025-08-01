package com.soul.goe.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = "goe", bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                SpellSelectionPacket.TYPE,
                SpellSelectionPacket.STREAM_CODEC,
                SpellSelectionPacket::handle
        );
    }
}