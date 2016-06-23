package de.tum.group34;

import de.tum.group34.serialization.SerializationUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.util.logging.Logger;

/**
 * Responsible to listen for incoming PULL REQUESTS and to answer them with the local view from
 * Brahms
 *
 * @author Hannes Dorfmann
 */
public class PullServer {

  private static final Logger log = Logger.getLogger(PullServer.class.getName());

  private TcpServer<ByteBuf, ByteBuf> server;
  private Brahms brahms;

  public PullServer(Brahms brahms, TcpServer<ByteBuf, ByteBuf> server) {
    this.brahms = brahms;
    this.server = server.enableWireLogging(LogLevel.DEBUG)
        .start(
            connection ->
                connection.writeBytesAndFlushOnEach(connection.getInput()
                    .doOnNext(byteBuf -> log.info("PULL QUERY REQUEST received"))
                    .map((byteBuf -> SerializationUtils.toBytes(brahms.getLocalView()))
                    )
                )
        );
  }

  public void awaitShutdown() {
    server.awaitShutdown();
  }
}
