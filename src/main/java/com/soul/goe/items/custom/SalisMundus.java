package com.soul.goe.items.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SalisMundus extends FoilItem {

    // eventually change these to config variables please
    private static final int SEARCH_RADIUS = 5;
    private static final double CENTER_Y_OFFSET = 1.0;

    // client-only config please
    private static final int PARTICLE_POINTS = 10;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;

    public SalisMundus(Properties pProperties) {
        super(pProperties);
    }


        @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos blockPos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();

        if (!level.isClientSide() && player != null) {
            // Add your interaction logic here
            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.SUCCESS;
    }
}
