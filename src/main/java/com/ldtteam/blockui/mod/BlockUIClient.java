package com.ldtteam.blockui.mod;

import net.fabricmc.api.ClientModInitializer;

public class BlockUIClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ClientLifecycleSubscriber.register();
        ClientEventSubscriber.register();
    }
}
