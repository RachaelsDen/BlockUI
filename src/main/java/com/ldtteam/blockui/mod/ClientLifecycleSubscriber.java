package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.AtlasManager;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
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

        final ReloadableResourceManager resourceManager = (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
        AtlasManager.INSTANCE.addAtlas(resourceManager::registerReloadListener, BlockUI.MOD_ID);

        ColorProviderRegistry.BLOCK.register(
            (state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : 0x638fe9,
            Blocks.WATER_CAULDRON);

        registered = true;
    }
}
