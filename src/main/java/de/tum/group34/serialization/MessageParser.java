package de.tum.group34.serialization;

import de.tum.group34.model.Peer;
import de.tum.group34.model.PeerSharingMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetAddress;

public class MessageParser {

  public static void isRpsQuery(ByteBuf buf) throws MessageException, UnknownMessage {

    //TODO: repair it

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
    //TODO: repair it

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

  public static ByteBuf buildGossipPushValidationResponse(int messageId, boolean valid) {

    ByteBuf buf = Unpooled.buffer();
    int size = 8; // Overall Message is 64 bit

    buf.setShort(2, (short) Message.GOSSIP_VALIDATION); // Setting type
    buf.setShort(4, (short) messageId);
    buf.setShort(6, (short) (valid ? 1 : 0));
    buf.setShort(0, (short) size);

    return buf;
  }

  public static PeerSharingMessage buildPeerFromGossipPush(ByteBuf buf) throws MessageException {

    int buf_size = buf.readableBytes();
    if (buf_size > Message.MAX_LENGTH) {
      throw new MessageException();
    }

    int size = unsignedIntFromShort(buf.getShort(0)); // Reading size of the header
    if (size != buf_size){    // verifying the declared size and the received one
      throw new MessageException();
    }

    int messageID = unsignedIntFromShort(buf.getShort(4));
    int type = unsignedIntFromShort(buf.getShort(6));

    if (type != Message.GOSSIP_PUSH) {
      throw new MessageException();
    }

    ByteBuf dst = Unpooled.buffer(512); //512 size of Peer
    buf.getBytes(8, dst);

    Peer peer = SerializationUtils.fromByteBuf(dst);
    return new PeerSharingMessage(messageID, peer);
  }

  /**
   * Announce Message to be sent to Gossip to push the Peer trough the network
   *
   * @param peer
   * @param ttl
   * @return
     */
  public static ByteBuf buildGossipPush(Peer peer, int ttl) {

    int size = 520; // Size of message if IPv4
    ByteBuf byteBuf = Unpooled.buffer(size);

    byteBuf.setShort(0, (short) size);
    byteBuf.setShort(2, (short) Message.GOSSIP_ANNUNCE); // setting type of announce
    byteBuf.setShort(6, (short) Message.GOSSIP_PUSH);
    byteBuf.setByte(4, (byte) (ttl & 0xFF));
    byteBuf.setByte(5, (byte) ((ttl >> 8) & 0xFF));

    byte[] peerBuf = SerializationUtils.toBytes(peer);
    byteBuf.setBytes(8, peerBuf);

    return byteBuf;
  }

  /**
   * Creates a message that is responsible to be register himself at gossip module
   *
   * @return bytebuf representing this message
   */
  public static ByteBuf buildRegisterForNotificationsMessages() {

    ByteBuf byteBuf = Unpooled.buffer();

    byteBuf.setShort(0, (short) 8); // Setting size of GOSSIP NOTIFY
    byteBuf.setShort(2, (short) Message.GOSSIP_NOTIFY);
    byteBuf.setShort(6, (short) Message.GOSSIP_PUSH);

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
    buf.setShort(0, 4);
    buf.setShort(2,(short) 520);

    return buf;
  }

  public static int getSizeFromNseMessage(ByteBuf byteBuf) {
    return (int) byteBuf.getUnsignedInt(4);
  }

  /**
   * The message that will be send from one RPS module to another to ask for the local view list.
   */
  public static ByteBuf getPullLocalView() {

    ByteBuf buf = Unpooled.buffer();
    buf.setShort(0, 4);
    buf.setShort(2,(short) 550);

    return buf;
  }

  public static byte[] getRpsQuery(){

    ByteBuf buf = Unpooled.buffer();
    buf.setShort(0, 4);
    buf.setShort(2,(short) 520);

    return buf.array();
  }
}
