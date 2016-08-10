package de.tum.group34.mock;

import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
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

  @Override
  public final TcpClient<ByteBuf, ByteBuf> enableWireLogging(String name,
      LogLevel wireLoggingLevel) {
    return this;
  }

  @Override public final TcpClient<ByteBuf, ByteBuf> enableWireLogging(LogLevel wireLoggingLevel) {
    return this;
  }

  final MockWriteAndFlushOnEachConnection getConnection() {
    return connection;
  }

  public MockTcpClient assertMessagesSent(int count) {
    getConnection().assertMessagesSent(count);
    return this;
  }

  public MockTcpClient assertLastSentMessageEquals(ByteBuf lastMessage) {
    getConnection().assertLastSentMessageEquals(lastMessage);
    return this;
  }

  public MockTcpClient assertMessagesSent(ByteBuf... messages) {
    List<ByteBuf> msgList = new ArrayList<>(messages.length);
    for (ByteBuf b : messages) {
      msgList.add(b);
    }

    assertMessagesSent(msgList);
    return this;
  }

  public MockTcpClient assertMessagesSent(List<ByteBuf> messages) {
    getConnection().assertMessageSent(messages);
    return this;
  }

  /**
   * Deliver a incoming message right now
   *
   * @param message The message to deliver
   */
  public MockTcpClient deliverIncomingMessage(ByteBuf message) {
    deliverIncomingMessageDelayed(message, 0);
    return this;
  }

  /**
   * Deliver incoming message after waiting a period of time (blocking)
   *
   * @param message the message to deliver
   * @param sleepMs The miliseconds do wait (blocking) before delivering the message
   */
  public MockTcpClient deliverIncomingMessageDelayed(ByteBuf message, long sleepMs) {
    if (sleepMs > 0) {
      try {
        Thread.sleep(sleepMs);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    getConnection().deliverIncomingMessage(message);
    return this;
  }
}
