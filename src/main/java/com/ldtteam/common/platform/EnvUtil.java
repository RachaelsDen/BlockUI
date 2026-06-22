package com.ldtteam.common.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Fabric-based environment utility for client/server checks.
 */
public final class EnvUtil
{
    private EnvUtil() { }

    public static boolean isClient()
    {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static boolean isServer()
    {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    public static boolean isDevelopment()
    {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean isProduction()
    {
        return !FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
