package de.tum.group34.query;

import de.tum.group34.serialization.MessageParser;
import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.net.InetSocketAddress;
import rx.Observable;

/**
 * @author Hannes Dorfmann
 */
public class QueryClient {

  private QueryClient() {
  }

  public static Observable<ByteBuf> query(String ip, int port) {

    return TcpClient.newClient(new InetSocketAddress(ip, port))
        .enableWireLogging("QueryClient", LogLevel.DEBUG)
        .createConnectionRequest()
        .flatMap(connection ->
            connection.writeBytes(Observable.just(MessageParser.getRpsQuery()))
                .cast(ByteBuf.class)
                .concatWith(connection.getInput())

        );
  }
}
