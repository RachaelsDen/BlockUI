package com.ldtteam.blockui.mod;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.block.Blocks;

public final class ClientLifecycleSubscriber
{
    private static boolean registered;

    private ClientLifecycleSubscriber()
    {
        // no instances
    }

    public static void register()
    {
        if (registered)
        {
            return;
        }

        ColorProviderRegistry.BLOCK.register(
            (state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : 0x638fe9,
            Blocks.WATER_CAULDRON);

        registered = true;
    }
}
