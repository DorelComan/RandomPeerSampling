package de.tum.group34;

import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.net.SocketAddress;
import rx.Observable;

/**
 * This class is responsible to send a push message
 *
 * @author Hannes Dorfmann
 */
public class GossipSender {

  /**
   * Send a push
   */
  public Observable<Void> sendMessage(long i) {
    // TODO implement
    return Observable.empty();
  }

  public Observable<Boolean> registerToGossip(SocketAddress gossipSocketAddress) {

    return TcpClient.newClient(gossipSocketAddress)
        .enableWireLogging(LogLevel.DEBUG)
        .createConnectionRequest()
        .flatMap(connection ->
            connection.writeString(Observable.just("Hello World!"))
                .cast(ByteBuf.class)
                .concatWith(connection.getInput())
        )
        .take(1)
        .map(byteBuf -> true);
  }
}
