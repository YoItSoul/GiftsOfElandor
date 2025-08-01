package com.soul.goe.client.screens;

import com.soul.goe.client.ModKeybinds;
import com.soul.goe.items.custom.Wand;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SpellRadialScreen extends Screen {
    private final ItemStack wandStack;
    private final List<ItemStack> spells;
    private final List<Integer> originalIndices;
    private final int currentSpellDisplayIndex;
    private int hoveredSpellIndex = -1;

    private static final int RADIAL_RADIUS = 80;
    private static final int ICON_SIZE = 32;
    private static final int CENTER_RADIUS = 20;

    public SpellRadialScreen(ItemStack wandStack, List<ItemStack> spells, List<Integer> originalIndices, int currentSpellDisplayIndex) {
        super(Component.literal("Spell Selection"));
        this.wandStack = wandStack;
        this.spells = spells;
        this.originalIndices = originalIndices;
        this.currentSpellDisplayIndex = currentSpellDisplayIndex;
        this.hoveredSpellIndex = currentSpellDisplayIndex;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = width / 2;
        int centerY = height / 2;

        updateHoveredSpell(mouseX, mouseY, centerX, centerY);

        guiGraphics.fill(centerX - CENTER_RADIUS, centerY - CENTER_RADIUS, centerX + CENTER_RADIUS, centerY + CENTER_RADIUS, 0x80000000);

        for (int i = 0; i < spells.size(); i++) {
            double angle = (2 * Math.PI * i) / spells.size() - Math.PI / 2;
            int spellX = centerX + (int) (Math.cos(angle) * RADIAL_RADIUS) - ICON_SIZE / 2;
            int spellY = centerY + (int) (Math.sin(angle) * RADIAL_RADIUS) - ICON_SIZE / 2;

            boolean isHovered = hoveredSpellIndex == i;
            boolean isCurrent = currentSpellDisplayIndex == i;

            int backgroundColor = isHovered ? 0xFFFFFFFF : (isCurrent ? 0xFF4CAF50 : 0x80000000);
            guiGraphics.fill(spellX - 2, spellY - 2, spellX + ICON_SIZE + 2, spellY + ICON_SIZE + 2, backgroundColor);

            guiGraphics.renderItem(spells.get(i), spellX + 8, spellY + 8);

            if (isHovered) {
                Component spellName = spells.get(i).getHoverName();
                int textWidth = font.width(spellName);
                int textX = centerX - textWidth / 2;
                int textY = centerY + RADIAL_RADIUS + 30;

                guiGraphics.fill(textX - 4, textY - 2, textX + textWidth + 4, textY + font.lineHeight + 2, 0x80000000);
                guiGraphics.drawCenteredString(font, spellName, centerX, textY, 0xFFFFFF);
            }
        }

        String keyName = ModKeybinds.SPELL_RADIAL_MENU.getTranslatedKeyMessage().getString();
        Component instruction = Component.literal("Release " + keyName + " to select");
        guiGraphics.drawCenteredString(font, instruction, centerX, centerY - RADIAL_RADIUS - 35, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void updateHoveredSpell(int mouseX, int mouseY, int centerX, int centerY) {
        double mouseDistance = Math.sqrt(Math.pow(mouseX - centerX, 2) + Math.pow(mouseY - centerY, 2));

        if (mouseDistance < CENTER_RADIUS) {
            hoveredSpellIndex = -1;
            return;
        }

        double mouseAngle = Math.atan2(mouseY - centerY, mouseX - centerX);
        mouseAngle = (mouseAngle + Math.PI / 2 + 2 * Math.PI) % (2 * Math.PI);

        double anglePerSpell = (2 * Math.PI) / spells.size();
        int closestSpell = (int) Math.round(mouseAngle / anglePerSpell) % spells.size();

        double spellAngle = (2 * Math.PI * closestSpell) / spells.size();
        int spellX = centerX + (int) (Math.cos(spellAngle - Math.PI / 2) * RADIAL_RADIUS);
        int spellY = centerY + (int) (Math.sin(spellAngle - Math.PI / 2) * RADIAL_RADIUS);

        double distanceToSpell = Math.sqrt(Math.pow(mouseX - spellX, 2) + Math.pow(mouseY - spellY, 2));

        if (distanceToSpell <= ICON_SIZE) {
            hoveredSpellIndex = closestSpell;
        } else {
            hoveredSpellIndex = -1;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeybinds.SPELL_RADIAL_MENU.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (ModKeybinds.SPELL_RADIAL_MENU.matches(keyCode, scanCode)) {
            selectSpell();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    public void onShiftReleased() {
        selectSpell();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1) {
            selectSpell();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void selectSpell() {
        if (hoveredSpellIndex >= 0 && hoveredSpellIndex < spells.size()) {
            int originalWandIndex = originalIndices.get(hoveredSpellIndex);

            if (minecraft != null && minecraft.player != null) {
                System.out.println("Selecting display index: " + hoveredSpellIndex + " (original wand index: " + originalWandIndex + ")");
                System.out.println("Spell name: " + spells.get(hoveredSpellIndex).getHoverName().getString());

                sendSpellSelectionToServer(originalWandIndex);

                Component spellName = spells.get(hoveredSpellIndex).getHoverName();
                minecraft.player.displayClientMessage(Component.literal("Selected spell: ").append(spellName), true);
            }
        } else {
            System.out.println("No spell hovered (hoveredSpellIndex: " + hoveredSpellIndex + ")");
        }

        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    private void sendSpellSelectionToServer(int originalWandIndex) {
        if (minecraft != null && minecraft.player != null) {
            ItemStack mainHandItem = minecraft.player.getMainHandItem();
            if (mainHandItem.getItem() instanceof Wand wand) {
                wand.setCurrentSpell(mainHandItem, originalWandIndex);

                com.soul.goe.network.SpellSelectionPacket packet = new com.soul.goe.network.SpellSelectionPacket(originalWandIndex);

                net.neoforged.neoforge.network.PacketDistributor.sendToServer(packet);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, width, height, 0x80000000);
    }
}