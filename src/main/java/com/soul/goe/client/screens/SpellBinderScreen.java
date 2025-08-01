package com.soul.goe.client.screens;

import com.soul.goe.menus.SpellBinderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SpellBinderScreen extends AbstractContainerScreen<SpellBinderMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("goe", "textures/gui/spell_binder.png");

    public SpellBinderScreen(SpellBinderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 220;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(
                RenderType::guiTextured,
                TEXTURE,
                this.leftPos,
                this.topPos,
                0.0F,
                0.0F,
                this.imageWidth,
                this.imageHeight,
                this.imageWidth,
                this.imageHeight
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}