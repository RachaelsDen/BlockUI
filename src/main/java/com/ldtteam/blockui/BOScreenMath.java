package com.ldtteam.blockui;

final class BOScreenMath
{
    private BOScreenMath()
    {
        // utility class
    }

    static double calcRelative(final double eventCoord, final double mcScale, final double offset, final double renderScale)
    {
        return (eventCoord * mcScale - offset) / renderScale;
    }

    static int calcCenteredOffset(final int framebufferExtent, final int minExtent, final int windowExtent, final double renderScale)
    {
        final int clampedExtent = Math.max(framebufferExtent, minExtent);
        return (int) Math.floor((clampedExtent - windowExtent * renderScale) / 2.0d);
    }
}
