package de.tum.group34;

import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.net.InetSocketAddress;

/**
 * Responsible to create a new TcpClient to connect to the specified address port
 *
 * @author Hannes Dorfmann
 */
public class TcpClientFactory {
  private String loggingTag;

  public TcpClientFactory(String loggingTag) {
    this.loggingTag = loggingTag;
  }

  /**
   * Creates a new TcpClient
   *
   * @param address The address
   * @return a new TcpClient instance
   */
  public TcpClient<ByteBuf, ByteBuf> newClient(InetSocketAddress address) {
    return TcpClient.newClient(address)
        .enableWireLogging(loggingTag, LogLevel.DEBUG);
  }
}
