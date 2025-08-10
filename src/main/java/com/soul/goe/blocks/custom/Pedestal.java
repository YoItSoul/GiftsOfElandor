package com.soul.goe.blocks.custom;

import com.mojang.serialization.MapCodec;
import com.soul.goe.Config;
import com.soul.goe.blocks.entity.PedestalCraftingManager;
import com.soul.goe.blocks.entity.PedestalEntity;
import com.soul.goe.items.custom.Wand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Pedestal extends BaseEntityBlock {
    public static final MapCodec<Pedestal> CODEC = simpleCodec(Pedestal::new);

    private static final VoxelShape SHAPE = Shapes.or(Block.box(4, 0, 4, 12, 1, 12), Block.box(6, 1, 6, 10, 9, 10), Block.box(5, 9, 5, 11, 11, 11), Block.box(4, 11, 4, 12, 13, 12), Block.box(5, 1, 9, 6, 2, 10), Block.box(10, 1, 6, 11, 2, 7), Block.box(10, 1, 9, 11, 2, 10), Block.box(5, 1, 6, 6, 2, 7), Block.box(6, 1, 5, 7, 2, 6), Block.box(9, 1, 5, 10, 2, 6), Block.box(6, 1, 10, 7, 2, 11), Block.box(9, 1, 10, 10, 2, 11));

    public Pedestal(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        System.out.println("useItemOn called with stack: " + stack);

        if (level.getBlockEntity(pos) instanceof PedestalEntity pedestal) {
            if (isWand(stack)) {
                System.out.println("Wand detected - Shift key state: ");
                System.out.println("Pedestal crafting enabled: " + Config.ENABLE_PEDESTAL_CRAFTING.get());
                if (Config.ENABLE_PEDESTAL_CRAFTING.get()) {
                    System.out.println("Wand crafting attempted");
                    // Pass player and wand stack to crafting manager
                    boolean success = PedestalCraftingManager.attemptCrafting(level, pos, pedestal, player, stack);
                    System.out.println("Crafting result: " + success);
                    return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
                }
            }

            // Handle empty hand (taking item)
            if (stack.isEmpty()) {
                System.out.println("Empty hand detected, pedestal isEmpty: " + pedestal.isEmpty());
                if (!pedestal.isEmpty()) {
                    ItemStack displayedItem = pedestal.takeItem();
                    System.out.println("Taking item: " + displayedItem);

                    if (!player.getInventory().add(displayedItem)) {
                        player.drop(displayedItem, false);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            // Handle item in hand (placing/swapping)
            else if (!isWand(stack)) {
                System.out.println("Item in hand, placing/swapping");
                ItemStack singleItem = stack.copy();
                singleItem.setCount(1);

                ItemStack currentItem = pedestal.getDisplayedItem();
                pedestal.setDisplayedItem(singleItem);

                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                if (!currentItem.isEmpty()) {
                    if (!player.getInventory().add(currentItem)) {
                        player.drop(currentItem, false);
                    }
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        System.out.println("useWithoutItem called - Client: " + level.isClientSide() + ", Shift: " + player.isShiftKeyDown());

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof PedestalEntity pedestal) {
            // Handle wand crafting (shift right-click with wand)
            ItemStack heldItem = player.getItemInHand(player.getUsedItemHand());
            System.out.println("Held item in useWithoutItem: " + heldItem);

            if (player.isShiftKeyDown() && isWand(heldItem)) {
                System.out.println("Wand crafting attempted via useWithoutItem");
                // Pass player and wand stack to crafting manager
                boolean success = PedestalCraftingManager.attemptCrafting(level, pos, pedestal, player, heldItem);
                return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
            }

            // Handle taking item with empty hand (non-shift)
            if (!player.isShiftKeyDown() && heldItem.isEmpty()) {
                System.out.println("Taking item with empty hand");
                if (!pedestal.isEmpty()) {
                    ItemStack displayedItem = pedestal.takeItem();
                    if (!player.getInventory().add(displayedItem)) {
                        player.drop(displayedItem, false);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }


    private boolean isWand(ItemStack stack) {
        // Check if the item is a Wand instance
        boolean isWand = stack.getItem() instanceof Wand;
        System.out.println("Checking if wand - item: " + stack.getItem().getClass().getSimpleName() + ", is wand: " + isWand);
        return isWand;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof PedestalEntity pedestal) {
                if (!pedestal.isEmpty()) {
                    popResource(level, pos, pedestal.getDisplayedItem());
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}