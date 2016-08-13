/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tum.group34.protocol;

import java.nio.ByteBuffer;

/**
 * @author troll
 */
public abstract class Message {

  protected int size;

  private boolean headerAdded;
  private Protocol.MessageType type;

  protected Message() {
    this.size = 0;
    this.headerAdded = false;
  }

  protected final void addHeader(Protocol.MessageType type) {

    assert (!this.headerAdded);
    this.headerAdded = true;
    this.type = type;
    this.size += 4;
  }

  protected final void changeMessageType(Protocol.MessageType type) {
    assert (this.headerAdded);
    this.type = type;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Message)) {
      return false;
    }
    Message otherMsg = (Message) obj;
    if (otherMsg.getSize() != size) {
      return false;
    }
    return otherMsg.getType() == type;
  }

  @Override public int hashCode() {
    int result = size;
    result = 31 * result + (headerAdded ? 1 : 0);
    result = 31 * result + type.hashCode();
    return result;
  }

  /**
   * Serialize the message into a byte buffer.
   *
   * @param out the bytebuffer to hold the serialized message
   */
  protected void send(ByteBuffer out) {
    assert (this.headerAdded);
    out.putShort((short) this.size);
    out.putShort((short) this.type.getNumVal());
  }

  protected final void sendEmptyBytes(ByteBuffer out, int nbytes) {
    assert (0 < nbytes);
    byte[] zeros = new byte[nbytes];
    out.put(zeros);
  }

  /**
   * @return the size
   */
  public int getSize() {
    return size;
  }

  /**
   * @return the type
   */
  public Protocol.MessageType getType() {
    return type;
  }

  public static short unsignedShortFromByte(byte value) {
    return (short) (value & ((short) 0xff));
  }

  public static int unsignedIntFromShort(short value) {
    return ((int) value) & 0xffff;
  }

  public static long unsignedLongFromInt(int value) {
    return value & 0xffffffffL;
  }

  public static int getUnsignedShort(ByteBuffer buf) {
    return unsignedIntFromShort(buf.getShort());
  }
}
