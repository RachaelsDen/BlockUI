package com.ldtteam.common.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Shared context passed to play message handlers.
 */
public interface PlayMessageContext
{
    Flow flow();

    @Nullable
    Player player();

    PacketSender responseSender();

    void enqueueWork(Runnable runnable);

    enum Flow
    {
        CLIENTBOUND("client"),
        SERVERBOUND("server");

        private final String receptionSide;

        Flow(final String receptionSide)
        {
            this.receptionSide = receptionSide;
        }

        public String getReceptionSide()
        {
            return receptionSide;
        }
    }
}
