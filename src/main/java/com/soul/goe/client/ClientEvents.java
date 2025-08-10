package com.soul.goe.client;

import com.soul.goe.Config;
import com.soul.goe.client.overlay.WandChargingOverlay;
import com.soul.goe.util.wands.WandMaterialData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = "goe", value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        List<Component> tooltip = event.getToolTip();

        if (isWandItem(item)) {
            addWandCraftingTooltip(tooltip);
        }

        String handleMaterial = Config.getMaterialNameFromItem(item, "handle");
        String binderMaterial = Config.getMaterialNameFromItem(item, "binder");
        String capMaterial = Config.getMaterialNameFromItem(item, "cap");

        if (handleMaterial != null) {
            addMaterialTooltip(tooltip, handleMaterial, "handle");
        } else if (binderMaterial != null) {
            addMaterialTooltip(tooltip, binderMaterial, "binder");
        } else if (capMaterial != null) {
            addMaterialTooltip(tooltip, capMaterial, "cap");
        }
    }

    private static boolean isWandItem(Item item) {
        ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
        return itemId.toString().equals("goe:wand");
    }

    private static void addWandCraftingTooltip(List<Component> tooltip) {
        tooltip.add(Component.literal("Can be crafted in:").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("• Crafting Table").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("• Wand Station (3 components)").withStyle(ChatFormatting.YELLOW));
    }

    private static Component formatStatTooltip(String statName, float value) {
        ChatFormatting color;
        String displayValue = String.format("x%.1f", value);

        color = value > 1.0f ? ChatFormatting.GREEN :
                value < 1.0f ? ChatFormatting.RED : ChatFormatting.WHITE;

        return Component.literal("  " + statName + ": " + displayValue).withStyle(color);
    }

    private static void addMaterialTooltip(List<Component> tooltip, String materialName, String partType) {
        Map<String, WandMaterialData> materials = switch (partType) {
            case "handle" -> Config.getHandleMaterials();
            case "binder" -> Config.getBinderMaterials();
            case "cap" -> Config.getCapMaterials();
            default -> Map.of();
        };

        WandMaterialData materialData = materials.get(materialName);
        if (materialData == null) return;

        tooltip.add(Component.literal("Wand " + capitalize(partType) + " Material").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Stats:").withStyle(ChatFormatting.GRAY));

        tooltip.add(formatStatTooltip("Power", materialData.stats().power()));
        tooltip.add(formatStatTooltip("Stability", materialData.stats().stability()));
        tooltip.add(formatStatTooltip("Durability", materialData.stats().durability()));
        tooltip.add(formatStatTooltip("Critical", materialData.stats().critical()));

        if (!materialData.stats().affinity().equals("neutral")) {
            tooltip.add(Component.literal("Affinity: " + capitalize(materialData.stats().affinity())).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @EventBusSubscriber(modid = "goe", value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAbove(
                    VanillaGuiLayers.CROSSHAIR,
                    ResourceLocation.parse("goe:wand_charging"),
                    new WandChargingOverlay()
            );
        }
    }
}