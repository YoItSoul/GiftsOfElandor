package com.soul.goe.client.screens;

import com.soul.goe.client.menus.SpellBinderMenu;
import com.soul.goe.util.wands.WandStats;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public class SpellBinderScreen extends AbstractContainerScreen<SpellBinderMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("goe", "textures/gui/spell_binder.png");

    public SpellBinderScreen(SpellBinderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 245;
        this.inventoryLabelY = this.imageHeight - 82;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(RenderType::guiTextured, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        if (menu.isWandDamaged() || menu.wandHasSpells()) {
            drawDamageWarning(guiGraphics);
        }

        if (!menu.canModifyParts()) {
            drawDisabledPartSlots(guiGraphics);
        }
    }

    private void drawDamageWarning(GuiGraphics guiGraphics) {
        int startX = this.leftPos + 58;
        int startY = this.topPos + 40;
        int width = 60;
        int height = 16;

        guiGraphics.fill(startX, startY, startX + width, startY + height, 0x44FF0000);

        Component warningText;
        if (menu.isWandDamaged()) {
            warningText = Component.literal("Repair Wand First!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        } else if (menu.wandHasSpells()) {
            warningText = Component.literal("Remove Spells First!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        } else {
            warningText = Component.literal("Cannot Modify!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        }

        int textWidth = this.font.width(warningText);
        int textX = startX + (width - textWidth) / 2;
        int textY = startY + 5;

        guiGraphics.drawString(this.font, warningText, textX, textY, 0xFFFFFF);
    }

    private void drawDisabledPartSlots(GuiGraphics guiGraphics) {
        for (int i = 1; i <= 3; i++) {
            Slot slot = menu.getSlot(i);
            if (slot.getItem().isEmpty()) {
                int slotX = this.leftPos + slot.x;
                int slotY = this.topPos + slot.y;

                guiGraphics.fill(slotX + 2, slotY + 2, slotX + 14, slotY + 4, 0xFF666666);
                guiGraphics.fill(slotX + 2, slotY + 12, slotX + 14, slotY + 14, 0xFF666666);
                guiGraphics.fill(slotX + 2, slotY + 2, slotX + 4, slotY + 14, 0xFF666666);
                guiGraphics.fill(slotX + 12, slotY + 2, slotX + 14, slotY + 14, 0xFF666666);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);

        drawWandStats(guiGraphics);
    }

    private void drawWandStats(GuiGraphics guiGraphics) {
        WandStats stats = menu.getCurrentWandStats();
        if (stats == null) {
            return;
        }

        int startX = 27;
        int startY = 85;
        int lineHeight = 10;
        int statNameWidth = 28;

        drawStatBar(guiGraphics, "PWR", stats.power(), startX, startY, statNameWidth);
        drawStatBar(guiGraphics, "STB", stats.stability(), startX, startY + lineHeight, statNameWidth);
        drawStatBar(guiGraphics, "DUR", stats.durability(), startX, startY + lineHeight * 2, statNameWidth);
        drawStatBar(guiGraphics, "CRIT", stats.critical(), startX, startY + lineHeight * 3, statNameWidth);

        // Add affinity display if not neutral
        if (!stats.affinity().equals("neutral")) {
            drawAffinityDisplay(guiGraphics, stats.affinity(), startX, startY + lineHeight * 4);
        }
    }

    private void drawStatBar(GuiGraphics guiGraphics, String statName, float value, int x, int y, int statNameWidth) {
        int barWidth = 65;
        int barHeight = 8;

        Component statText = Component.literal(statName + ":");
        guiGraphics.drawString(this.font, statText, x, y, 0xc6c6c6, true);

        int barX = x + statNameWidth;
        int barY = y;

        guiGraphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, 0xFF000000);
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);

        String displayText;
        float barFillPercentage;
        int textColor = 0x404040;

        switch (statName.toLowerCase()) {

            case "crit" -> {
                // Keep critical as percentage
                displayText = String.format("%.1f%%", value * 100);
                barFillPercentage = Math.min(value * 500f, 100f);
                textColor = value > 0.1f ? 0x44FF44 : value < 0.1f ? 0xFF4444 : 0x404040;
            }
            default -> {
                // PWR and STB as multipliers
                displayText = String.format("x%.1f", value);
                barFillPercentage = Math.min(value * 50f, 100f); // Scale multiplier to fill bar
                textColor = value > 1.0f ? 0x44FF44 : value < 1.0f ? 0xFF4444 : 0x404040;
            }
        }

        int fillWidth = (int) (barWidth * (barFillPercentage / 100f));
        int barColor = getStatBarColor(value);

        if (fillWidth > 0) {
            guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight, barColor);
        }

        int centerX = barX + barWidth / 2;
        guiGraphics.fill(centerX, barY, centerX + 1, barY + barHeight, 0xFF888888);

        int textX = barX + barWidth + 4;
        guiGraphics.drawString(this.font, Component.literal(displayText), textX, y, textColor, true);
    }

    private int getStatBarColor(float value) {
        if (value >= 2.0f) {
            long time = System.currentTimeMillis();
            float shimmer = (float) (Math.sin(time * 0.008) * 0.3 + 0.7);
            int r = (int) (0xAA * shimmer);
            int b = (int) (0xAA * shimmer);
            return 0xFF000000 | (r << 16) | b;
        } else if (value > 1.0f) {
            return 0xFF44FF44;
        } else if (value < 1.0f) {
            return 0xFFFF4444;
        } else {
            return 0xFFAA00AA;
        }
    }

    private String formatMaterialName(String materialName) {
        return materialName.substring(0, 1).toUpperCase() + materialName.substring(1).replace("_", " ");
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        // Check for stat bar tooltips first
        if (isHoveringStatBars(x, y)) {
            String hoveredStat = getHoveredStat(x, y);
            if (hoveredStat != null) {
                List<Component> tooltip = getStatTooltip(hoveredStat);
                if (!tooltip.isEmpty()) {
                    renderTooltipToLeft(guiGraphics, tooltip, x, y);
                    return; // Don't show other tooltips if showing stat tooltip
                }
            }
        }

        if (isHoveringPartSlots(x, y) && !menu.canModifyParts()) {
            List<Component> tooltip = new ArrayList<>();

            if (menu.isWandDamaged()) {
                tooltip.add(Component.literal("Wand is damaged!").withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal("Repair it before modifying parts.").withStyle(ChatFormatting.GRAY));
            } else if (menu.wandHasSpells()) {
                tooltip.add(Component.literal("Wand has bound spells!").withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal("Remove all spells before modifying parts.").withStyle(ChatFormatting.GRAY));
            } else if (!menu.hasWand()) {
                tooltip.add(Component.literal("Place a wand first").withStyle(ChatFormatting.GRAY));
            }

            if (!tooltip.isEmpty()) {
                renderTooltipToLeft(guiGraphics, tooltip, x, y);
            }
        }

        if (menu.canModifyParts()) {
            Slot hoveredSlot = getSlotUnderMouse();
            if (hoveredSlot != null && menu.isPartSlot(hoveredSlot.index)) {
                List<Component> tooltip = new ArrayList<>();
                String partType = menu.getPartType(hoveredSlot.index);

                tooltip.add(Component.literal(formatMaterialName(partType) + " Slot").withStyle(ChatFormatting.BLUE));

                switch (partType) {
                    case "handle" ->
                            tooltip.add(Component.literal("Affects: Durability, Stability").withStyle(ChatFormatting.GRAY));
                    case "binder" ->
                            tooltip.add(Component.literal("Affects: Power, Critical").withStyle(ChatFormatting.GRAY));
                    case "cap" ->
                            tooltip.add(Component.literal("Affects: Power, Stability, Durability").withStyle(ChatFormatting.GRAY));
                }

                renderTooltipToLeft(guiGraphics, tooltip, x, y);
            }
        }
    }

    private boolean isHoveringStatBars(int mouseX, int mouseY) {
        int relativeX = mouseX - this.leftPos;
        int relativeY = mouseY - this.topPos;

        // Match the actual coordinates from drawWandStats
        int startX = 27;
        int startY = 85;
        int lineHeight = 10;  // This matches your drawWandStats lineHeight
        int maxLines = 5; // 4 stats + potential affinity
        int statWidth = 105; // Approximate width of entire stat display area

        return relativeX >= startX && relativeX <= startX + statWidth &&
                relativeY >= startY && relativeY <= startY + (lineHeight * maxLines);
    }

    private String getHoveredStat(int mouseX, int mouseY) {
        int relativeX = mouseX - this.leftPos;
        int relativeY = mouseY - this.topPos;

        // Use the EXACT same coordinates as drawWandStats
        int startX = 27;
        int startY = 85;
        int lineHeight = 10;  // This matches your drawWandStats lineHeight
        int statWidth = 105; // Approximate width of stat name + bar + value

        if (relativeX >= startX && relativeX <= startX + statWidth) {
            if (relativeY >= startY && relativeY < startY + lineHeight) {
                return "PWR";
            } else if (relativeY >= startY + lineHeight && relativeY < startY + (lineHeight * 2)) {
                return "STB";
            } else if (relativeY >= startY + (lineHeight * 2) && relativeY < startY + (lineHeight * 3)) {
                return "DUR";
            } else if (relativeY >= startY + (lineHeight * 3) && relativeY < startY + (lineHeight * 4)) {
                return "CRIT";
            } else if (relativeY >= startY + (lineHeight * 4) && relativeY < startY + (lineHeight * 5)) {
                // Check if there's an affinity display
                WandStats stats = menu.getCurrentWandStats();
                if (stats != null && !stats.affinity().equals("neutral")) {
                    return "AFF";
                }
            }
        }

        return null;
    }

    private List<Component> getStatTooltip(String statName) {
        List<Component> tooltip = new ArrayList<>();

        switch (statName.toUpperCase()) {
            case "PWR" -> {
                tooltip.add(Component.literal("Power").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                tooltip.add(Component.literal("Affects spell damage and velocity").withStyle(ChatFormatting.WHITE));
                tooltip.add(Component.literal("Higher power = stronger, faster spells").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("Influenced most by: Binder materials").withStyle(ChatFormatting.DARK_AQUA));
            }
            case "STB" -> {
                tooltip.add(Component.literal("Stability").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                tooltip.add(Component.literal("Affects spell success rate and accuracy").withStyle(ChatFormatting.WHITE));
                tooltip.add(Component.literal("Higher stability = more reliable spells").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Also reduces casting time").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("Influenced most by: Handle materials").withStyle(ChatFormatting.DARK_AQUA));
            }
            case "DUR" -> {
                tooltip.add(Component.literal("Durability").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                tooltip.add(Component.literal("Affects wand longevity and spell range").withStyle(ChatFormatting.WHITE));
                tooltip.add(Component.literal("Higher durability = longer lasting wand").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Also increases maximum spell range").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("Influenced most by: Handle materials").withStyle(ChatFormatting.DARK_AQUA));
            }
            case "CRIT" -> {
                tooltip.add(Component.literal("Critical Hit Chance").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                tooltip.add(Component.literal("Chance for spells to deal extra damage").withStyle(ChatFormatting.WHITE));
                tooltip.add(Component.literal("Higher critical = more frequent crits").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("Influenced most by: Binder materials").withStyle(ChatFormatting.DARK_AQUA));
            }
            case "AFF" -> {
                WandStats stats = menu.getCurrentWandStats();
                if (stats != null) {
                    String affinity = formatMaterialName(stats.affinity());
                    tooltip.add(Component.literal("Affinity: " + affinity).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                    tooltip.add(Component.literal("Determines which spells can be empowered").withStyle(ChatFormatting.WHITE));
                    tooltip.add(Component.literal("Matching affinity = enhanced spell effects").withStyle(ChatFormatting.GRAY));
                    tooltip.add(Component.empty());
                    tooltip.add(Component.literal("Determined by: Binder material primarily").withStyle(ChatFormatting.DARK_AQUA));
                }
            }
        }

        return tooltip;
    }

    private void drawAffinityDisplay(GuiGraphics guiGraphics, String affinity, int x, int y) {
        String displayName = formatMaterialName(affinity);
        Component affinityText = Component.literal("AFF: " + displayName);

        // Choose color based on affinity type
        int affinityColor = switch (affinity.toLowerCase()) {
            case "fire" -> 0xFFFF4444;
            case "ice" -> 0xFF44DDFF;
            case "arc" -> 0xFFFFFF44;
            case "nature" -> 0xFF44FF44;
            case "dark" -> 0xFF884488;
            case "void" -> 0xFF8844FF;
            case "air" -> 0xFFCCCCCC;
            default -> 0xFF888888;
        };

        guiGraphics.drawString(this.font, affinityText, x, y, affinityColor, true);
    }

    private void renderTooltipToLeft(GuiGraphics guiGraphics, List<Component> tooltip, int x, int y) {
        if (tooltip.isEmpty()) return;

        int maxWidth = 0;
        for (Component component : tooltip) {
            maxWidth = Math.max(maxWidth, this.font.width(component));
        }

        int tooltipX = x - maxWidth - 10;
        int tooltipY = y;

        if (tooltipX < 4) {
            tooltipX = 4;
        }

        guiGraphics.renderComponentTooltip(this.font, tooltip, tooltipX, tooltipY);
    }

    private boolean isHoveringPartSlots(int mouseX, int mouseY) {
        int relativeX = mouseX - this.leftPos;
        int relativeY = mouseY - this.topPos;

        return relativeX >= 44 && relativeX <= 132 && relativeY >= 45 && relativeY <= 63;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}