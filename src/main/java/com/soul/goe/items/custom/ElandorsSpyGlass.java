package com.soul.goe.items.custom;

import com.soul.goe.client.AspectDiscoveryManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ElandorsSpyGlass extends Item {
    public ElandorsSpyGlass(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (level.isClientSide) {
            HitResult hitResult = player.pick(5.0D, 0.0F, false); // 5 blocks range

            if (hitResult instanceof EntityHitResult entityHit) {
                if (entityHit.getEntity() instanceof Player targetPlayer) {
                    ItemStack targetStack = targetPlayer.getMainHandItem();
                    if (!targetStack.isEmpty()) {
                        AspectDiscoveryManager.discoverItemAspects(player, targetStack.getItem());
                    }
                }
            }
        }

        player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);

        return InteractionResult.SUCCESS;
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

}
