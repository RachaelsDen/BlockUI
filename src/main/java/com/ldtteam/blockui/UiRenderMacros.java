package com.ldtteam.blockui;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.NineSlice;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.Tile;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.Type;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Our replacement for GuiComponent.
 */
public class UiRenderMacros
{
    public static final double HALF_BIAS = 0.5;

    static int alphaFromArgb(final int argbColor)
    {
        return (argbColor >> 24) & 0xff;
    }

    static int redFromArgb(final int argbColor)
    {
        return (argbColor >> 16) & 0xff;
    }

    static int greenFromArgb(final int argbColor)
    {
        return (argbColor >> 8) & 0xff;
    }

    static int blueFromArgb(final int argbColor)
    {
        return argbColor & 0xff;
    }

    static int argb(final int red, final int green, final int blue, final int alpha)
    {
        return (alpha & 0xff) << 24 | (red & 0xff) << 16 | (green & 0xff) << 8 | (blue & 0xff);
    }

    private static UnsupportedOperationException unsupported26_2(final String method)
    {
        return new UnsupportedOperationException(method + " still uses the removed pre-26.2 immediate-mode render pipeline");
    }

    private static GuiGraphicsExtractor requireActiveExtractor(final String method)
    {
        final GuiGraphicsExtractor extractor = BOGuiGraphics.activeExtractor();
        if (extractor == null)
        {
            throw new IllegalStateException(method + " requires an active BOGuiGraphics extractor context");
        }
        return extractor;
    }

    private static RectI transformRect(final PoseStack ps, final int x, final int y, final int w, final int h)
    {
        final Matrix4f matrix = ps.last().pose();
        final Vector3f topLeft = matrix.transformPosition(new Vector3f(x, y, 0));
        final Vector3f bottomRight = matrix.transformPosition(new Vector3f(x + w, y + h, 0));
        final int x0 = Math.round(Math.min(topLeft.x, bottomRight.x));
        final int y0 = Math.round(Math.min(topLeft.y, bottomRight.y));
        final int x1 = Math.round(Math.max(topLeft.x, bottomRight.x));
        final int y1 = Math.round(Math.max(topLeft.y, bottomRight.y));
        return new RectI(x0, y0, x1, y1);
    }

    public static void drawLineRectGradient(final PoseStack ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColorStart,
        final int argbColorEnd)
    {
        drawLineRectGradient(ps, x, y, w, h, argbColorStart, argbColorEnd, 1);
    }

    public static void drawLineRectGradient(final PoseStack ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColorStart,
        final int argbColorEnd,
        final int lineWidth)
    {
        drawLineRectGradient(ps,
            x,
            y,
            w,
            h,
            redFromArgb(argbColorStart),
            redFromArgb(argbColorEnd),
            greenFromArgb(argbColorStart),
            greenFromArgb(argbColorEnd),
            blueFromArgb(argbColorStart),
            blueFromArgb(argbColorEnd),
            alphaFromArgb(argbColorStart),
            alphaFromArgb(argbColorEnd),
            lineWidth);
    }

    public static void drawLineRectGradient(final PoseStack ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int redStart,
        final int redEnd,
        final int greenStart,
        final int greenEnd,
        final int blueStart,
        final int blueEnd,
        final int alphaStart,
        final int alphaEnd,
        final int lineWidth)
    {
        if (lineWidth < 1 || (alphaStart == 0 && alphaEnd == 0))
        {
            return;
        }
        final GuiGraphicsExtractor extractor = requireActiveExtractor("drawLineRectGradient");
        final RectI top = transformRect(ps, x, y, w, lineWidth);
        final RectI bottom = transformRect(ps, x, y + h - lineWidth, w, lineWidth);
        final RectI left = transformRect(ps, x, y + lineWidth, lineWidth, Math.max(0, h - 2 * lineWidth));
        final RectI right = transformRect(ps, x + w - lineWidth, y + lineWidth, lineWidth, Math.max(0, h - 2 * lineWidth));

        extractor.fill(top.x0, top.y0, top.x1, top.y1, argb(redStart, greenStart, blueStart, alphaStart));
        extractor.fill(bottom.x0, bottom.y0, bottom.x1, bottom.y1, argb(redEnd, greenEnd, blueEnd, alphaEnd));
        if (left.x0 != left.x1 && left.y0 != left.y1)
        {
            extractor.fillGradient(left.x0, left.y0, left.x1, left.y1, argb(redStart, greenStart, blueStart, alphaStart), argb(redEnd, greenEnd, blueEnd, alphaEnd));
        }
        if (right.x0 != right.x1 && right.y0 != right.y1)
        {
            extractor.fillGradient(right.x0, right.y0, right.x1, right.y1, argb(redStart, greenStart, blueStart, alphaStart), argb(redEnd, greenEnd, blueEnd, alphaEnd));
        }
    }

