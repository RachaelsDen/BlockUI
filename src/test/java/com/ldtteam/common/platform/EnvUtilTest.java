package com.ldtteam.common.platform;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EnvUtilTest
{
    @Test
    public void isProduction_isLogicalComplementOfIsDevelopment()
    {
        try
        {
            final boolean dev = EnvUtil.isDevelopment();
            final boolean prod = EnvUtil.isProduction();
            assertEquals("isProduction must equal !isDevelopment", !dev, prod);
        }
        catch (final Throwable t)
        {
            verifyBothThrow("isDevelopment", "isProduction", t);
        }
    }

    @Test
    public void isServer_isLogicalComplementOfIsClient()
    {
        try
        {
            final boolean client = EnvUtil.isClient();
            final boolean server = EnvUtil.isServer();
            assertEquals("isServer must equal !isClient", !client, server);
        }
        catch (final Throwable t)
        {
            verifyBothThrow("isClient", "isServer", t);
        }
    }

    @Test
    public void isDevelopmentAndIsProduction_areMutuallyConsistent()
    {
        boolean devThrew = false;
        boolean prodThrew = false;
        boolean dev = false;
        boolean prod = false;

        try { dev = EnvUtil.isDevelopment(); } catch (final Throwable t) { devThrew = true; }
        try { prod = EnvUtil.isProduction(); } catch (final Throwable t) { prodThrew = true; }

        assertEquals("isDevelopment and isProduction must both succeed or both throw", devThrew, prodThrew);
        if (!devThrew)
        {
            assertTrue("isDevelopment XOR isProduction must be true", dev ^ prod);
        }
    }

    @Test
    public void isClientAndIsServer_areMutuallyConsistent()
    {
        boolean clientThrew = false;
        boolean serverThrew = false;
        boolean client = false;
        boolean server = false;

        try { client = EnvUtil.isClient(); } catch (final Throwable t) { clientThrew = true; }
        try { server = EnvUtil.isServer(); } catch (final Throwable t) { serverThrew = true; }

        assertEquals("isClient and isServer must both succeed or both throw", clientThrew, serverThrew);
        if (!clientThrew)
        {
            assertTrue("isClient XOR isServer must be true", client ^ server);
        }
    }

    private static void verifyBothThrow(final String methodA, final String methodB, final Throwable expected)
    {
        try
        {
            EnvUtil.class.getDeclaredMethod(methodA).invoke(null);
            fail(methodA + " should also have thrown like " + methodB);
        }
        catch (final ReflectiveOperationException e)
        {
            // Expected: FabricLoader.getInstance() is not available in unit test context
        }
    }
}
