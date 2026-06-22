package com.ldtteam.common.network;

import com.ldtteam.common.platform.EnvUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * List of possible network targets when sending from server to client.
 */
public interface IClientboundDistributor extends CustomPacketPayload
{
    /**
     * @see #sendToPlayer(ServerPlayer)
     */
    public default void sendToPlayer(final Collection<ServerPlayer> players)
    {
        for (final ServerPlayer serverPlayer : players)
        {
            sendToPlayer(serverPlayer);
        }
    }

    public default void sendToPlayer(final ServerPlayer player)
    {
        ServerPlayNetworking.send(player, this);
    }

    public default void sendToDimension(final ServerLevel serverLevel)
    {
        sendToPlayer(PlayerLookup.world(serverLevel));
    }

    public default void sendToTargetPoint(final ServerLevel level,
        @Nullable final ServerPlayer excluded,
        final double x,
        final double y,
        final double z,
        final double radius)
    {
        for (final ServerPlayer player : PlayerLookup.around(level, new Vec3(x, y, z), radius))
        {
            if (player != excluded)
            {
                sendToPlayer(player);
            }
        }
    }

    public default void sendToAllClients()
    {
        final MinecraftServer server = NetworkServerState.server();
        if (server == null)
        {
            reportInvalidTarget("Cannot send clientbound message without an active server: " + this.getClass().getName());
            return;
        }

        sendToPlayer(PlayerLookup.all(server));
    }

    public default void sendToTrackingEntity(final Entity entity)
    {
        sendToPlayer(PlayerLookup.tracking(entity));
    }

    public default void sendToTrackingEntityAndSelf(final Entity entity)
    {
        final LinkedHashSet<ServerPlayer> players = new LinkedHashSet<>(PlayerLookup.tracking(entity));
        if (entity instanceof final ServerPlayer serverPlayer)
        {
            players.add(serverPlayer);
        }

        sendToPlayer(players);
    }

    public default void sendToPlayersTrackingChunk(final LevelChunk chunk)
    {
        if (chunk.getLevel() instanceof final ServerLevel level)
        {
            sendToPlayersTrackingChunk(level, chunk.getPos());
            return;
        }

        reportInvalidTarget("Got client chunk for server network message: " + this.getClass().getName() + " - " + chunk.getClass().getName());
    }

    public default void sendToPlayersTrackingChunk(final ServerLevel serverLevel, final ChunkPos chunkPos)
    {
        sendToPlayer(PlayerLookup.tracking(serverLevel, chunkPos));
    }

    private void reportInvalidTarget(final String message)
    {
        if (EnvUtil.isProduction())
        {
            new IllegalArgumentException(message).printStackTrace();
        }
        else
        {
            throw new IllegalArgumentException(message);
        }
    }
}

final class NetworkServerState
{
    @Nullable
    private static volatile MinecraftServer currentServer;

    static
    {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (currentServer == server)
            {
                currentServer = null;
            }
        });
    }

    private NetworkServerState()
    {
    }

    static void init()
    {
    }

    @Nullable
    static MinecraftServer server()
    {
        return currentServer;
    }
}
