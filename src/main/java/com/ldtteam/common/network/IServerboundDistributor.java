package com.ldtteam.common.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * List of possible network targets when sending from client to server.
 */
public interface IServerboundDistributor extends CustomPacketPayload
{
    public default void sendToServer()
    {
        throw new UnsupportedOperationException("Client-to-server packet path still needs 26.2 migration");
    }
}
