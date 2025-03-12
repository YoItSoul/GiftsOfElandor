package com.soul.goe.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.soul.goe.Goe;
import com.soul.goe.api.aspects.AspectList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AspectCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("aspects").requires(source -> source.hasPermission(2)) // Requires op level 2
                .executes(context -> {
                    if (context.getSource().getEntity() instanceof Player player) {
                        ItemStack heldItem = player.getMainHandItem();

                        if (heldItem.isEmpty()) {
                            context.getSource().sendFailure(Component.literal("You must hold an item to check its aspects!"));
                            return 0;
                        }

                        AspectList aspects = Goe.ITEM_ASPECT_REGISTRY.getAspects(heldItem.getItem());

                        if (aspects.isEmpty()) {
                            context.getSource().sendSuccess(() -> Component.literal("No aspects found for item: " + heldItem.getDisplayName().getString()), false);
                            return 0;
                        }

                        context.getSource().sendSuccess(() -> Component.literal("Aspects for " + heldItem.getDisplayName().getString() + ":"), false);

                        aspects.getAspects().forEach((aspect, amount) -> context.getSource().sendSuccess(() -> aspect.getName().append(": " + amount), false));

                        return 1;
                    }
                    context.getSource().sendFailure(Component.literal("This command can only be used by players!"));
                    return 0;
                }));
    }
}