package de.tum.group34.pull;

import de.tum.group34.Brahms;
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

  private static final String LOG_TAG = "PullServer";
  private static final Logger log = Logger.getLogger(PullServer.class.getName());

  private TcpServer<ByteBuf, ByteBuf> server;

  public PullServer(Brahms brahms, TcpServer<ByteBuf, ByteBuf> server) {
    this.server = server.enableWireLogging(LOG_TAG, LogLevel.DEBUG)
        .start(
            connection ->
                connection.writeBytesAndFlushOnEach(connection.getInput()
                    .doOnNext(byteBuf -> log.info(LOG_TAG + " Query request received"))
                    .map((byteBuf -> SerializationUtils.toBytes(brahms.getLocalView())))
                    .doOnNext(bytes -> log.info(LOG_TAG + " sending query response"))
                )
        );
  }

  /**
   * Runs the server forever (until shutdown)
   */
  public void awaitShutdown() {
    server.awaitShutdown();
  }

  /**
   * Shuts the server down
   */
  public void shutDown() {
    server.shutdown();
  }
}
