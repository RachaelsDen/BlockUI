package com.ldtteam.blockui.mod.item;

import com.ldtteam.blockui.mod.Log;
import com.ldtteam.common.util.BlockToItemHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import java.util.function.Function;

/**
 * Holds blockstate rendering data for UIs. BlockState must match blockEntity
 */
public record BlockStateRenderingData(BlockState blockState,
    @Nullable BlockEntity blockEntity,
    boolean modelNeedsRotationFix,
    ItemStack playerPickedItemStack)
{
    public static final BlockPos ILLEGAL_BLOCK_ENTITY_POS = BlockPos.ZERO.below(1000);

    private BlockStateRenderingData(final BlockState blockState,
        final BlockEntity blockEntity,
        final boolean modelNeedsRotationFix)
    {
        this(blockState,
            blockEntity,
            modelNeedsRotationFix,
            createItemStack(blockState, blockEntity));
    }

    private BlockStateRenderingData(final BlockState blockState, final BlockEntity blockEntity)
    {
        this(blockState, blockEntity, checkModelForYrotation(blockState));
    }

    /**
     * @param blockEntity must match blockState
     */
    public static BlockStateRenderingData of(final BlockState blockState, @Nullable final BlockEntity blockEntity)
    {
        return blockEntity == null ? of(blockState) : new BlockStateRenderingData(blockState, blockEntity);
    }

    /**
     * If blockState should have blockEntity then a new fresh empty one will be created. Use {@link #of(BlockState, BlockEntity)} everywhere possible
     */
    public static BlockStateRenderingData of(final BlockState blockState)
    {
        if (blockState.hasBlockEntity() && blockState.getBlock() instanceof final EntityBlock entityBlock)
        {
            final BlockEntity be = entityBlock.newBlockEntity(ILLEGAL_BLOCK_ENTITY_POS, blockState);
            if (be != null)
            {
                return of(blockState, be);
            }
        }
        return new BlockStateRenderingData(blockState, null, checkModelForYrotation(blockState), createItemStack(blockState, null));
    }

    /**
     * Useful when you want to update blockEntity. Keeps modelData in sync
     */
    public BlockStateRenderingData updateBlockEntity(final Function<BlockEntity, BlockEntity> updater)
    {
        final BlockEntity updated = updater.apply(blockEntity);
        return new BlockStateRenderingData(blockState, updated, modelNeedsRotationFix, createItemStack(blockState, updated));
    }

    /**
     * @return best guess using player pick and similar methods
     */
    public ItemStack itemStack()
    {
        return playerPickedItemStack;
    }

    /**
     * @return captures blockstate in given level at given pos in current time (now)
     */
    public static BlockStateRenderingData of(final Level level, final BlockPos pos, final Player player)
    {
        final BlockState blockState = level.getBlockState(pos);
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        return new BlockStateRenderingData(blockState,
            blockEntity,
            checkModelForYrotation(blockState),
            BlockToItemHelper.getItemStack(level, pos, player));
    }

    private static ItemStack createItemStack(final BlockState blockState, @Nullable final BlockEntity blockEntity)
    {
        try
        {
            final Player player = Minecraft.getInstance().player;
            if (player != null)
            {
                return BlockToItemHelper.getItemStack(blockState, blockEntity, player);
            }
        }
        catch (final Exception e)
        {
            Log.getLogger().warn("Could not resolve item stack for block state: " + blockState, e);
        }

        final ItemStack itemStack = BlockToItemHelper.getItem(blockState).getDefaultInstance();
        if (!itemStack.isEmpty() && blockEntity != null && Minecraft.getInstance().level != null)
        {
            blockEntity.saveToItem(itemStack, Minecraft.getInstance().level.registryAccess());
        }
        return itemStack;
    }

    /**
     * @return true if model contains only Y axis rotations
     * TODO: move to tag
     */
    public static boolean checkModelForYrotation(final BlockState blockState)
    {
        final ModelResourceLocation modelResLoc = BlockModelShaper.stateToModelLocation(blockState);
        if (Minecraft.getInstance().getModelManager().getModel(modelResLoc) == null)
        {
            return false;
        }

        if (blockState.hasProperty(BlockStateProperties.AXIS))
        {
            return blockState.getValue(BlockStateProperties.AXIS) == Axis.Y;
        }

        if (blockState.hasProperty(BlockStateProperties.FACING))
        {
            final Direction facing = blockState.getValue(BlockStateProperties.FACING);
            return facing == Direction.UP || facing == Direction.DOWN;
        }

        return true;
    }
}
