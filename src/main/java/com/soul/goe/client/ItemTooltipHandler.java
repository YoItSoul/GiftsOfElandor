package com.soul.goe.client;

import com.soul.goe.Goe;
import com.soul.goe.api.aspects.Aspect;
import com.soul.goe.api.aspects.AspectList;
import com.soul.goe.registry.ItemAspectRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Goe.MODID, value = Dist.CLIENT)
public class ItemTooltipHandler {
    private static final Component ASPECTS_HEADER = Component.translatable("goe.tooltip.aspects")
            .withStyle(Style.EMPTY.withColor(0xFFFFFF).withBold(true));
    private static final String BULLET_POINT = "• ";
    private static final String ASPECT_SEPARATOR = "  ";

    private static ItemAspectRegistry itemAspectRegistry;

    public static void init(ItemAspectRegistry registry) {
        itemAspectRegistry = registry;
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!itemAspectRegistry.hasAspects(stack.getItem())) {
            return;
        }

        if (!AspectDiscoveryManager.hasDiscoveredAspects(event.getEntity(), stack.getItem())) {
            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.translatable("goe.tooltip.aspects.unknown")
                    .withStyle(Style.EMPTY.withColor(0x888888)));
            return;
        }

        AspectList aspects = itemAspectRegistry.getAspects(stack.getItem());
        if (aspects.isEmpty()) {
            return;
        }

        addTooltipContent(event, aspects);
    }

    private static void addTooltipContent(ItemTooltipEvent event, AspectList aspects) {
        if (!event.getToolTip().isEmpty()) {
            event.getToolTip().add(Component.empty());
        }
        event.getToolTip().add(ASPECTS_HEADER);
        event.getToolTip().addAll(formatAspectLines(aspects));
    }

    private static List<Component> formatAspectLines(AspectList aspects) {
        List<Component> lines = new ArrayList<>();
        Iterator<Map.Entry<Aspect, Integer>> iterator = aspects.getAspects().entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Aspect, Integer> entry = iterator.next();
            Component aspectText = formatAspectText(entry.getKey(), entry.getValue());

            if (iterator.hasNext() && lines.size() % 2 == 0) {
                Map.Entry<Aspect, Integer> nextEntry = iterator.next();
                Component nextAspectText = formatAspectText(nextEntry.getKey(), nextEntry.getValue());
                lines.add(createPairedAspectLine(aspectText, nextAspectText));
            } else {
                lines.add(aspectText);
            }
        }

        return lines;
    }

    private static Component createPairedAspectLine(Component first, Component second) {
        return Component.empty()
                .append(first)
                .append(Component.literal(ASPECT_SEPARATOR))
                .append(second);
    }

    private static Component formatAspectText(Aspect aspect, Integer amount) {
        // Use bright colors to ensure visibility against dark backgrounds
        Style nameStyle = Style.EMPTY
                .withColor(aspect.getColor())
                .withBold(true);

        Style amountStyle = Style.EMPTY
                .withColor(0xFFFFFF); // Pure white for maximum contrast

        // Create the component with shadow for better visibility against dark backgrounds
        return Component.literal(BULLET_POINT)
                .withStyle(Style.EMPTY.withColor(0xFFFFFF))
                .append(Component.literal(aspect.getName().getString())
                        .withStyle(nameStyle))
                .append(Component.literal(" " + amount)
                        .withStyle(amountStyle));
    }
}