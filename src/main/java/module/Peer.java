package module;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * @author Hannes Dorfmann
 */
public class Peer implements Serializable {


  private String peerIdentity;
  private InetSocketAddress ipAddress; // SocketAddress (includes port)
  private Integer msgID;
  private byte[] hostkey;

  public InetSocketAddress getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(InetSocketAddress ipAddress) {
    this.ipAddress = ipAddress;
  }

  public void setMessageID(int id) {

    this.msgID = id;
  }

  public Integer getMessageID(){

    return msgID;
  }

  public void setHostkey(byte[] hostkey) {
    this.hostkey = hostkey;
  }

  public byte[] getHostkey() {
    return hostkey;
  }
}
