package com.soul.goe.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.soul.goe.items.custom.Wand;
import com.soul.goe.util.wands.WandStats;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WandChargingOverlay implements LayeredDraw.Layer {
    private static final int BAR_WIDTH = 20;
    private static final int BAR_HEIGHT = 2;
    private static final int BAR_OFFSET_Y = 5;
    private static final int BORDER_WIDTH = 1;

    private static final int BACKGROUND_COLOR = 0x80000000;
    private static final int BORDER_COLOR = 0xFF404040;
    private static final int CHARGING_COLOR = 0xFFFFFFFF;
    private static final int CHARGED_COLOR = 0xFFAA00FF;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || minecraft.options.hideGui) {
            return;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof Wand wand)) {
            return;
        }

        if (!player.isUsingItem() || player.getUseItem() != mainHandItem) {
            return;
        }

        WandStats wandStats = Wand.getWandStats(mainHandItem);
        if (wandStats == null) {
            return;
        }

        float totalStats = wandStats.power() + wandStats.stability() + wandStats.durability() + wandStats.critical();
        int requiredCastTime = Math.round(totalStats * 20);
        int useDuration = player.getTicksUsingItem();

        float progress = Math.min(1.0f, (float) useDuration / requiredCastTime);
        boolean isCharged = progress >= 1.0f;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int barX = (screenWidth - BAR_WIDTH) / 2;
        int barY = (screenHeight / 2) + BAR_OFFSET_Y;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        guiGraphics.fill(
                barX - BORDER_WIDTH,
                barY - BORDER_WIDTH,
                barX + BAR_WIDTH + BORDER_WIDTH,
                barY + BAR_HEIGHT + BORDER_WIDTH,
                BORDER_COLOR
        );

        guiGraphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, BACKGROUND_COLOR);

        int fillWidth = Math.round(BAR_WIDTH * progress);
        int fillColor = isCharged ? CHARGED_COLOR : CHARGING_COLOR;

        if (fillWidth > 0) {
            guiGraphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, fillColor);
        }

        if (isCharged) {
            float pulse = (float) Math.sin(System.currentTimeMillis() * 0.01) * 0.3f + 0.7f;
            int glowColor = (int) (pulse * 255) << 24 | (CHARGED_COLOR & 0xFFFFFF);

            guiGraphics.fill(
                    barX - 1,
                    barY - 1,
                    barX + BAR_WIDTH + 1,
                    barY + BAR_HEIGHT + 1,
                    glowColor
            );
        }

        RenderSystem.disableBlend();
    }
}