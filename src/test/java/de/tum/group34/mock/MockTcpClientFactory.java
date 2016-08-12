package de.tum.group34.mock;

import de.tum.group34.TcpClientFactory;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.net.InetSocketAddress;

/**
 * @author Hannes Dorfmann
 */
public class MockTcpClientFactory implements TcpClientFactory {

  private MockTcpClient clients[];
  private int clientIndex = -1;

  private MockTcpClientFactory(MockTcpClient[] clients) {
    if (clients == null) {
      throw new NullPointerException("No clients set");
    }
    this.clients = clients;
  }

  /**
   * Return a single TcpClie
   */
  public static TcpClientFactory withClients(MockTcpClient... clients) {
    return new MockTcpClientFactory(clients);
  }

  @Override public TcpClient<ByteBuf, ByteBuf> newClient(InetSocketAddress address) {
    clientIndex++;
    if (clientIndex >= clients.length) {
      throw new IndexOutOfBoundsException("You try to use a TcpClient at index "
          + clientIndex
          + " but there are only "
          + clients.length
          + " TcpClients available");
    }
    return clients[clientIndex];
  }
}
