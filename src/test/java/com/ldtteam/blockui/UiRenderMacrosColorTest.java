package com.ldtteam.blockui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UiRenderMacrosColorTest
{
    @Test
    public void testArgbChannelExtraction()
    {
        assertEquals(0x01, UiRenderMacros.alphaFromArgb(0x01020304));
        assertEquals(0x02, UiRenderMacros.redFromArgb(0x01020304));
        assertEquals(0x03, UiRenderMacros.greenFromArgb(0x01020304));
        assertEquals(0x04, UiRenderMacros.blueFromArgb(0x01020304));
    }

    @Test
    public void testOpaqueWhiteAndTransparentBlackExtraction()
    {
        assertEquals(0xFF, UiRenderMacros.alphaFromArgb(0xFFFFFFFF));
        assertEquals(0xFF, UiRenderMacros.redFromArgb(0xFFFFFFFF));
        assertEquals(0xFF, UiRenderMacros.greenFromArgb(0xFFFFFFFF));
        assertEquals(0xFF, UiRenderMacros.blueFromArgb(0xFFFFFFFF));

        assertEquals(0x00, UiRenderMacros.alphaFromArgb(0x00000000));
        assertEquals(0x00, UiRenderMacros.redFromArgb(0x00000000));
        assertEquals(0x00, UiRenderMacros.greenFromArgb(0x00000000));
        assertEquals(0x00, UiRenderMacros.blueFromArgb(0x00000000));
    }
}
