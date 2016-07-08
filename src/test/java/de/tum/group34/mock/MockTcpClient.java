package de.tum.group34.mock;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.client.ConnectionRequest;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.util.ArrayList;
import java.util.List;
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

  public void assertMessageSent(ByteBuf... messages) {
    List<ByteBuf> msgList = new ArrayList<>(messages.length);
    for (ByteBuf b : messages) {
      msgList.add(b);
    }

    assertMessageSent(msgList);
  }

  public void assertMessageSent(List<ByteBuf> messages) {
    getConnection().assertMessageSent(messages);
  }
}
