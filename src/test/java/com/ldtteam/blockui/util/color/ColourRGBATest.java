package com.ldtteam.blockui.util.color;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColourRGBATest
{
    @Test
    public void white_allChannelsMax()
    {
        final ColourRGBA c = new ColourRGBA(0xFFFFFFFF);
        assertEquals(255, c.red());
        assertEquals(255, c.green());
        assertEquals(255, c.blue());
        assertEquals(255, c.alpha());
        assertEquals(1.0f, c.alphaF(), 0.0f);
    }

    @Test
    public void transparent_allChannelsZero()
    {
        final ColourRGBA c = new ColourRGBA(0x00000000);
        assertEquals(0, c.red());
        assertEquals(0, c.green());
        assertEquals(0, c.blue());
        assertEquals(0, c.alpha());
        assertEquals(0.0f, c.alphaF(), 0.0f);
    }

    @Test
    public void blackOpaque_rgbZeroAlphaMax()
    {
        final ColourRGBA c = new ColourRGBA(0x000000FF);
        assertEquals(0, c.red());
        assertEquals(0, c.green());
        assertEquals(0, c.blue());
        assertEquals(255, c.alpha());
        assertEquals(1.0f, c.alphaF(), 0.0f);
    }

    @Test
    public void redOpaque()
    {
        final ColourRGBA c = new ColourRGBA(0xFF0000FF);
        assertEquals(255, c.red());
        assertEquals(0, c.green());
        assertEquals(0, c.blue());
        assertEquals(255, c.alpha());
    }

    @Test
    public void rgbaToArgb_white()
    {
        final ColourRGBA c = new ColourRGBA(0xFFFFFFFF);
        assertEquals(0xFFFFFFFF, c.argb());
    }

    @Test
    public void rgbaToArgb_blackOpaque()
    {
        final ColourRGBA c = new ColourRGBA(0x000000FF);
        assertEquals(0xFF000000, c.argb());
    }

    @Test
    public void rgbaToArgb_distinctChannels()
    {
        final ColourRGBA c = new ColourRGBA(0x04030201);
        assertEquals(0x01040302, c.argb());
    }

    @Test
    public void rgbaToArgb_transparent()
    {
        final ColourRGBA c = new ColourRGBA(0x7F7F7F00);
        assertEquals(0x007F7F7F, c.argb());
    }

    @Test
    public void asRGBA_returnsSelf()
    {
        final ColourRGBA c = new ColourRGBA(0x12345678);
        assertEquals(c, c.asRGBA());
    }

    @Test
    public void asARGB_producesCorrectARGB()
    {
        final ColourRGBA c = new ColourRGBA(0x10203040);
        final ColourARGB argb = c.asARGB();

        assertEquals(0x40102030, argb.argb());
        assertEquals(0x10, argb.red());
        assertEquals(0x20, argb.green());
        assertEquals(0x30, argb.blue());
        assertEquals(0x40, argb.alpha());
    }

    @Test
    public void asARGB_roundTripsThroughRGBA()
    {
        final ColourRGBA original = new ColourRGBA(0x12345678);
        final ColourARGB argb = original.asARGB();
        final ColourRGBA back = argb.asRGBA();

        assertEquals(original.red(), back.red());
        assertEquals(original.green(), back.green());
        assertEquals(original.blue(), back.blue());
        assertEquals(original.alpha(), back.alpha());
    }

    @Test
    public void asQuartet_preservesAllChannels()
    {
        final ColourRGBA c = new ColourRGBA(0xDEADBEEF);
        final ColourQuartet q = c.asQuartet();

        assertEquals(0xDE, q.red());
        assertEquals(0xAD, q.green());
        assertEquals(0xBE, q.blue());
        assertEquals(0xEF, q.alpha());
    }

    @Test
    public void asQuartet_thenARGB_matchesOriginalARGB()
    {
        final ColourRGBA c = new ColourRGBA(0x10203040);
        final int expectedARGB = c.argb();
        final int actualARGB = c.asQuartet().asARGB().argb();

        assertEquals(expectedARGB, actualARGB);
    }

    @Test
    public void alphaF_halfAlpha()
    {
        final ColourRGBA c = new ColourRGBA(0x00000080);
        assertEquals(128 / 255.0f, c.alphaF(), 0.001f);
    }

    @Test
    public void rgba_roundTripThroughQuartet()
    {
        final int original = 0x12345678;
        final ColourRGBA c = new ColourRGBA(original);
        final ColourQuartet q = c.asQuartet();
        final ColourRGBA rebuilt = q.asRGBA();

        assertEquals(c.red(), rebuilt.red());
        assertEquals(c.green(), rebuilt.green());
        assertEquals(c.blue(), rebuilt.blue());
        assertEquals(c.alpha(), rebuilt.alpha());
    }

    @Test
    public void argb_fromARGB_roundTrip()
    {
        final int argbVal = 0xFF804020;
        final ColourARGB argb = new ColourARGB(argbVal);
        final ColourRGBA rgba = argb.asRGBA();
        final ColourARGB back = rgba.asARGB();

        assertEquals(argb.red(), rgba.red());
        assertEquals(argb.green(), rgba.green());
        assertEquals(argb.blue(), rgba.blue());
        assertEquals(argb.alpha(), rgba.alpha());
        assertEquals(argbVal, back.argb());
    }
}
