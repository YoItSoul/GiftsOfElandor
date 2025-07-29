package com.soul.goe.blocks.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soul.goe.blocks.entity.PedestalEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PedestalRenderer implements BlockEntityRenderer<PedestalEntity> {

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PedestalEntity pedestal, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        ItemStack displayedItem = pedestal.getDisplayedItem();
        if (displayedItem.isEmpty()) {
            return;
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        poseStack.pushPose();

        // Position the item above the pedestal (center of top platform)
        poseStack.translate(0.5, 1.2, 0.5);

        // Add floating animation (gentle bobbing up and down)
        long time = pedestal.getLevel().getGameTime();
        float bob = (float) Math.sin((time + partialTick) * 0.1) * 0.1f;
        poseStack.translate(0, bob, 0);

        // Rotate the item continuously
        float rotation = (time + partialTick) * 2.0f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Scale the item slightly larger for better visibility
        poseStack.scale(0.75f, 0.75f, 0.75f);

        // Render the item
        itemRenderer.renderStatic(displayedItem, ItemDisplayContext.GROUND,
                packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, pedestal.getLevel(), 0);

        poseStack.popPose();
    }
}