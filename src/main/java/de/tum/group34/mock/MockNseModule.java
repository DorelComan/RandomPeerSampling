package de.tum.group34.mock;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import rx.Observable;

/**
 * A very simply Mock NSE Module that answers with some random / or predefined NetworkSize numbers
 *
 * @author Hannes Dorfmann
 */
public class MockNseModule {

  public static final int PORT = 9944;

  public static void main(String[] args) {

    ByteBuf buf = Unpooled.buffer();
    buf.setShort(16, 23);

    TcpServer<ByteBuf, ByteBuf> server =
        TcpServer.newServer(PORT).enableWireLogging("Mock NSE Module", LogLevel.DEBUG)
            .start(connection -> connection.writeAndFlushOnEach(
                connection.getInput()
                    .doOnNext(byteBuf -> System.out.println("NSE: Incoming Query"))
                    .flatMap(byteBuf -> Observable.just(buf))
                )
            );

    System.out.println("NSE module server started");
    server.awaitShutdown();
  }
}
