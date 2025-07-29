package com.soul.goe.blocks.custom;

import com.mojang.serialization.MapCodec;
import com.soul.goe.Config;
import com.soul.goe.blocks.entity.PedestalEntity;
import com.soul.goe.blocks.entity.PedestalCraftingManager;
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

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 2, 16),        // Base platform
            Block.box(0, 14, 0, 16, 16, 16),      // Top platform
            Block.box(0, 2, 0, 2, 14, 2),         // Corner leg 1
            Block.box(0, 2, 14, 2, 14, 16),       // Corner leg 2
            Block.box(14, 2, 0, 16, 14, 2),       // Corner leg 3
            Block.box(14, 2, 14, 16, 14, 16),     // Corner leg 4
            Block.box(6, 2, 6, 10, 10, 10),       // Central pillar
            Block.box(4, 12, 4, 12, 14, 12),      // Upper funnel
            Block.box(5, 10, 5, 11, 12, 11)       // Lower funnel
    );

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
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        System.out.println("useItemOn called with stack: " + stack);

        if (level.getBlockEntity(pos) instanceof PedestalEntity pedestal) {
            if (isWand(stack)) {
                System.out.println("Wand detected - Shift key state: ");
                System.out.println("Pedestal crafting enabled: " + Config.ENABLE_PEDESTAL_CRAFTING.get());
                if (Config.ENABLE_PEDESTAL_CRAFTING.get()) {
                    System.out.println("Wand crafting attempted");
                    boolean success = PedestalCraftingManager.attemptCrafting(level, pos, pedestal);
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

    private boolean isWand(ItemStack stack) {
        // Check if the item is a Wand instance
        boolean isWand = stack.getItem() instanceof Wand;
        System.out.println("Checking if wand - item: " + stack.getItem().getClass().getSimpleName() + ", is wand: " + isWand);
        return isWand;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        System.out.println("useWithoutItem called - Client: " + level.isClientSide() + ", Shift: " + player.isShiftKeyDown());

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof PedestalEntity pedestal) {
            // Handle wand crafting (shift right-click with wand)
            ItemStack heldItem = player.getItemInHand(player.getUsedItemHand());
            System.out.println("Held item in useWithoutItem: " + heldItem);

            if (player.isShiftKeyDown() && isWand(heldItem)) {
                System.out.println("Wand crafting attempted via useWithoutItem");
                boolean success = PedestalCraftingManager.attemptCrafting(level, pos, pedestal);
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