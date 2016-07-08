package de.tum.group34.mock;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.client.ConnectionRequest;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import org.mockito.Mockito;

/**
 * A Mock TCP Client (to send messages to a server)
 *
 * @author Hannes Dorfmann
 */
public abstract class MockTcpClient extends TcpClient<ByteBuf, ByteBuf> {

  private MockWriteAndFlushOnEachConnection connection;

  public static MockTcpClient create() {

    MockWriteAndFlushOnEachConnection connection = new MockWriteAndFlushOnEachConnection();
    ConnectionRequest<ByteBuf, ByteBuf> connectionRequest =
        new MockConnectionRequest(connection);

    MockTcpClient tcpClient = Mockito.spy(MockTcpClient.class);
    Mockito.when(tcpClient.createConnectionRequest()).thenReturn(connectionRequest);
    // Mockito.when(tcpClient.getConnection()).thenCallRealMethod();

    tcpClient.setConnection(connection);

    return tcpClient;
  }

  final void setConnection(MockWriteAndFlushOnEachConnection connection) {
    this.connection = connection;
  }

  final MockWriteAndFlushOnEachConnection getConnection() {
    return connection;
  }

  public void assertMessagesSent(int count) {
    getConnection().assertMessagesSent(count);
  }

  public void assertLastSentMessageEquals(ByteBuf lastMessage) {
    getConnection().assertLastSentMessageEquals(lastMessage);
  }
}
