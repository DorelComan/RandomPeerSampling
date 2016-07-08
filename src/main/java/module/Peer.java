package module;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * @author Hannes Dorfmann
 */
public class Peer implements Serializable {

  private InetSocketAddress ipAddress; // SocketAddress (includes port)
  private Integer msgID;
  private byte[] hostkey;

  public Peer() {
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
}
