package com.ldtteam.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class NetworkCodecTest
{
    @Test
    public void flowEnum_clientbound_hasCorrectSide()
    {
        assertEquals("client", PlayMessageContext.Flow.CLIENTBOUND.getReceptionSide());
    }

    @Test
    public void flowEnum_serverbound_hasCorrectSide()
    {
        assertEquals("server", PlayMessageContext.Flow.SERVERBOUND.getReceptionSide());
    }

    @Test
    public void flowEnum_hasExactlyTwoValues()
    {
        assertEquals(2, PlayMessageContext.Flow.values().length);
    }

    @Test
    public void forClient_bifunction_createsValidType()
    {
        final PlayMessageType<TestClientMessage> type = PlayMessageType.forClient(
            "testmod", "client_msg", TestClientMessage::new);

        assertNotNull(type);
        assertNotNull(type.id());
        assertNotNull(type.codec());
        assertFalse(type.allowNullPlayer());
    }

    @Test
    public void forClient_bifunction_idHasCorrectResourceLocation()
    {
        final PlayMessageType<TestClientMessage> type = PlayMessageType.forClient(
            "testmod", "client_msg", TestClientMessage::new);

        final ResourceLocation rl = type.id().id();
        assertEquals("testmod", rl.getNamespace());
        assertEquals("client_msg", rl.getPath());
    }

    @Test
    public void forServer_bifunction_createsValidType()
    {
        final PlayMessageType<TestServerMessage> type = PlayMessageType.forServer(
            "testmod", "server_msg", TestServerMessage::new);

        assertNotNull(type);
        assertNotNull(type.id());
        assertNotNull(type.codec());
        assertFalse(type.allowNullPlayer());
    }

    @Test
    public void forBothSides_bifunction_createsValidType()
    {
        final PlayMessageType<TestBidirectionalMessage> type = PlayMessageType.forBothSides(
            "testmod", "bi_msg", TestBidirectionalMessage::new);

        assertNotNull(type);
        assertNotNull(type.id());
        assertNotNull(type.codec());
        assertFalse(type.allowNullPlayer());
    }

    @Test
    public void forClient_streamCodec_codecMatches()
    {
        final PlayMessageType<TestClientMessage> type = PlayMessageType.forClient(
            "testmod", "client_codec_msg", TestClientMessage.CODEC);

        assertSame(TestClientMessage.CODEC, type.codec());
    }

    @Test
    public void forServer_streamCodec_codecMatches()
    {
        final PlayMessageType<TestServerMessage> type = PlayMessageType.forServer(
            "testmod", "server_codec_msg", TestServerMessage.CODEC);

        assertSame(TestServerMessage.CODEC, type.codec());
    }

    @Test
    public void forBothSides_streamCodec_codecMatches()
    {
        final PlayMessageType<TestBidirectionalMessage> type = PlayMessageType.forBothSides(
            "testmod", "bi_codec_msg", TestBidirectionalMessage.CODEC);

        assertSame(TestBidirectionalMessage.CODEC, type.codec());
    }

    @Test
    public void forClient_bifunction_allowNullPlayerFlag()
    {
        final PlayMessageType<TestClientMessage> type = PlayMessageType.forClient(
            "testmod", "client_nullable", TestClientMessage::new, true, false);

        assertTrue(type.allowNullPlayer());
    }

    @Test
    public void forServer_bifunction_allowNullPlayerFlag()
    {
        final PlayMessageType<TestServerMessage> type = PlayMessageType.forServer(
            "testmod", "server_nullable", TestServerMessage::new, true, false);

        assertTrue(type.allowNullPlayer());
    }

    @Test
    public void forBothSides_bifunction_allowNullPlayerFlag()
    {
        final PlayMessageType<TestBidirectionalMessage> type = PlayMessageType.forBothSides(
            "testmod", "bi_nullable", TestBidirectionalMessage::new, true, false);

        assertTrue(type.allowNullPlayer());
    }

    @Test
    public void forClient_bifunction_correctResourceLocation()
    {
        final PlayMessageType<TestServerMessage> type = PlayMessageType.forServer(
            "testmod", "server_msg", TestServerMessage::new);

        final ResourceLocation rl = type.id().id();
        assertEquals("testmod", rl.getNamespace());
        assertEquals("server_msg", rl.getPath());
    }

    @Test
    public void forBothSides_bifunction_correctResourceLocation()
    {
        final PlayMessageType<TestBidirectionalMessage> type = PlayMessageType.forBothSides(
            "testmod", "bi_msg", TestBidirectionalMessage::new);

        final ResourceLocation rl = type.id().id();
        assertEquals("testmod", rl.getNamespace());
        assertEquals("bi_msg", rl.getPath());
    }

    @Test
    public void message_type_returnsCorrectId()
    {
        final PlayMessageType<TestClientMessage> type = PlayMessageType.forClient(
            "testmod", "type_check", TestClientMessage::new);

        final TestClientMessage msg = new TestClientMessage(type, "hello", 42);
        assertEquals(type.id(), msg.type());
    }

    @Test
    public void message_holdsCorrectData()
    {
        final PlayMessageType<TestClientMessage> type = PlayMessageType.forClient(
            "testmod", "data_check", TestClientMessage::new);

        final TestClientMessage msg = new TestClientMessage(type, "hello", 42);
        assertEquals("hello", msg.getText());
        assertEquals(42, msg.getNumber());
    }

    static final class TestClientMessage extends AbstractClientPlayMessage
    {
        private String text;
        private int    number;

        static final StreamCodec<RegistryFriendlyByteBuf, TestClientMessage> CODEC =
            new StreamCodec<>()
            {
                @Override
                public TestClientMessage decode(final RegistryFriendlyByteBuf buf)
                {
                    return new TestClientMessage(buf, null);
                }

                @Override
                public void encode(final RegistryFriendlyByteBuf buf, final TestClientMessage msg)
                {
                    msg.toBytes(buf);
                }
            };

        TestClientMessage(final PlayMessageType<TestClientMessage> type)
        {
            super(type);
            this.text = "";
            this.number = 0;
        }

        TestClientMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<TestClientMessage> type)
        {
            super(buf, type);
            this.text = buf.readUtf();
            this.number = buf.readVarInt();
        }

        TestClientMessage(final PlayMessageType<?> type, final String text, final int number)
        {
            super(type);
            this.text = text;
            this.number = number;
        }

        @Override
        protected void toBytes(final RegistryFriendlyByteBuf buf)
        {
            buf.writeUtf(text);
            buf.writeVarInt(number);
        }

        @Override
        protected void onExecute(final PlayMessageContext context, final Player player) { }

        String getText()   { return text; }
        int    getNumber() { return number; }
    }

    static final class TestServerMessage extends AbstractServerPlayMessage
    {
        static final StreamCodec<RegistryFriendlyByteBuf, TestServerMessage> CODEC =
            new StreamCodec<>()
            {
                @Override
                public TestServerMessage decode(final RegistryFriendlyByteBuf buf) { return null; }

                @Override
                public void encode(final RegistryFriendlyByteBuf buf, final TestServerMessage msg) { }
            };

        TestServerMessage(final PlayMessageType<TestServerMessage> type) { super(type); }

        TestServerMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<TestServerMessage> type)
        {
            super(buf, type);
        }

        @Override
        protected void toBytes(final RegistryFriendlyByteBuf buf) { }

        @Override
        protected void onExecute(final PlayMessageContext context, final ServerPlayer player) { }
    }

    static final class TestBidirectionalMessage extends AbstractPlayMessage
    {
        static final StreamCodec<RegistryFriendlyByteBuf, TestBidirectionalMessage> CODEC =
            new StreamCodec<>()
            {
                @Override
                public TestBidirectionalMessage decode(final RegistryFriendlyByteBuf buf) { return null; }

                @Override
                public void encode(final RegistryFriendlyByteBuf buf, final TestBidirectionalMessage msg) { }
            };

        TestBidirectionalMessage(final PlayMessageType<TestBidirectionalMessage> type) { super(type); }

        TestBidirectionalMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<TestBidirectionalMessage> type)
        {
            super(buf, type);
        }

        @Override
        protected void toBytes(final RegistryFriendlyByteBuf buf) { }

        @Override
        protected void onClientExecute(final PlayMessageContext context, final Player player) { }

        @Override
        protected void onServerExecute(final PlayMessageContext context, final ServerPlayer player) { }
    }
}