    public static void drawLineRect(final PoseStack ps, final int x, final int y, final int w, final int h, final int argbColor)
    {
        drawLineRect(ps, x, y, w, h, argbColor, 1);
    }

    public static void drawLineRect(final PoseStack ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColor,
        final int lineWidth)
    {
        drawLineRect(ps,
            x,
            y,
            w,
            h,
            redFromArgb(argbColor),
            greenFromArgb(argbColor),
            blueFromArgb(argbColor),
            alphaFromArgb(argbColor),
            lineWidth);
    }

    public static void drawLineRect(final PoseStack ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final int lineWidth)
    {
        if (lineWidth < 1 || alpha == 0)
        {
            return;
        }
        final GuiGraphicsExtractor extractor = requireActiveExtractor("drawLineRect");
        final int color = argb(red, green, blue, alpha);
        final RectI top = transformRect(ps, x, y, w, lineWidth);
        final RectI bottom = transformRect(ps, x, y + h - lineWidth, w, lineWidth);
        final RectI left = transformRect(ps, x, y + lineWidth, lineWidth, Math.max(0, h - 2 * lineWidth));
        final RectI right = transformRect(ps, x + w - lineWidth, y + lineWidth, lineWidth, Math.max(0, h - 2 * lineWidth));

        extractor.fill(top.x0, top.y0, top.x1, top.y1, color);
        extractor.fill(bottom.x0, bottom.y0, bottom.x1, bottom.y1, color);
        if (left.x0 != left.x1 && left.y0 != left.y1)
        {
            extractor.fill(left.x0, left.y0, left.x1, left.y1, color);
        }
        if (right.x0 != right.x1 && right.y0 != right.y1)
        {
            extractor.fill(right.x0, right.y0, right.x1, right.y1, color);
        }
    }

    public static void fill(final PoseStack ps, final int x, final int y, final int w, final int h, final int argbColor)
    {
        fill(ps, x, y, w, h, redFromArgb(argbColor), greenFromArgb(argbColor), blueFromArgb(argbColor), alphaFromArgb(argbColor));
    }

    public static void fill(final PoseStack ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        if (alpha == 0)
        {
            return;
        }
        final GuiGraphicsExtractor extractor = requireActiveExtractor("fill");
        final RectI rect = transformRect(ps, x, y, w, h);
        extractor.fill(rect.x0, rect.y0, rect.x1, rect.y1, argb(red, green, blue, alpha));
    }

    public static void fillGradient(final PoseStack ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColorStart,
        final int argbColorEnd)
    {
        fillGradient(ps,
            x,
            y,
            w,
            h,
            redFromArgb(argbColorStart),
            redFromArgb(argbColorEnd),
            greenFromArgb(argbColorStart),
            greenFromArgb(argbColorEnd),
            blueFromArgb(argbColorStart),
            blueFromArgb(argbColorEnd),
            alphaFromArgb(argbColorStart),
            alphaFromArgb(argbColorEnd));
    }

