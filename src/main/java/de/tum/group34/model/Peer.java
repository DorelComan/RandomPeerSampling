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

  private InetSocketAddress ipAddress; // SocketAddress (includes port)
  private byte[] hostkey;
  private int pushServerPort;
  private int pullServerPort;

  public Peer() {
    hostkey = ("").getBytes(StandardCharsets.UTF_8);
  }

  public Peer(InetSocketAddress inetSocketAddress) {

    hostkey = ("").getBytes(StandardCharsets.UTF_8);
    this.ipAddress = inetSocketAddress;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public Peer(InetSocketAddress inetSocketAddress, int pushServerPort, int pullServerPort,
      byte[] hostkey) {
    this.ipAddress = inetSocketAddress;
    this.pullServerPort = pullServerPort;
    this.pushServerPort = pushServerPort;
    this.hostkey = hostkey;
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

    if (pushServerPort != peer.pushServerPort) return false;
    if (pullServerPort != peer.pullServerPort) return false;
    if (ipAddress != null ? !ipAddress.equals(peer.ipAddress) : peer.ipAddress != null) {
      return false;
    }
    return Arrays.equals(hostkey, peer.hostkey);
  }

  @Override public int hashCode() {
    int result = ipAddress != null ? ipAddress.hashCode() : 0;
    result = 31 * result + Arrays.hashCode(hostkey);
    result = 31 * result + pushServerPort;
    result = 31 * result + pullServerPort;
    return result;
  }

  @SuppressFBWarnings("CN_IDIOM_NO_SUPER_CALL")
  @Override
  public Peer clone() {
    Peer peer = new Peer(this.ipAddress);
    peer.hostkey = this.hostkey;
    peer.pullServerPort = pullServerPort;
    peer.pushServerPort = pushServerPort;
    return peer;
  }

  public InetSocketAddress getPushServerAddress() {
    return new InetSocketAddress(ipAddress.getAddress(), pushServerPort);
  }

  public void setPushServerPort(int pushServerPort) {
    this.pushServerPort = pushServerPort;
  }

  public InetSocketAddress getPullServerAdress() {
    return new InetSocketAddress(ipAddress.getAddress(), pullServerPort);
  }

  public void setPullServerPort(int pullServerPort) {
    this.pullServerPort = pullServerPort;
  }

  public int getPushServerPort() {

    return this.pullServerPort;
  }

  @Override public String toString() {
    return "Peer{" +
        "ipAddress=" + ipAddress +
        ", pushServerPort=" + pushServerPort +
        ", pullServerPort=" + pullServerPort +
        '}';
  }
}
