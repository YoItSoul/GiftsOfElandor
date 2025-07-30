package com.soul.goe.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

public class EmptyEntityRenderer<T extends Entity, S extends EntityRenderState> extends EntityRenderer<T, S> {
    public EmptyEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public S createRenderState() {
        return (S) new EntityRenderState();
    }

    @Override
    public void extractRenderState(T entity, S renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
    }

    @Override
    public void render(S renderState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Render nothing - entity is invisible
    }
}