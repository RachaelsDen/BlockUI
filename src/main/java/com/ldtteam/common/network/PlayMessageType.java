package com.ldtteam.common.network;

import com.ldtteam.common.platform.EnvUtil;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Class to connect message type with proper sided registration.
 */
public record PlayMessageType<T extends AbstractUnsidedPlayMessage>(Type<T> id,
    StreamCodec<RegistryFriendlyByteBuf, T> codec,
    boolean allowNullPlayer,
    @Nullable PayloadAction<T, Player> client,
    @Nullable PayloadAction<T, ServerPlayer> server)
{
    // =============== MESSAGE VARIANTS ===============

    /**
     * Creates type for Server (sender) -> Client (receiver) message
     */
    public static <T extends AbstractClientPlayMessage> PlayMessageType<T> forClient(final String modId,
        final String messageName,
        final BiFunction<RegistryFriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
    {
        return forClient(modId, messageName, messageFactory, false, false);
    }

    /**
     * Creates type for Client (sender) -> Server (receiver) message
     */
    public static <T extends AbstractServerPlayMessage> PlayMessageType<T> forServer(final String modId,
        final String messageName,
        final BiFunction<RegistryFriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
    {
        return forServer(modId, messageName, messageFactory, false, false);
    }

    /**
     * Creates type for bidirectional message
     */
    public static <T extends AbstractPlayMessage> PlayMessageType<T> forBothSides(final String modId,
        final String messageName,
        final BiFunction<RegistryFriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
    {
        return forBothSides(modId, messageName, messageFactory, false, false);
    }

    /**
     * Creates type for Server (sender) -> Client (receiver) message
     *
     * @param playerNullable         if false then message wont execute without player
     * @param executeOnNetworkThread if true will execute on logical side main thread
     */
    public static <T extends AbstractClientPlayMessage> PlayMessageType<T> forClient(final String modId,
        final String messageName,
        final BiFunction<RegistryFriendlyByteBuf, PlayMessageType<T>, T> messageFactory,
        final boolean playerNullable,
        final boolean executeOnNetworkThread)
    {
        return codecise(new Type<>(ResourceLocation.fromNamespaceAndPath(modId, messageName)),
            messageFactory,
            playerNullable,
            threadRedirect(AbstractClientPlayMessage::onExecute, executeOnNetworkThread),
            null);
    }

    /**
     * Creates type for Client (sender) -> Server (receiver) message
     *
     * @param playerNullable         if false then message wont execute without player
     * @param executeOnNetworkThread if true will execute on logical side main thread
     */
    public static <T extends AbstractServerPlayMessage> PlayMessageType<T> forServer(final String modId,
        final String messageName,
        final BiFunction<RegistryFriendlyByteBuf, PlayMessageType<T>, T> messageFactory,
        final boolean playerNullable,
        final boolean executeOnNetworkThread)
    {
        return codecise(new Type<>(ResourceLocation.fromNamespaceAndPath(modId, messageName)),
            messageFactory,
            playerNullable,
            null,
            threadRedirect(AbstractServerPlayMessage::onExecute, executeOnNetworkThread));
    }

    /**
     * Creates type for bidirectional message
     *
     * @param playerNullable         if false then message wont execute without player
     * @param executeOnNetworkThread if true will execute on logical side main thread
     */
    public static <T extends AbstractPlayMessage> PlayMessageType<T> forBothSides(final String modId,
        final String messageName,
        final BiFunction<RegistryFriendlyByteBuf, PlayMessageType<T>, T> messageFactory,
        final boolean playerNullable,
        final boolean executeOnNetworkThread)
    {
        return codecise(new Type<>(ResourceLocation.fromNamespaceAndPath(modId, messageName)),
            messageFactory,
            playerNullable,
            threadRedirect(AbstractPlayMessage::onClientExecute, executeOnNetworkThread),
            threadRedirect(AbstractPlayMessage::onServerExecute, executeOnNetworkThread));
    }

    // =============== CODEC VARIANTS ===============

    /**
     * Creates type for Server (sender) -> Client (receiver) message
     */
    public static <T extends AbstractClientPlayMessage> PlayMessageType<T> forClient(final String modId,
        final String messageName,
        final StreamCodec<RegistryFriendlyByteBuf, T> codec)
    {
        return forClient(modId, messageName, codec, false, false);
    }

    /**
     * Creates type for Client (sender) -> Server (receiver) message
     */
    public static <T extends AbstractServerPlayMessage> PlayMessageType<T> forServer(final String modId,
        final String messageName,
        final StreamCodec<RegistryFriendlyByteBuf, T> codec)
    {
        return forServer(modId, messageName, codec, false, false);
    }

    /**
     * Creates type for bidirectional message
     */
    public static <T extends AbstractPlayMessage> PlayMessageType<T> forBothSides(final String modId,
        final String messageName,
        final StreamCodec<RegistryFriendlyByteBuf, T> codec)
    {
        return forBothSides(modId, messageName, codec, false, false);
    }

    /**
     * Creates type for Server (sender) -> Client (receiver) message
     *
     * @param playerNullable         if false then message wont execute without player
     * @param executeOnNetworkThread if true will execute on logical side main thread
     */
    public static <T extends AbstractClientPlayMessage> PlayMessageType<T> forClient(final String modId,
        final String messageName,
        final StreamCodec<RegistryFriendlyByteBuf, T> codec,
        final boolean playerNullable,
        final boolean executeOnNetworkThread)
    {
        return new PlayMessageType<>(new Type<>(ResourceLocation.fromNamespaceAndPath(modId, messageName)),
            codec,
            playerNullable,
            threadRedirect(AbstractClientPlayMessage::onExecute, executeOnNetworkThread),
            null);
    }

    /**
     * Creates type for Client (sender) -> Server (receiver) message
     *
     * @param playerNullable         if false then message wont execute without player
     * @param executeOnNetworkThread if true will execute on logical side main thread
     */
    public static <T extends AbstractServerPlayMessage> PlayMessageType<T> forServer(final String modId,
        final String messageName,
        final StreamCodec<RegistryFriendlyByteBuf, T> codec,
        final boolean playerNullable,
        final boolean executeOnNetworkThread)
    {
        return new PlayMessageType<>(new Type<>(ResourceLocation.fromNamespaceAndPath(modId, messageName)),
            codec,
            playerNullable,
            null,
            threadRedirect(AbstractServerPlayMessage::onExecute, executeOnNetworkThread));
    }

    /**
     * Creates type for bidirectional message
     *
     * @param playerNullable         if false then message wont execute without player
     * @param executeOnNetworkThread if true will execute on logical side main thread
     */
    public static <T extends AbstractPlayMessage> PlayMessageType<T> forBothSides(final String modId,
        final String messageName,
        final StreamCodec<RegistryFriendlyByteBuf, T> codec,
        final boolean playerNullable,
        final boolean executeOnNetworkThread)
    {
        return new PlayMessageType<>(new Type<>(ResourceLocation.fromNamespaceAndPath(modId, messageName)),
            codec,
            playerNullable,
            threadRedirect(AbstractPlayMessage::onClientExecute, executeOnNetworkThread),
            threadRedirect(AbstractPlayMessage::onServerExecute, executeOnNetworkThread));
    }

    /**
     * Register the payload type and the appropriate Fabric handlers.
     *
     * @param ignored retained for source-level call-site compatibility, ignored on Fabric
     */
    public void register(final Object ignored)
    {
        register();
    }

    public void register()
    {
        NetworkServerState.init();

        if (client != null)
        {
            registerClientbound();
        }

        if (server != null)
        {
            registerServerbound();
        }
    }

    private void registerClientbound()
    {
        if (REGISTERED_S2C_TYPES.add(id))
        {
            PayloadTypeRegistry.playS2C().register(id, codec);
        }

        if (EnvUtil.isClient() && REGISTERED_CLIENT_HANDLERS.add(id))
        {
            ClientReceiverRegistrar.register(id, this::onClient);
        }
    }

    private void registerServerbound()
    {
        if (REGISTERED_C2S_TYPES.add(id))
        {
            PayloadTypeRegistry.playC2S().register(id, codec);
        }

        if (REGISTERED_SERVER_HANDLERS.add(id))
        {
            ServerPlayNetworking.registerGlobalReceiver(id, this::onServer);
        }
    }

    private void onClient(final T payload, final ClientPlayNetworking.Context context)
    {
        final PlayMessageContext playMessageContext = new ClientPayloadContext(context);
        final Player player = playMessageContext.player();
        if (!allowNullPlayer && player == null)
        {
            wrongPlayerException(playMessageContext, payload);
            return;
        }

        client.handle(payload, playMessageContext, player);
    }

    private void onServer(final T payload, final ServerPlayNetworking.Context context)
    {
        final PlayMessageContext playMessageContext = new ServerPayloadContext(context);
        final ServerPlayer serverPlayer = playMessageContext.player() instanceof final ServerPlayer sp ? sp : null;
        if ((!allowNullPlayer && serverPlayer == null))
        {
            wrongPlayerException(playMessageContext, payload);
            return;
        }

        server.handle(payload, playMessageContext, serverPlayer);
    }

    private static <T extends AbstractUnsidedPlayMessage, U extends Player> PayloadAction<T, U> threadRedirect(
        final PayloadAction<T, U> payloadAction,
        final boolean executeOnNetworkThread)
    {
        return executeOnNetworkThread ? payloadAction :
            (payload, context, player) -> context.enqueueWork(() -> payloadAction.handle(payload, context, player));
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Set<Type<?>> REGISTERED_S2C_TYPES = ConcurrentHashMap.newKeySet();
    private static final Set<Type<?>> REGISTERED_C2S_TYPES = ConcurrentHashMap.newKeySet();
    private static final Set<Type<?>> REGISTERED_CLIENT_HANDLERS = ConcurrentHashMap.newKeySet();
    private static final Set<Type<?>> REGISTERED_SERVER_HANDLERS = ConcurrentHashMap.newKeySet();

    private static void wrongPlayerException(final PlayMessageContext context, final AbstractUnsidedPlayMessage payload)
    {
        final Player player = context.player();
        LOGGER.warn("Invalid packet received for - " + payload.getClass().getName() +
            " player: " +
            (player == null ? "MISSING" : player.getClass().getName()) +
            " logical-side: " +
            context.flow().getReceptionSide());
    }
    @FunctionalInterface
    private interface PayloadAction<T, U>
    {
        void handle(T payload, PlayMessageContext context, U player);
    }

    /**
     * Redirect our messages to {@link StreamCodec}
     */
    private static <T extends AbstractUnsidedPlayMessage> PlayMessageType<T> codecise(Type<T> id,
        BiFunction<RegistryFriendlyByteBuf, PlayMessageType<T>, T> messageFactory,
        boolean allowNullPlayer,
        @Nullable PayloadAction<T, Player> client,
        @Nullable PayloadAction<T, ServerPlayer> server)
    {
        final MessageStreamCodec<T> codec = new MessageStreamCodec<>(messageFactory);
        final PlayMessageType<T> type = new PlayMessageType<>(id, codec, allowNullPlayer, client, server);
        codec.type = type;
        return type;
    }

    /**
     * Codec for wrapping our messages
     */
    private static class MessageStreamCodec<T extends AbstractUnsidedPlayMessage> implements StreamCodec<RegistryFriendlyByteBuf, T>
    {
        private final BiFunction<RegistryFriendlyByteBuf, PlayMessageType<T>, T> messageFactory;
        private PlayMessageType<T> type;

        private MessageStreamCodec(BiFunction<RegistryFriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
        {
            this.messageFactory = messageFactory;
        }

        @Override
        public T decode(RegistryFriendlyByteBuf buf)
        {
            return messageFactory.apply(buf, type);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, T msg)
        {
            msg.toBytes(buf);
        }
    }

    @Environment(EnvType.CLIENT)
    private static final class ClientReceiverRegistrar
    {
        private ClientReceiverRegistrar()
        {
        }

        private static <T extends AbstractUnsidedPlayMessage> void register(final Type<T> id,
            final ClientPlayNetworking.PlayPayloadHandler<T> handler)
        {
            ClientPlayNetworking.registerGlobalReceiver(id, handler);
        }
    }

    @Environment(EnvType.CLIENT)
    private record ClientPayloadContext(ClientPlayNetworking.Context context) implements PlayMessageContext
    {
        @Override
        public Flow flow()
        {
            return Flow.CLIENTBOUND;
        }

        @Override
        public Player player()
        {
            return context.player();
        }

        @Override
        public net.fabricmc.fabric.api.networking.v1.PacketSender responseSender()
        {
            return context.responseSender();
        }

        @Override
        public void enqueueWork(final Runnable runnable)
        {
            context.client().execute(runnable);
        }
    }

    private record ServerPayloadContext(ServerPlayNetworking.Context context) implements PlayMessageContext
    {
        @Override
        public Flow flow()
        {
            return Flow.SERVERBOUND;
        }

        @Override
        public Player player()
        {
            return context.player();
        }

        @Override
        public net.fabricmc.fabric.api.networking.v1.PacketSender responseSender()
        {
            return context.responseSender();
        }

        @Override
        public void enqueueWork(final Runnable runnable)
        {
            context.server().execute(runnable);
        }
    }
}
