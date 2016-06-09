package de.tum.group34;

import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.util.logging.Logger;

/**
 * Responsible to listen for incoming QUERY REQUESTS and to answer them
 *
 * @author Hannes Dorfmann
 */
public class QueryServer {

  private static final Logger log = Logger.getLogger(QueryServer.class.getName());

  private TcpServer<ByteBuf, ByteBuf> server;

  public QueryServer(TcpServer<ByteBuf, ByteBuf> server) {
    this.server = server.enableWireLogging(LogLevel.DEBUG)
        .start(
            connection ->
                connection.writeBytesAndFlushOnEach(connection.getInput()
                    .doOnNext(byteBuf -> log.info("QUERY REQUEST received"))
                    .map((byteBuf -> "QUERY Response".getBytes())) // TODO implement
                )
        );
  }



  public void awaitShutdown() {
    server.awaitShutdown();
  }
}
