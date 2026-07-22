package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.AtlasManager;
import com.ldtteam.blockui.Loader;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.ModMismatchEvent;

public class ClientLifecycleSubscriber
{
    @SubscribeEvent
    public static void onAddClientReloadListeners(final AddClientReloadListenersEvent event)
    {
        event.addListener(Identifier.fromNamespaceAndPath(BlockUI.MOD_ID, "loader"), Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterTextureAtlases(final RegisterTextureAtlasesEvent event)
    {
        // deferred: native atlas registration for custom GUI atlases
    }

    @SubscribeEvent
    public static void onRegisterBlockColor(final RegisterColorHandlersEvent.BlockTintSources event)
    {
        // deferred: water cauldron tint registration on 26.2
    }

    @SubscribeEvent
    public static void onModMismatch(final ModMismatchEvent event)
    {
        // there are no world data and rest is mod compat anyway
        event.getVersionDifference(BlockUI.MOD_ID).ifPresent(id -> event.markResolved(BlockUI.MOD_ID));
    }
}
