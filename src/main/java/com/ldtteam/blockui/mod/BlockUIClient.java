package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.mod.container.ContainerHook;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BlockUIClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener()
        {
            @Override
            public ResourceLocation getFabricId()
            {
                return ResourceLocation.fromNamespaceAndPath(BlockUI.MOD_ID, "xml_loader");
            }

            @Override
            public CompletableFuture<Void> reload(final PreparableReloadListener.PreparationBarrier preparationBarrier,
                final ResourceManager resourceManager,
                final ProfilerFiller preparationsProfiler,
                final ProfilerFiller reloadProfiler,
                final Executor backgroundExecutor,
                final Executor gameExecutor)
            {
                return Loader.INSTANCE.reload(
                    preparationBarrier,
                    resourceManager,
                    preparationsProfiler,
                    reloadProfiler,
                    backgroundExecutor,
                    gameExecutor);
            }

            @Override
            public String getName()
            {
                return Loader.INSTANCE.getName();
            }
        });

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener()
        {
            @Override
            public ResourceLocation getFabricId()
            {
                return ResourceLocation.fromNamespaceAndPath(BlockUI.MOD_ID, "container_tags");
            }

            @Override
            public void onResourceManagerReload(final ResourceManager resourceManager)
            {
                ContainerHook.init();
            }
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(minecraft -> {
            ClientLifecycleSubscriber.register();
            ClientEventSubscriber.register();
        });
    }
}
