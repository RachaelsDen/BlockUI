package com.ldtteam.blockui.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class BlockUIClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ClientLifecycleEvents.CLIENT_STARTED.register(minecraft -> {
            ClientLifecycleSubscriber.register();
            ClientEventSubscriber.register();
        });
    }
}
