package com.soul.goe.blocks.custom;


import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Flare extends Block {

    public Flare(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        Vec3 playerPos = Minecraft.getInstance().player.position();
        if (pos.distToCenterSqr(playerPos.x(), playerPos.y(), playerPos.z()) > 256) {
            return;
        }

        for (int i = 0; i < 5; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.5;
            double offsetY = (random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (random.nextDouble() - 0.5) * 0.5;

            double x = pos.getX() + 0.5 + offsetX;
            double y = pos.getY() + 0.5 + offsetY;
            double z = pos.getZ() + 0.5 + offsetZ;

            if (i % 2 == 0) {
                world.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
            } else {
                world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0, 0);
            }
        }
    }
}
