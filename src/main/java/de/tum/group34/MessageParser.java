package de.tum.group34;

import de.tum.group34.serialization.SerializationUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import module.Peer;

public class MessageParser {

    public static Boolean isRpsQuery(ByteBuf buf) throws MessageException, UnknownMessage {

        int buf_size = buf.readableBytes();

        if(buf_size > Message.MAX_LENGTH)
            throw new MessageException(); // TODO: pass message string

        int size = buf.getUnsignedShort(0); // Reading size of the header

        if(size != buf_size)        // verifying the declared size and the received one
            throw new MessageException();

        int type = buf.getUnsignedShort(16); // type of message

        return type == Message.TYPE_RPS_QUERY;
    }

    public static ByteBuf buildRpsRespone(Peer peer){

        ByteBuf buf = Unpooled.buffer();

        return buf;

    }

    public static Peer buildPeerFromGossipPush(ByteBuf buf) throws MessageException {
        int buf_size = buf.readableBytes();

        if(buf_size > Message.MAX_LENGTH)
            throw new MessageException(); // TODO: pass message string

        int size = buf.getUnsignedShort(0); // Reading size of the header

        if(size != buf_size)        // verifying the declared size and the received one
            throw new MessageException();

        int messageID = buf.getUnsignedShort(32);

        int type = buf.getUnsignedShort(48);

        if (type != Message.GOSSIP_PUSH)
            throw new MessageException();

        ByteBuf dst = Unpooled.buffer();
        buf.getBytes(64, dst);

        Peer peer = SerializationUtils.fromByteBuf(dst);
        peer.setMessageID(messageID);

        return peer;
    }
}
