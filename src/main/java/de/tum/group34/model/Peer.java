package de.tum.group34.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Hannes Dorfmann
 */
public class Peer implements Serializable, Cloneable {

  // TODO: how to determine own IP Address
  private InetSocketAddress ipAddress; // SocketAddress (includes port)
  private byte[] hostkey;
  private int msgId; // todo We should put the message here
  private int pushServerPort;
  private int pullServerPort;

  public Peer() {
    hostkey = ("").getBytes(StandardCharsets.UTF_8);
  }

  public Peer(InetSocketAddress inetSocketAddress) {

    hostkey = ("").getBytes(StandardCharsets.UTF_8);
    this.ipAddress = inetSocketAddress;
  }

  public InetSocketAddress getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(InetSocketAddress ipAddress) {
    this.ipAddress = ipAddress;
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

    if (ipAddress != null ? !ipAddress.equals(peer.ipAddress) : peer.ipAddress != null) {
      return false;
    }
    return Arrays.equals(hostkey, peer.hostkey);
  }

  @Override public int hashCode() {
    int result = ipAddress != null ? ipAddress.hashCode() : 0;
    result = 31 * result + Arrays.hashCode(hostkey);
    return result;
  }

  @Override public String toString() {
    return "Peer{" +
        "ipAddress=" + ipAddress +
        '}';
  }

  @SuppressFBWarnings("CN_IDIOM_NO_SUPER_CALL")
  @Override
  public Peer clone() {
    Peer peer = new Peer(this.ipAddress);
    peer.hostkey = this.hostkey;
    return peer;
  }

  public InetSocketAddress getPushServerAddress() {
    return new InetSocketAddress(ipAddress.getAddress(), pushServerPort);
  }

  public void setPushServerPort(int pushServerPort) {
    this.pushServerPort = pushServerPort;
  }

  public InetSocketAddress getPullServerPort() {
    return new InetSocketAddress(ipAddress.getAddress(), pullServerPort);
  }

  public void setPullServerPort(int pullServerPort) {
    this.pullServerPort = pullServerPort;
  }
}
