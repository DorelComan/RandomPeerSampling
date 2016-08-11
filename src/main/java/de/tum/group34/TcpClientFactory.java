package de.tum.group34;

import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;

/**
 * Responsible to create a new TcpClient to connect to the specified address port
 *
 * @author Hannes Dorfmann
 */
public class TcpClientFactory {
  private String loggingTag;
  private String address;
  private int port;

  public TcpClientFactory(String address, int port, String loggingTag) {
    this.port = port;
    this.address = address;
    this.loggingTag = loggingTag;
  }

  public TcpClient<ByteBuf, ByteBuf> newClient() {
    return TcpClient.newClient(address, port).enableWireLogging(loggingTag, LogLevel.DEBUG);
  }
}
