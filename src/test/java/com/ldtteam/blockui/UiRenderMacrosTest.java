package com.ldtteam.blockui;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class UiRenderMacrosTest
{
    @Test
    public void testBlitRepeatableRejectsInvalidRepeatBoxArguments()
    {
        assertInvalidRepeatable(-1, 0, 1, 1, 2, 2);
        assertInvalidRepeatable(0, -1, 1, 1, 2, 2);
        assertInvalidRepeatable(2, 0, 1, 1, 2, 2);
        assertInvalidRepeatable(0, 2, 1, 1, 2, 2);
        assertInvalidRepeatable(0, 0, 0, 1, 2, 2);
        assertInvalidRepeatable(0, 0, 1, 0, 2, 2);
        assertInvalidRepeatable(1, 0, 2, 1, 2, 2);
        assertInvalidRepeatable(0, 1, 1, 2, 2, 2);
    }

    @Test
    public void testPopulateFillTrianglesEmitsExpectedVertices()
    {
        final RecordingVertexConsumer buffer = new RecordingVertexConsumer();

        UiRenderMacros.populateFillTriangles(new Matrix4f().identity(), buffer, 10, 20, 30, 40, 1, 2, 3, 4);

        assertEquals(6, buffer.vertices.size());
        assertVertex(buffer.vertices.get(0), 10.0f, 20.0f, 0.0f, 1, 2, 3, 4, null, null);
        assertVertex(buffer.vertices.get(1), 10.0f, 60.0f, 0.0f, 1, 2, 3, 4, null, null);
        assertVertex(buffer.vertices.get(2), 40.0f, 20.0f, 0.0f, 1, 2, 3, 4, null, null);
        assertVertex(buffer.vertices.get(3), 40.0f, 20.0f, 0.0f, 1, 2, 3, 4, null, null);
        assertVertex(buffer.vertices.get(4), 10.0f, 60.0f, 0.0f, 1, 2, 3, 4, null, null);
        assertVertex(buffer.vertices.get(5), 40.0f, 60.0f, 0.0f, 1, 2, 3, 4, null, null);
    }

    @Test
    public void testPopulateFillGradientTrianglesEmitsExpectedVertices()
    {
        final RecordingVertexConsumer buffer = new RecordingVertexConsumer();

        UiRenderMacros.populateFillGradientTriangles(new Matrix4f().identity(), buffer, 5, 6, 7, 8, 10, 20, 30, 40, 50, 60, 70, 80);

        assertEquals(6, buffer.vertices.size());
        assertVertex(buffer.vertices.get(0), 5.0f, 6.0f, 0.0f, 10, 30, 50, 70, null, null);
        assertVertex(buffer.vertices.get(1), 5.0f, 14.0f, 0.0f, 20, 40, 60, 80, null, null);
        assertVertex(buffer.vertices.get(2), 12.0f, 6.0f, 0.0f, 10, 30, 50, 70, null, null);
        assertVertex(buffer.vertices.get(3), 12.0f, 6.0f, 0.0f, 10, 30, 50, 70, null, null);
        assertVertex(buffer.vertices.get(4), 5.0f, 14.0f, 0.0f, 20, 40, 60, 80, null, null);
        assertVertex(buffer.vertices.get(5), 12.0f, 14.0f, 0.0f, 20, 40, 60, 80, null, null);
    }

    @Test
    public void testPopulateBlitTrianglesEmitsExpectedVertices()
    {
        final RecordingVertexConsumer buffer = new RecordingVertexConsumer();

        UiRenderMacros.populateBlitTriangles(buffer, new Matrix4f().identity(), 1.5f, 2.5f, 3.5f, 4.5f, 0.1f, 0.2f, 0.3f, 0.4f);

        assertEquals(6, buffer.vertices.size());
        assertVertex(buffer.vertices.get(0), 1.5f, 3.5f, 0.0f, null, null, null, null, 0.1f, 0.3f);
        assertVertex(buffer.vertices.get(1), 1.5f, 4.5f, 0.0f, null, null, null, null, 0.1f, 0.4f);
        assertVertex(buffer.vertices.get(2), 2.5f, 3.5f, 0.0f, null, null, null, null, 0.2f, 0.3f);
        assertVertex(buffer.vertices.get(3), 2.5f, 3.5f, 0.0f, null, null, null, null, 0.2f, 0.3f);
        assertVertex(buffer.vertices.get(4), 1.5f, 4.5f, 0.0f, null, null, null, null, 0.1f, 0.4f);
        assertVertex(buffer.vertices.get(5), 2.5f, 4.5f, 0.0f, null, null, null, null, 0.2f, 0.4f);
    }

    private static void assertInvalidRepeatable(final int uRepeat,
        final int vRepeat,
        final int repeatWidth,
        final int repeatHeight,
        final int repeatBoxWidth,
        final int repeatBoxHeight)
    {
        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> UiRenderMacros.blitRepeatable(
            null,
            null,
            0,
            0,
            16,
            16,
            0.0f,
            0.0f,
            1.0f,
            1.0f,
            uRepeat,
            vRepeat,
            repeatWidth,
            repeatHeight,
            repeatBoxWidth,
            repeatBoxHeight));

        assertEquals("Repeatable box is outside of texture box", error.getMessage());
    }

    private static void assertVertex(final VertexRecord vertex,
        final float expectedX,
        final float expectedY,
        final float expectedZ,
        final Integer expectedRed,
        final Integer expectedGreen,
        final Integer expectedBlue,
        final Integer expectedAlpha,
        final Float expectedU,
        final Float expectedV)
    {
        assertEquals(expectedX, vertex.x, 0.0f);
        assertEquals(expectedY, vertex.y, 0.0f);
        assertEquals(expectedZ, vertex.z, 0.0f);
        assertEquals(expectedRed, vertex.red);
        assertEquals(expectedGreen, vertex.green);
        assertEquals(expectedBlue, vertex.blue);
        assertEquals(expectedAlpha, vertex.alpha);
        assertEquals(expectedU, vertex.u);
        assertEquals(expectedV, vertex.v);
    }

    private static final class RecordingVertexConsumer implements VertexConsumer
    {
        private final List<VertexRecord> vertices = new ArrayList<>();

        @Override
        public VertexConsumer addVertex(final float x, final float y, final float z)
        {
            vertices.add(new VertexRecord(x, y, z));
            return this;
        }

        @Override
        public VertexConsumer setColor(final int r, final int g, final int b, final int a)
        {
            final VertexRecord current = vertices.get(vertices.size() - 1);
            current.red = r;
            current.green = g;
            current.blue = b;
            current.alpha = a;
            return this;
        }

        @Override
        public VertexConsumer setColor(final int color)
        {
            final VertexRecord current = vertices.get(vertices.size() - 1);
            current.red = (color >> 16) & 0xFF;
            current.green = (color >> 8) & 0xFF;
            current.blue = color & 0xFF;
            current.alpha = (color >> 24) & 0xFF;
            return this;
        }

        @Override
        public VertexConsumer setUv(final float u, final float v)
        {
            final VertexRecord current = vertices.get(vertices.size() - 1);
            current.u = u;
            current.v = v;
            return this;
        }

        @Override
        public VertexConsumer setUv1(final int u, final int v)
        {
            return this;
        }

        @Override
        public VertexConsumer setUv2(final int u, final int v)
        {
            return this;
        }

        @Override
        public VertexConsumer setNormal(final float x, final float y, final float z)
        {
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(final float width)
        {
            return this;
        }
    }

    private static final class VertexRecord
    {
        private final float x;
        private final float y;
        private final float z;
        private Integer red;
        private Integer green;
        private Integer blue;
        private Integer alpha;
        private Float u;
        private Float v;

        private VertexRecord(final float x, final float y, final float z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
