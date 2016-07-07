package de.tum.group34;

import de.tum.group34.serialization.SerializationUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetAddress;
import module.Peer;

public class MessageParser {

  private static int SHORT_ADD = 32768;

  public static void isRpsQuery(ByteBuf buf) throws MessageException, UnknownMessage {

    int buf_size = buf.readableBytes();

    if (buf_size > Message.MAX_LENGTH) {
      throw new MessageException(); // TODO: pass message string
    }

    int size = buf.getShort(0) + SHORT_ADD;// Reading size of the header

    if (size != buf_size)        // verifying the declared size and the received one
    {
      throw new MessageException();
    }

    int type = buf.getShort(16) + SHORT_ADD;  // type of message

    if (type != Message.TYPE_RPS_QUERY) {
      throw new MessageException();
    }
  }

  public static ByteBuf buildRpsRespone(Peer peer) {

    ByteBuf buf = Unpooled.buffer();
    int size = 64; // Counting the size, 64 bit is the header

    buf.setShort(16, Message.TYPE_RPS_PEER - SHORT_ADD); // Setting type
    buf.setShort(32, peer.getIpAddress().getPort() - SHORT_ADD); // Setting port

    InetAddress inetAddress = peer.getIpAddress().getAddress();

    byte[] address = inetAddress.getAddress();
    if (address.length == 4) {
      size += 32;
    } else {
      size += 128;
    }

    buf.setBytes(64, address);

    buf.setBytes(size, peer.getHostkey());
    size += peer.getHostkey().length * 8;
    buf.setShort(0, size - SHORT_ADD);

    return buf;
  }

  public static Peer buildPeerFromGossipPush(ByteBuf buf) throws MessageException {
    int buf_size = buf.readableBytes();

    if (buf_size > Message.MAX_LENGTH) {
      throw new MessageException(); // TODO: pass message string
    }

    int size = buf.getUnsignedShort(0); // Reading size of the header

    if (size != buf_size)        // verifying the declared size and the received one
    {
      throw new MessageException();
    }

    int messageID = buf.getUnsignedShort(32);

    int type = buf.getUnsignedShort(48);

    if (type != Message.GOSSIP_PUSH) {
      throw new MessageException();
    }

    ByteBuf dst = Unpooled.buffer();
    buf.getBytes(64, dst);

    Peer peer = SerializationUtils.fromByteBuf(dst);
    peer.setMessageID(messageID);

    return peer;
  }

  public static ByteBuf buildGossipPush(Peer peer, int ttl) {
    int size = 64;

    ByteBuf byteBuf = Unpooled.buffer();

    byteBuf.setShort(16, Message.GOSSIP_ANNUNCE - SHORT_ADD); // setting type of announce

    return byteBuf;
  }

  public static ByteBuf getGossipNotifyForPush() {

    ByteBuf byteBuf = Unpooled.buffer();

    byteBuf.setShort(0, 64); // Setting size of GOSSIP NOTIFY
    byteBuf.setShort(16, Message.GOSSIP_NOTIFY - SHORT_ADD);
    byteBuf.setShort(48, Message.GOSSIP_PUSH - SHORT_ADD);

    return byteBuf;
  }
}
