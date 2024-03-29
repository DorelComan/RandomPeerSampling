package de.tum.group34.mock;

import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.ConnectionHandler;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.util.ArrayList;
import java.util.List;
import org.mockito.Mockito;

/**
 * @author Hannes Dorfmann
 */
public abstract class MockTcpServer extends TcpServer<ByteBuf, ByteBuf> {

  private MockWriteAndFlushOnEachConnection connection;

  private MockTcpServer() {
  }

  final void setConnection(MockWriteAndFlushOnEachConnection connection) {
    this.connection = connection;
  }

  final MockWriteAndFlushOnEachConnection getConnection() {
    return connection;
  }

  @Override
  public final TcpServer<ByteBuf, ByteBuf> enableWireLogging(String name,
      LogLevel wireLoggingLevel) {
    return this;
  }

  @Override final public TcpServer<ByteBuf, ByteBuf> enableWireLogging(LogLevel wireLoggingLevel) {
    return this;
  }

  @Override
  public final TcpServer<ByteBuf, ByteBuf> start(
      ConnectionHandler<ByteBuf, ByteBuf> connectionHandler) {
    connectionHandler.handle(connection);
    return this;
  }

  public static MockTcpServer create() {
    MockTcpServer server = Mockito.spy(MockTcpServer.class);
    server.setConnection(new MockWriteAndFlushOnEachConnection());
    return server;
  }

  public MockTcpServer assertMessagesSent(int count) {
    getConnection().assertMessagesSent(count);
    return this;
  }

  public MockTcpServer assertLastSentMessageEquals(ByteBuf lastMessage) {
    getConnection().assertLastSentMessageEquals(lastMessage);
    return this;
  }

  public MockTcpServer assertMessageSent(ByteBuf... messages) {
    List<ByteBuf> msgList = new ArrayList<>(messages.length);
    for (ByteBuf b : messages) {
      msgList.add(b);
    }

    assertMessageSent(msgList);
    return this;
  }

  public MockTcpServer assertMessageSent(List<ByteBuf> messages) {
    getConnection().assertMessageSent(messages);
    return this;
  }

  public MockTcpServer addIncomingMessage(ByteBuf message) {
    getConnection().deliverIncomingMessage(message);
    return this;
  }

  public MockTcpServer simulateIncomingSocketError(Throwable error){
    getConnection().simulateIncommingSocketError(error);
    return this;
  }
}
