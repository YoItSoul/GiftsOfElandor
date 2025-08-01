package com.soul.goe.network;

import com.soul.goe.items.custom.Wand;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SpellSelectionPacket(int spellIndex) implements CustomPacketPayload {

    public static final Type<SpellSelectionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("goe", "spell_selection"));

    public static final StreamCodec<ByteBuf, SpellSelectionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SpellSelectionPacket::spellIndex,
            SpellSelectionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SpellSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ItemStack mainHandItem = serverPlayer.getMainHandItem();
                if (mainHandItem.getItem() instanceof Wand wand) {
                    wand.setCurrentSpell(mainHandItem, packet.spellIndex());
                }
            }
        });
    }
}