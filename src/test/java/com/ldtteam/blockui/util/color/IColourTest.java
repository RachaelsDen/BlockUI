package com.ldtteam.blockui.util.color;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class IColourTest
{
    @Test
    public void testRgbaBackedColourRoundTrip()
    {
        IColour col = new ColourRGBA(0x04030201);

        assertEquals(1.0f / 255, col.alphaF(), 0.0f);
        assertColour(col, 4, 3, 2, 1, 0x01040302, 0x04030201);

        col = col.asARGB();
        assertColour(col, 4, 3, 2, 1, 0x01040302, 0x04030201);

        col = col.asQuartet();
        assertColour(col, 4, 3, 2, 1, 0x01040302, 0x04030201);
    }

    @Test
    public void testArgbBackedColourRoundTrip()
    {
        final ColourARGB col = new ColourARGB(0x01020304);

        assertColour(col, 2, 3, 4, 1, 0x01020304, 0x02030401);
        assertSame(col, col.asARGB());
        assertColour(col.asRGBA(), 2, 3, 4, 1, 0x01020304, 0x02030401);
        assertColour(col.asQuartet(), 2, 3, 4, 1, 0x01020304, 0x02030401);
    }

    @Test
    public void testQuartetBackedColourRoundTrip()
    {
        final ColourQuartet col = new ColourQuartet(7, 8, 9, 10);

        assertColour(col, 7, 8, 9, 10, 0x0A070809, 0x0708090A);
        assertSame(col, col.asQuartet());
        assertColour(col.asRGBA(), 7, 8, 9, 10, 0x0A070809, 0x0708090A);
        assertColour(col.asARGB(), 7, 8, 9, 10, 0x0A070809, 0x0708090A);
    }

    @Test
    public void testWriteIntoBufferAndColouredVertexConsumerDelegation()
    {
        final RecordingVertexConsumer parent = new RecordingVertexConsumer();
        final ColouredVertexConsumer wrapper = new ColouredVertexConsumer(parent);

        wrapper.defaultColor = new ColourQuartet(11, 12, 13, 14);
        assertSame(wrapper, wrapper.addVertex(1.0f, 2.0f, 3.0f));
        assertSame(wrapper, wrapper.setUv(4.0f, 5.0f));
        assertSame(wrapper, wrapper.setUv1(6, 7));
        assertSame(wrapper, wrapper.setUv2(8, 9));
        assertSame(wrapper, wrapper.setNormal(10.0f, 11.0f, 12.0f));
        assertSame(wrapper, wrapper.misc(null, 1, 2, 3));
        assertSame(wrapper, wrapper.setDefaultColor());

        assertEquals(1.0f, parent.x, 0.0f);
        assertEquals(2.0f, parent.y, 0.0f);
        assertEquals(3.0f, parent.z, 0.0f);
        assertEquals(4.0f, parent.u, 0.0f);
        assertEquals(5.0f, parent.v, 0.0f);
        assertEquals(6, parent.overlayU);
        assertEquals(7, parent.overlayV);
        assertEquals(8, parent.lightU);
        assertEquals(9, parent.lightV);
        assertEquals(10.0f, parent.normalX, 0.0f);
        assertEquals(11.0f, parent.normalY, 0.0f);
        assertEquals(12.0f, parent.normalZ, 0.0f);
        assertEquals(11, parent.red);
        assertEquals(12, parent.green);
        assertEquals(13, parent.blue);
        assertEquals(14, parent.alpha);
        assertEquals(1, parent.miscCalls);
        assertEquals(1, parent.colorCalls);
    }

    private static void assertColour(final IColour col,
        final int expectedRed,
        final int expectedGreen,
        final int expectedBlue,
        final int expectedAlpha,
        final int expectedArgb,
        final int expectedRgba)
    {
        assertEquals(expectedRed, col.red());
        assertEquals(expectedGreen, col.green());
        assertEquals(expectedBlue, col.blue());
        assertEquals(expectedAlpha, col.alpha());
        assertEquals(expectedArgb, col.argb());
        assertEquals(expectedRgba, col.rgba());
    }

    private static final class RecordingVertexConsumer implements VertexConsumer
    {
        private float x;
        private float y;
        private float z;
        private int red;
        private int green;
        private int blue;
        private int alpha;
        private int colorCalls;
        private float u;
        private float v;
        private int overlayU;
        private int overlayV;
        private int lightU;
        private int lightV;
        private float normalX;
        private float normalY;
        private float normalZ;
        private int miscCalls;

        @Override
        public VertexConsumer addVertex(final float x, final float y, final float z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        @Override
        public VertexConsumer setColor(final int r, final int g, final int b, final int a)
        {
            this.red = r;
            this.green = g;
            this.blue = b;
            this.alpha = a;
            this.colorCalls++;
            return this;
        }

        @Override
        public VertexConsumer setColor(final int color)
        {
            this.red = (color >> 16) & 0xFF;
            this.green = (color >> 8) & 0xFF;
            this.blue = color & 0xFF;
            this.alpha = (color >> 24) & 0xFF;
            this.colorCalls++;
            return this;
        }

        @Override
        public VertexConsumer setUv(final float u, final float v)
        {
            this.u = u;
            this.v = v;
            return this;
        }

        @Override
        public VertexConsumer setUv1(final int u, final int v)
        {
            this.overlayU = u;
            this.overlayV = v;
            return this;
        }

        @Override
        public VertexConsumer setUv2(final int u, final int v)
        {
            this.lightU = u;
            this.lightV = v;
            return this;
        }

        @Override
        public VertexConsumer setNormal(final float x, final float y, final float z)
        {
            this.normalX = x;
            this.normalY = y;
            this.normalZ = z;
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(final float width)
        {
            return this;
        }

        @Override
        public VertexConsumer misc(final VertexFormatElement element, final int... values)
        {
            this.miscCalls++;
            return this;
        }
    }
}
