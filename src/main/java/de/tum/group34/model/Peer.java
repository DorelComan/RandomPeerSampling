package de.tum.group34.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * @author Hannes Dorfmann
 */
public class Peer implements Serializable {

  private InetSocketAddress ipAddress; // SocketAddress (includes port)
  private Integer msgID;
  private byte[] hostkey;

  public Peer() {
  }

  public Peer(InetSocketAddress inetSocketAddress) {

    this.ipAddress = inetSocketAddress;
  }

  public InetSocketAddress getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(InetSocketAddress ipAddress) {
    this.ipAddress = ipAddress;
  }

  public void setMessageID(int id) {

    this.msgID = id;
  }

  public Integer getMessageID() {

    return msgID;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public void setHostkey(byte[] hostkey) {
    this.hostkey = hostkey;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public byte[] getHostkey() {
    return hostkey;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Peer)) return false;

    Peer peer = (Peer) o;

    if (ipAddress != null ? !ipAddress.equals(peer.ipAddress) : peer.ipAddress != null)
      return false;
    if (msgID != null ? !msgID.equals(peer.msgID) : peer.msgID != null) return false;
    return Arrays.equals(hostkey, peer.hostkey);
  }

  @Override public int hashCode() {
    int result = ipAddress != null ? ipAddress.hashCode() : 0;
    result = 31 * result + (msgID != null ? msgID.hashCode() : 0);
    result = 31 * result + Arrays.hashCode(hostkey);
    return result;
  }
}
