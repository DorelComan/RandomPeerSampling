package de.tum.group34.serialization;

import de.tum.group34.model.Peer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetAddress;

public class MessageParser {

  public static void isRpsQuery(ByteBuf buf) throws MessageException, UnknownMessage {

    int buf_size = buf.readableBytes();

    if (buf_size > Message.MAX_LENGTH) {
      throw new MessageException(); // TODO: pass message string
    }

    int size = unsignedIntFromShort(buf.getShort(0));// Reading size of the header

    if (size != buf_size)        // verifying the declared size and the received one
    {
      throw new MessageException();
    }

    int type = unsignedIntFromShort(buf.getShort(16)); // type of message

    if (type != Message.TYPE_RPS_QUERY) {
      throw new MessageException();
    }
  }

  public static ByteBuf buildRpsRespone(Peer peer) {

    ByteBuf buf = Unpooled.buffer();
    int size = 64; // Counting the size, 64 bit is the header

    buf.setShort(16, (short) Message.TYPE_RPS_PEER); // Setting type
    buf.setShort(32, (short) peer.getIpAddress().getPort()); // Setting port

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
    buf.setShort(0, (short) size);

    return buf;
  }

  public static Peer buildPeerFromGossipPush(ByteBuf buf) throws MessageException {

    int buf_size = buf.readableBytes();
    if (buf_size > Message.MAX_LENGTH) {
      throw new MessageException();
    }

    int size = unsignedIntFromShort(buf.getShort(0)); // Reading size of the header
    if (size != buf_size)    // verifying the declared size and the received one
    {
      throw new MessageException();
    }

    int messageID = unsignedIntFromShort(buf.getShort(32));
    int type = unsignedIntFromShort(buf.getShort(48));

    if (type != Message.GOSSIP_PUSH) {
      throw new MessageException();
    }

    ByteBuf dst = Unpooled.buffer();
    buf.getBytes(64, dst);

    Peer peer = SerializationUtils.fromByteBuf(dst);peer.setMessageID(messageID);

    return peer;
  }

  public static ByteBuf buildGossipPush(Peer peer, int ttl) {

    int size = 64;
    ByteBuf byteBuf = Unpooled.buffer();

    byteBuf.setShort(16, (short) Message.GOSSIP_ANNUNCE); // setting type of announce
    byteBuf.setShort(48, (short) Message.GOSSIP_PUSH);
    byteBuf.setByte(32, (byte) (ttl & 0xFF));
    byteBuf.setByte(33, (byte) ((ttl >> 8) & 0xFF));

    size += SerializationUtils.toByteBuf(peer).readableBytes();

    byteBuf.setShort(0, (short) size);

    return byteBuf;
  }

  public static ByteBuf getGossipNotifyForPush() {

    ByteBuf byteBuf = Unpooled.buffer();

    byteBuf.setShort(0, (short) 64); // Setting size of GOSSIP NOTIFY
    byteBuf.setShort(16, (short) Message.GOSSIP_NOTIFY);
    byteBuf.setShort(48, (short) Message.GOSSIP_PUSH);

    return byteBuf;
  }

  public static short unsignedShortFromByte(byte value) {
    return (short) (value & ((short) 0xff));
  }

  public static int unsignedIntFromShort(short value) {
    return ((int) value) & 0xffff;
  }

  public static ByteBuf getNseQuery() {

    //TODO: take a better look if short is correct
    ByteBuf buf = Unpooled.buffer();
    buf.setShort(0, 32);
    buf.setShort(16, 520);

    return buf;
  }

  public static int getSizeFromNseMessage(ByteBuf byteBuf) {

    return (int) byteBuf.getUnsignedInt(32);
  }

  /**
   * The message that will be send from one RPS module to another to ask for the local view list.
   */
  public static ByteBuf getPullLocalView() {

    ByteBuf buf = Unpooled.buffer();
    buf.setShort(16, 550);

    return buf;
  }
}
