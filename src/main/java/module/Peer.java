package module;

import java.net.InetSocketAddress;

/**
 * @author Hannes Dorfmann
 */
public class Peer {
  private String peerIdentity;
  private InetSocketAddress ipAddress; // SocketAddress
  private int port;


  public InetSocketAddress getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(InetSocketAddress ipAddress) {
    this.ipAddress = ipAddress;
  }
}
