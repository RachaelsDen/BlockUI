package com.ldtteam.blockui.mod.item;

import com.ldtteam.common.util.BlockToItemHelper;
import net.minecraft.client.Minecraft;
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
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Holds blockstate rendering data for UIs.
 * The richer 26.2 preview/model-data integration is deferred until the dedicated preview batch.
 */
public record BlockStateRenderingData(BlockState blockState,
    @Nullable BlockEntity blockEntity,
    boolean modelNeedsRotationFix,
    Lazy<ItemStack> playerPickedItemStack)
{
    public static final BlockPos ILLEGAL_BLOCK_ENTITY_POS = BlockPos.ZERO.below(1000);

    private BlockStateRenderingData(final BlockState blockState,
        @Nullable final BlockEntity blockEntity,
        final boolean modelNeedsRotationFix)
    {
        this(blockState,
            blockEntity,
            modelNeedsRotationFix,
            Lazy.of(() -> BlockToItemHelper.getItemStack(blockState, blockEntity, Minecraft.getInstance().player)));
    }

    public static BlockStateRenderingData of(final Level level, final BlockPos pos, final Player player)
    {
        final BlockState blockState = level.getBlockState(pos);
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        final ItemStack itemStack = BlockToItemHelper.getItemStack(level, pos, player);

        return new BlockStateRenderingData(blockState,
            blockEntity,
            checkModelForYrotation(blockState),
            Lazy.of(() -> itemStack));
    }

    public static BlockStateRenderingData of(final BlockState blockState, @Nullable final BlockEntity blockEntity)
    {
        return new BlockStateRenderingData(blockState, blockEntity, checkModelForYrotation(blockState));
    }

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
        return new BlockStateRenderingData(blockState, null, false);
    }

    public BlockStateRenderingData updateBlockEntity(final Function<BlockEntity, BlockEntity> updater)
    {
        final BlockEntity updated = updater.apply(blockEntity);
        return new BlockStateRenderingData(blockState, updated, modelNeedsRotationFix);
    }

    public ItemStack itemStack()
    {
        return playerPickedItemStack.get();
    }

    public static boolean checkModelForYrotation(final BlockState blockState)
    {
        if (blockState.hasProperty(BlockStateProperties.AXIS))
        {
            return blockState.getValue(BlockStateProperties.AXIS) == Axis.Y;
        }

        if (blockState.hasProperty(BlockStateProperties.FACING))
        {
            final Direction facing = blockState.getValue(BlockStateProperties.FACING);
            return facing == Direction.UP || facing == Direction.DOWN;
        }

        return false;
    }
}