    public static void fillGradient(final PoseStack ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int redStart,
        final int redEnd,
        final int greenStart,
        final int greenEnd,
        final int blueStart,
        final int blueEnd,
        final int alphaStart,
        final int alphaEnd)
    {
        if (alphaStart == 0 && alphaEnd == 0)
        {
            return;
        }
        final GuiGraphicsExtractor extractor = requireActiveExtractor("fillGradient");
        final RectI rect = transformRect(ps, x, y, w, h);
        extractor.fillGradient(rect.x0, rect.y0, rect.x1, rect.y1, argb(redStart, greenStart, blueStart, alphaStart), argb(redEnd, greenEnd, blueEnd, alphaEnd));
    }

    public static void hLine(final PoseStack ps, final int x, final int xEnd, final int y, final int argbColor)
    {
        line(ps, x, y, xEnd, y, redFromArgb(argbColor), greenFromArgb(argbColor), blueFromArgb(argbColor), alphaFromArgb(argbColor));
    }

    public static void hLine(final PoseStack ps,
        final int x,
        final int xEnd,
        final int y,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        line(ps, x, y, xEnd, y, red, green, blue, alpha);
    }

    public static void vLine(final PoseStack ps, final int x, final int y, final int yEnd, final int argbColor)
    {
        line(ps, x, y, x, yEnd, redFromArgb(argbColor), greenFromArgb(argbColor), blueFromArgb(argbColor), alphaFromArgb(argbColor));
    }

    public static void vLine(final PoseStack ps,
        final int x,
        final int y,
        final int yEnd,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        line(ps, x, y, x, yEnd, red, green, blue, alpha);
    }

    public static void line(final PoseStack ps, final int x, final int y, final int xEnd, final int yEnd, final int argbColor)
    {
        line(ps, x, y, xEnd, yEnd, redFromArgb(argbColor), greenFromArgb(argbColor), blueFromArgb(argbColor), alphaFromArgb(argbColor));
    }

    public static void line(final PoseStack ps,
        final int x,
        final int y,
        final int xEnd,
        final int yEnd,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        if (alpha == 0)
        {
            return;
        }
        final GuiGraphicsExtractor extractor = requireActiveExtractor("line");
        final int color = argb(red, green, blue, alpha);
        if (x == xEnd)
        {
            final int startY = Math.min(y, yEnd);
            final int height = Math.max(1, Math.abs(yEnd - y));
            final RectI rect = transformRect(ps, x, startY, 1, height);
            extractor.fill(rect.x0, rect.y0, rect.x1, rect.y1, color);
            return;
        }
        if (y == yEnd)
        {
            final int startX = Math.min(x, xEnd);
            final int width = Math.max(1, Math.abs(xEnd - x));
            final RectI rect = transformRect(ps, startX, y, width, 1);
            extractor.fill(rect.x0, rect.y0, rect.x1, rect.y1, color);
            return;
        }
        throw unsupported26_2("line (non-axis-aligned)");
    }

    public static void blit(final PoseStack ps,
        final Identifier rl,
        final int x,
        final int y,
        final int w,
        final int h,
        final int u,
        final int v,
        final int mapW,
        final int mapH)
    {
        blit(ps, rl, x, y, w, h, (float) u / mapW, (float) v / mapH, (float) (u + w) / mapW, (float) (v + h) / mapH);
    }

    public static void blit(final PoseStack ps,
        final Identifier rl,
        final int x,
        final int y,
        final int w,
        final int h,
        final int u,
        final int v,
        final int uW,
        final int vH,
        final int mapW,
        final int mapH)
    {
        blit(ps, rl, x, y, w, h, (float) u / mapW, (float) v / mapH, (float) (u + uW) / mapW, (float) (v + vH) / mapH);
    }

