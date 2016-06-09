package de.tum.group34;

import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.util.logging.Logger;

/**
 * Responsible to listen for incoming PULL REQUESTS to answer with the local view
 *
 * @author Hannes Dorfmann
 */
public class PullLocalViewServer {

  private static final Logger log = Logger.getLogger(PullLocalViewServer.class.getName());

  private TcpServer<ByteBuf, ByteBuf> server;

  public PullLocalViewServer(TcpServer<ByteBuf, ByteBuf> s) {
    this.server = s
        .enableWireLogging(LogLevel.DEBUG)
        .start(
            connection ->
                connection.writeBytesAndFlushOnEach(connection.getInput()
                    .doOnNext(byteBuf -> log.info("PULL REQUEST local view  received"))
                    .map(
                        (byteBuf -> "PULL local view response: Here should be the local view".getBytes()))

                )
        );
  }

  public void awaitShutdown() {
    server.awaitShutdown();
  }
}
