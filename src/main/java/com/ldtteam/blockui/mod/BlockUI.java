package com.ldtteam.blockui.mod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockUI implements ModInitializer
{
    public static final String MOD_ID = "blockui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize()
    {
        // Server-safe initialization (network + config framework stubs).
        // Filled in by later Fabric port tasks.
    }
}
