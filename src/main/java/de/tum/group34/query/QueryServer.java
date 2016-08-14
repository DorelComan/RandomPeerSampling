package de.tum.group34.query;

import de.tum.group34.Brahms;
import de.tum.group34.serialization.MessageParser;
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

  public QueryServer(TcpServer<ByteBuf, ByteBuf> server, Brahms brahms) {

    this.server = server
        .enableWireLogging("QueryServer module", LogLevel.DEBUG)
        .start(connection -> connection.writeBytesAndFlushOnEach(
            connection.getInput()
                .doOnNext(byteBuf -> log.info("QUERY REQUEST received"))
                .flatMap(byteBuf -> {
                  MessageParser.isRpsQuery(byteBuf);
                  return brahms.getRandomPeerObservable();
                })
                .take(1)
                .map(peer -> MessageParser.buildRpsRespone(peer).array())
                .doOnNext(bytes -> log.info("QUERY REQUEST sending answer out"))
            )
        );
  }

  public void awaitShutdown() {
    server.awaitShutdown();
  }

  public void shutdown() {
    server.shutdown();
  }
}
