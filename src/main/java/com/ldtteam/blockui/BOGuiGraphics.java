package com.ldtteam.blockui;

import com.ldtteam.blockui.mod.item.BlockStateRenderingData;
import com.ldtteam.blockui.util.cursor.Cursor;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

public class BOGuiGraphics
{
    private static final ThreadLocal<BOGuiGraphics> ACTIVE = new ThreadLocal<>();

    private final Minecraft minecraft;
    private final GuiGraphicsExtractor extractor;
    private final PoseStack pose;
    private final CursorSelector cursorSelector = new CursorSelector();

    public BOGuiGraphics(final Minecraft minecraft, final GuiGraphicsExtractor extractor, final PoseStack pose)
    {
        this.minecraft = minecraft;
        this.extractor = extractor;
        this.pose = pose;
    }

    public PoseStack pose()
    {
        return pose;
    }

    GuiGraphicsExtractor extractor()
    {
        return extractor;
    }

    static void setActive(@Nullable final BOGuiGraphics graphics)
    {
        if (graphics == null)
        {
            ACTIVE.remove();
        }
        else
        {
            ACTIVE.set(graphics);
        }
    }

    @Nullable
    static GuiGraphicsExtractor activeExtractor()
    {
        final BOGuiGraphics graphics = ACTIVE.get();
        return graphics == null ? null : graphics.extractor();
    }

    private Font getFont(@Nullable final ItemStack itemStack)
    {
        if (itemStack != null)
        {
            final Font font = IClientItemExtensions.of(itemStack).getFont(itemStack, IClientItemExtensions.FontContext.ITEM_COUNT);
            if (font != null)
            {
                return font;
            }
        }
        return minecraft.font;
    }

    public void renderItem(final ItemStack itemStack, final float x, final float y)
    {
        final ScreenPoint point = transformPoint(x, y);
        extractor.item(itemStack, point.x(), point.y());
    }

    public void renderItemDecorations(final ItemStack itemStack, final float x, final float y)
    {
        final ScreenPoint point = transformPoint(x, y);
        extractor.itemDecorations(getFont(itemStack), itemStack, point.x(), point.y());
    }

    public void renderItemDecorations(final ItemStack itemStack, final float x, final float y, @Nullable final String altStackSize)
    {
        final ScreenPoint point = transformPoint(x, y);
        extractor.itemDecorations(getFont(itemStack), itemStack, point.x(), point.y(), altStackSize);
    }

    public int drawString(final String text, final float x, final float y, final int color)
    {
        return drawString(text, x, y, color, false);
    }

    public int drawString(final String text, final float x, final float y, final int color, final boolean shadow)
    {
        final ScreenPoint point = transformPoint(x, y);
        extractor.text(minecraft.font, text, point.x(), point.y(), normalizeTextColor(color), shadow);
        return point.x() + minecraft.font.width(text);
    }

    public int drawString(final FormattedCharSequence text, final float x, final float y, final int color, final boolean shadow)
    {
        final ScreenPoint point = transformPoint(x, y);
        extractor.text(minecraft.font, text, point.x(), point.y(), normalizeTextColor(color), shadow);
        return point.x() + minecraft.font.width(text);
    }

    private static int normalizeTextColor(final int color)
    {
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    private ScreenPoint transformPoint(final float x, final float y)
    {
        final Vector3f transformed = pose().last().pose().transformPosition(new Vector3f(x, y, 0));
        return new ScreenPoint(Math.round(transformed.x), Math.round(transformed.y));
    }

    public void setCursor(final Cursor cursor)
    {
        cursorSelector.consider(0, cursor);
    }

    public void applyCursor(final int debugXoffset)
    {
        final Cursor selectedCursor = cursorSelector.selectedCursor();
        final CursorType mapped = mapCursor(selectedCursor);
        if (mapped != null)
        {
            extractor.requestCursor(mapped);
        }
        else
        {
            selectedCursor.apply();
        }

        if (Pane.debugging)
        {
            drawString(selectedCursor.toString(), debugXoffset, -minecraft.font.lineHeight, Color.getByName("white"));
        }
    }

    private static CursorType mapCursor(final Cursor cursor)
    {
        if (cursor == Cursor.DEFAULT || cursor == Cursor.ARROW)
        {
            return CursorType.DEFAULT;
        }
        if (cursor == Cursor.TEXT_CURSOR)
        {
            return CursorTypes.IBEAM;
        }
        if (cursor == Cursor.CROSSHAIR)
        {
            return CursorTypes.CROSSHAIR;
        }
        if (cursor == Cursor.HAND)
        {
            return CursorTypes.POINTING_HAND;
        }
        if (cursor == Cursor.HORIZONTAL_RESIZE)
        {
            return CursorTypes.RESIZE_EW;
        }
        if (cursor == Cursor.VERTICAL_RESIZE)
        {
            return CursorTypes.RESIZE_NS;
        }
        if (cursor == Cursor.RESIZE)
        {
            return CursorTypes.RESIZE_ALL;
        }
        return null;
    }

    public void renderBlockStateAsItem(final BlockStateRenderingData data, final ItemStack itemStack)
    {
        final ItemStack renderStack;
        if (itemStack.isEmpty())
        {
            renderStack = itemStack;
        }
        else
        {
            renderStack = itemStack.copy();
            BlockItemStateProperties props = BlockItemStateProperties.EMPTY;
            for (final var property : data.blockState().getProperties())
            {
                props = props.with(property, data.blockState());
            }
            if (!props.isEmpty())
            {
                renderStack.set(DataComponents.BLOCK_STATE, props);
            }
        }
        final ScreenPoint point = transformPoint(0, 0);
        extractor.item(renderStack, point.x(), point.y());
    }

    public static double getAltSpeedFactor()
    {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LALT) ? 5 : 1;
    }

    private record ScreenPoint(int x, int y)
    {
    }
}
