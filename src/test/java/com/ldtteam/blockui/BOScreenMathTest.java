package com.ldtteam.blockui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BOScreenMathTest
{
    @Test
    public void testCalcRelativeIdentity()
    {
        assertEquals(50.0d, BOScreenMath.calcRelative(50.0d, 1.0d, 0.0d, 1.0d), 0.0d);
    }

    @Test
    public void testCalcRelativeAppliesVanillaScaleBeforeOffsetAndRenderScale()
    {
        assertEquals(80.0d, BOScreenMath.calcRelative(100.0d, 2.0d, 40.0d, 2.0d), 0.0d);
        assertEquals(70.0d, BOScreenMath.calcRelative(100.0d, 1.0d, 30.0d, 1.0d), 0.0d);
        assertEquals(50.0d, BOScreenMath.calcRelative(100.0d, 1.0d, 0.0d, 2.0d), 0.0d);
    }

    @Test
    public void testCalcRelativeSupportsNegativeCoordinates()
    {
        assertEquals(-10.0d, BOScreenMath.calcRelative(-10.0d, 1.0d, 0.0d, 1.0d), 0.0d);
    }

    @Test
    public void testCalcRelativeCurrentZeroRenderScaleBehavior()
    {
        assertTrue(Double.isInfinite(BOScreenMath.calcRelative(1.0d, 1.0d, 0.0d, 0.0d)));
        assertTrue(Double.isNaN(BOScreenMath.calcRelative(0.0d, 1.0d, 0.0d, 0.0d)));
    }

    @Test
    public void testCalcCenteredOffsetAppliesMinimumExtentAndFlooring()
    {
        assertEquals(110, BOScreenMath.calcCenteredOffset(200, 320, 100, 1.0d));
        assertEquals(110, BOScreenMath.calcCenteredOffset(321, 320, 100, 1.0d));
        assertEquals(150, BOScreenMath.calcCenteredOffset(400, 320, 100, 1.0d));
    }

    @Test
    public void testCalcCenteredOffsetAccountsForRenderScaleAndOversizeWindows()
    {
        assertEquals(140, BOScreenMath.calcCenteredOffset(480, 320, 100, 2.0d));
        assertEquals(-90, BOScreenMath.calcCenteredOffset(100, 320, 500, 1.0d));
        assertEquals(-180, BOScreenMath.calcCenteredOffset(100, 240, 600, 1.0d));
    }
}
