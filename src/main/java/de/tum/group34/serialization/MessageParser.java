package de.tum.group34.serialization;

import de.tum.group34.model.Peer;
import de.tum.group34.model.PeerSharingMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetAddress;

public class MessageParser {

  /**
   * Checks if the incomming message is a RPS QUERY
   *
   * @param buf ByteBuffer The incomming message
   * @throws MessageException If message is not as expected
   */
  public static void isRpsQuery(ByteBuf buf) throws MessageException {

    int buf_size = buf.readableBytes();

    if (buf_size > Message.MAX_LENGTH) {
      throw new MessageException(); // TODO: pass message string
    }

    int size = unsignedIntFromShort(buf.getShort(0));// Reading size of the header

    if (size != buf_size) {        // verifying the declared size and the received one
      throw new MessageException();
    }

    int type = unsignedIntFromShort(buf.getShort(2)); // type of message

    if (type != Message.TYPE_RPS_QUERY) {
      throw new MessageException();
    }
  }

  /**
   * Checks if the incoming message is a PullLocalView
   *
   * @throws MessageException
   */
  public static void isPullLocalViewMessage(ByteBuf byteBuf) throws MessageException {
    // TODO implement
  }

  public static ByteBuf buildRpsRespone(Peer peer) {

    int size = 8; // Counting the size, 64 bit is the header
    size += peer.getHostkey().length;
    InetAddress inetAddress = peer.getIpAddress().getAddress();
    byte[] address = inetAddress.getAddress();

    size += address.length;

    ByteBuf buf = Unpooled.buffer(size);

    buf.setShort(2, (short) Message.TYPE_RPS_PEER); // Setting type
    buf.setShort(4, (short) peer.getIpAddress().getPort()); // Setting port

    buf.setBytes(8, address);

    buf.setBytes(size, peer.getHostkey());
    buf.setShort(0, (short) size);

    return buf;
  }

  public static ByteBuf buildGossipPushValidationResponse(int messageId, boolean valid) {

    ByteBuf buf = Unpooled.buffer(8);
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
    if (size != buf_size) {    // verifying the declared size and the received one
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
   */
  public static ByteBuf buildGossipAnnouncePush(Peer peer, int ttl) {

    byte[] peerBuf = SerializationUtils.toBytes(peer);
    int size = 8 + peerBuf.length; // Size of message if IPv4
    ByteBuf byteBuf = Unpooled.buffer(size);

    byteBuf.setShort(0, (short) size);
    byteBuf.setShort(2, (short) Message.GOSSIP_ANNOUNCE); // setting type of announce
    byteBuf.setShort(6, (short) Message.GOSSIP_PUSH);
    byteBuf.setByte(4, (byte) (ttl & 0xFF));
    byteBuf.setByte(5, (byte) ((ttl >> 8) & 0xFF));
    byteBuf.setBytes(8, peerBuf);

    return byteBuf;
  }

  /**
   * Creates a message that is responsible to be register himself at gossip module
   *
   * @return bytebuf representing this message
   */
  public static ByteBuf buildRegisterForNotificationsMessages() {

    ByteBuf byteBuf = Unpooled.buffer(8);

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
    ByteBuf buf = Unpooled.buffer(4);
    buf.setShort(0, 4);
    buf.setShort(2, (short) Message.NSE_QUERY);

    return buf;
  }

  public static int getSizeFromNseMessage(ByteBuf byteBuf) {
    return (int) byteBuf.getUnsignedInt(4);
  }

  /**
   * The message that will be send from one RPS module to another to ask for the local view list.
   */
  public static ByteBuf getPullLocalView() {

    ByteBuf buf = Unpooled.buffer(4);
    buf.setShort(0, 4);
    buf.setShort(2, (short) Message.PULL_LOCAL_VIEW);

    return buf;
  }

  public static boolean isPullLocalView(ByteBuf buf) {

    return MessageParser.unsignedIntFromShort(buf.getShort(2)) == Message.PULL_LOCAL_VIEW
        && !(buf.capacity() != 4 && MessageParser.unsignedIntFromShort(buf.getShort(0)) != 4);
  }

  public static byte[] getRpsQuery() {

    ByteBuf buf = Unpooled.buffer(4);
    buf.setShort(0, 4);
    buf.setShort(2, (short) Message.TYPE_RPS_QUERY);

    return buf.array();
  }
}