    public static void blitSprite(final PoseStack ps,
        final TextureAtlasSprite sprite,
        final GuiSpriteScaling guiScaling,
        final int x,
        final int y,
        final int w,
        final int h)
    {
        final Identifier atlasLocation = sprite.atlasLocation();
        final float u0 = sprite.getU0();
        final float v0 = sprite.getV0();
        final float u1 = sprite.getU1();
        final float v1 = sprite.getV1();
        if (guiScaling.type() == Type.STRETCH)
        {
            blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
        }
        else if (guiScaling instanceof final NineSlice nineSlice)
        {
            final int rbW = nineSlice.width();
            final int rbH = nineSlice.height();

            if (rbW == w && rbH == h)
            {
                blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
            }
            else
            {
                final int uR = nineSlice.border().left();
                final int vR = nineSlice.border().top();
                final int rW = rbW - uR - nineSlice.border().right();
                final int rH = rbH - vR - nineSlice.border().bottom();
                blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, uR, vR, rW, rH, rbW, rbH);
            }
        }
        else if (guiScaling instanceof final Tile tile)
        {
            final int tW = tile.width();
            final int tH = tile.height();

            if (tW == w && tH == h)
            {
                blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
            }
            else
            {
                blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, 0, 0, tW, tH, tW, tH);
            }
        }
    }

    public static void blitSprite(final PoseStack ps,
        final TextureAtlasSprite sprite,
        final int x,
        final int y,
        final int w,
        final int h)
    {
        blit(ps, sprite.atlasLocation(), x, y, w, h, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }

    public static void blit(final PoseStack ps, final Identifier rl, final int x, final int y, final int w, final int h)
    {
        blit(ps, rl, x, y, w, h, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    public static void blit(final PoseStack ps,
        final Identifier rl,
        final int x,
        final int y,
        final int w,
        final int h,
        final float uMin,
        final float vMin,
        final float uMax,
        final float vMax)
    {
        final GuiGraphicsExtractor extractor = requireActiveExtractor("blit");
        final RectI rect = transformRect(ps, x, y, w, h);
        extractor.blit(rl, rect.x0, rect.y0, rect.x1, rect.y1, uMin, uMax, vMin, vMax);
    }

    /**
     * Draws texture without scaling so one texel is one pixel, using repeatable texture center. TODO: Nightenom - rework to better
     * algoritm from pgr, also texture extensions?
     *
     * @param ps              MatrixStack
     * @param rl              image ResLoc
     * @param x               start target coords [pixels]
     * @param y               start target coords [pixels]
     * @param width           target rendering box [pixels]
     * @param height          target rendering box [pixels]
     * @param uMin            texture start offset [normalized texels]
     * @param vMin            texture start offset [normalized texels]
     * @param uMax            texture end offset [normalized texels]
     * @param vMax            texture end offset [normalized texels]
     * @param uRepeat         offset relative to u, v [texels], smaller than uWidth
     * @param vRepeat         offset relative to u, v [texels], smaller than vHeight
     * @param repeatWidth     size of repeatable part in texture [texels], smaller than or equal repeatBoxWidth - uRepeat
     * @param repeatHeight    size of repeatable part in texture [texels], smaller than or equal repeatBoxHeight - vRepeat
     * @param repeatBoxWidth  size of entire repeatable box (borders + repeat part) [texels]
     * @param repeatBoxHeight size of entire repeatable box (borders + repeat part) [texels]
     */
    protected static void blitRepeatable(final PoseStack ps,
        final Identifier rl,
        final int x,
        final int y,
        final int width,
        final int height,
        final float uMin,
        final float vMin,
        final float uMax,
        final float vMax,
        final int uRepeat,
        final int vRepeat,
        final int repeatWidth,
        final int repeatHeight,
        final int repeatBoxWidth,
        final int repeatBoxHeight)
    {
        if (uRepeat < 0 || vRepeat < 0 ||
            uRepeat >= repeatBoxWidth ||
            vRepeat >= repeatBoxHeight ||
            repeatWidth < 1 ||
            repeatHeight < 1 ||
            repeatWidth > repeatBoxWidth - uRepeat ||
            repeatHeight > repeatBoxHeight - vRepeat)
        {
            throw new IllegalArgumentException("Repeatable box is outside of texture box");
        }

        final int repeatCountX = Math.max(1, Math.max(0, width - (repeatBoxWidth - repeatWidth)) / repeatWidth);
        final int repeatCountY = Math.max(1, Math.max(0, height - (repeatBoxHeight - repeatHeight)) / repeatHeight);
        final float uTexelWidth = (uMax - uMin) / repeatBoxWidth;
        final float vTexelHeight = (vMax - vMin) / repeatBoxHeight;

        final GuiGraphicsExtractor extractor = requireActiveExtractor("blitRepeatable");

        // main
        for (int i = 0; i < repeatCountX; i++)
        {
            final int uAdjust = i == 0 ? 0 : uRepeat;
            final int xStart = x + uAdjust + i * repeatWidth;
            final int w = Math.min(repeatWidth + uRepeat - uAdjust, width - (repeatBoxWidth - uRepeat - repeatWidth));
            final float minU = uMin + uTexelWidth * uAdjust;
            final float maxU = minU + uTexelWidth * w;

            for (int j = 0; j < repeatCountY; j++)
            {
                final int vAdjust = j == 0 ? 0 : vRepeat;
                final int yStart = y + vAdjust + j * repeatHeight;
                final int h = Math.min(repeatHeight + vRepeat - vAdjust, height - (repeatBoxHeight - vRepeat - repeatHeight));
                final float minV = vMin + vTexelHeight * vAdjust;
                final float maxV = minV + vTexelHeight * h;

                final RectI rect = transformRect(ps, xStart, yStart, w, h);
                extractor.blit(rl, rect.x0, rect.y0, rect.x1, rect.y1, minU, maxU, minV, maxV);
            }
        }

        final int xEnd = x + Math.min(uRepeat + repeatCountX * repeatWidth, width - (repeatBoxWidth - uRepeat - repeatWidth));
        final int yEnd = y + Math.min(vRepeat + repeatCountY * repeatHeight, height - (repeatBoxHeight - vRepeat - repeatHeight));
        final int uLeft = width - (xEnd - x);
        final int vBot = height - (yEnd - y);
        final float restMinU = uMax - uLeft * uTexelWidth;
        final float restMinV = vMax - vBot * vTexelHeight;

        // bot border
        for (int i = 0; i < repeatCountX; i++)
        {
            final int uAdjust = i == 0 ? 0 : uRepeat;
            final int xStart = x + uAdjust + i * repeatWidth;
            final int w = Math.min(repeatWidth + uRepeat - uAdjust, width - uLeft);
            final float minU = uMin + uTexelWidth * uAdjust;
            final float maxU = minU + uTexelWidth * w;

            final RectI rect = transformRect(ps, xStart, yEnd, w, vBot);
            extractor.blit(rl, rect.x0, rect.y0, rect.x1, rect.y1, minU, maxU, restMinV, vMax);
        }

        // left border
        for (int j = 0; j < repeatCountY; j++)
        {
            final int vAdjust = j == 0 ? 0 : vRepeat;
            final int yStart = y + vAdjust + j * repeatHeight;
            final int h = Math.min(repeatHeight + vRepeat - vAdjust, height - vBot);
            final float minV = vMin + vTexelHeight * vAdjust;
            final float maxV = minV + vTexelHeight * h;

            final RectI rect = transformRect(ps, xEnd, yStart, uLeft, h);
            extractor.blit(rl, rect.x0, rect.y0, rect.x1, rect.y1, restMinU, uMax, minV, maxV);
        }

        // bot left corner
        final RectI rect = transformRect(ps, xEnd, yEnd, uLeft, vBot);
        extractor.blit(rl, rect.x0, rect.y0, rect.x1, rect.y1, restMinU, uMax, restMinV, vMax);
    }

    public static void populateFillTriangles(final Matrix4f m,
        final VertexConsumer buffer,
        final int x,
        final int y,
        final int w,
        final int h,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        buffer.addVertex(m, x, y, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x, y + h, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x, y + h, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y + h, 0).setColor(red, green, blue, alpha);
    }

    public static void populateFillGradientTriangles(final Matrix4f m,
        final VertexConsumer buffer,
        final int x,
        final int y,
        final int w,
        final int h,
        final int redStart,
        final int redEnd,
        final int greenStart,
        final int greenEnd,
        final int blueStart,
        final int blueEnd,
        final int alphaStart,
        final int alphaEnd)
    {
        buffer.addVertex(m, x, y, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertex(m, x, y + h, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertex(m, x + w, y, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertex(m, x + w, y, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertex(m, x, y + h, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertex(m, x + w, y + h, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
    }

    public static void populateBlitTriangles(final VertexConsumer buffer,
        final Matrix4f mat,
        final float xStart,
        final float xEnd,
        final float yStart,
        final float yEnd,
        final float uMin,
        final float uMax,
        final float vMin,
        final float vMax)
    {
        buffer.addVertex(mat, xStart, yStart, 0).setUv(uMin, vMin);
        buffer.addVertex(mat, xStart, yEnd, 0).setUv(uMin, vMax);
        buffer.addVertex(mat, xEnd, yStart, 0).setUv(uMax, vMin);
        buffer.addVertex(mat, xEnd, yStart, 0).setUv(uMax, vMin);
        buffer.addVertex(mat, xStart, yEnd, 0).setUv(uMin, vMax);
        buffer.addVertex(mat, xEnd, yEnd, 0).setUv(uMax, vMax);
    }

    /**
     * Render an entity on a GUI.
     * 
     * @param poseStack matrix
     * @param x         horizontal center position
     * @param y         vertical bottom position
     * @param scale     scaling factor
     * @param headYaw   adjusts look rotation
     * @param yaw       adjusts body rotation
     * @param pitch     adjusts look rotation
     * @param entity    the entity to render
     */
    public static void drawEntity(final PoseStack poseStack,
        final int x,
        final int y,
        final double scale,
        final float headYaw,
        final float yaw,
        final float pitch,
        final Entity entity)
    {
        throw unsupported26_2("drawEntity");
    }

    /**
     * @return rendering lambda detached from sprite and guiScaling instances
     * @implNote same as logic {@link #blitSprite(PoseStack, TextureAtlasSprite, GuiSpriteScaling, int, int, int, int)}
     */
    public static ResolvedBlit resolveSprite(final TextureAtlasSprite sprite, final GuiSpriteScaling guiScaling)
    {
        final Identifier atlasLocation = sprite.atlasLocation();
        final float u0 = sprite.getU0();
        final float v0 = sprite.getV0();
        final float u1 = sprite.getU1();
        final float v1 = sprite.getV1();
        if (guiScaling.type() == Type.STRETCH)
        {
            return (ps, x, y, w, h) -> blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
        }
        else if (guiScaling instanceof final NineSlice nineSlice)
        {
            final int rbW = nineSlice.width();
            final int rbH = nineSlice.height();
            final int uR = nineSlice.border().left();
            final int vR = nineSlice.border().top();
            final int rW = rbW - uR - nineSlice.border().right();
            final int rH = rbH - vR - nineSlice.border().bottom();

            return (ps, x, y, w, h) -> {
                if (rbW == w && rbH == h)
                {
                    blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
                }
                else
                {
                    blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, uR, vR, rW, rH, rbW, rbH);
                }
            };
        }
        else if (guiScaling instanceof final Tile tile)
        {
            final int tW = tile.width();
            final int tH = tile.height();

            return (ps, x, y, w, h) -> {
                if (tW == w && tH == h)
                {
                    blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
                }
                else
                {
                    blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, 0, 0, tW, tH, tW, tH);
                }
            };
        }
        return ResolvedBlit.EMPTY;
    }

    /**
     * Used for precompiling math around rendering
     */
    @FunctionalInterface
    public static interface ResolvedBlit
    {
        public static final ResolvedBlit EMPTY = (ps, x, y, w, h) -> {};

        void blit(PoseStack ps, int x, int y, int w, int h);
    }

    private record RectI(int x0, int y0, int x1, int y1)
    {
    }
}
