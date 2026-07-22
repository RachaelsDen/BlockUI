package com.ldtteam.blockui;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AlignmentTest
{
    @Test
    public void testTopLeftFlags()
    {
        assertFalse(Alignment.TOP_LEFT.isRightAligned());
        assertFalse(Alignment.TOP_LEFT.isBottomAligned());
        assertFalse(Alignment.TOP_LEFT.isHorizontalCentered());
        assertFalse(Alignment.TOP_LEFT.isVerticalCentered());
    }

    @Test
    public void testMiddleFlags()
    {
        assertFalse(Alignment.MIDDLE.isRightAligned());
        assertFalse(Alignment.MIDDLE.isBottomAligned());
        assertTrue(Alignment.MIDDLE.isHorizontalCentered());
        assertTrue(Alignment.MIDDLE.isVerticalCentered());
    }

    @Test
    public void testBottomRightFlags()
    {
        assertTrue(Alignment.BOTTOM_RIGHT.isRightAligned());
        assertTrue(Alignment.BOTTOM_RIGHT.isBottomAligned());
        assertFalse(Alignment.BOTTOM_RIGHT.isHorizontalCentered());
        assertFalse(Alignment.BOTTOM_RIGHT.isVerticalCentered());
    }
}
