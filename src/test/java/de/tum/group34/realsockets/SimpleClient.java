package de.tum.group34.realsockets;

import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import rx.Observable;

/**
 * @author Hannes Dorfmann
 */
public class SimpleClient {

  public static void main(String args[]) {

    TcpClient.newClient(new InetSocketAddress("127.0.0.1", SimpleServer.PORT))
        .enableWireLogging("echo-client", LogLevel.DEBUG)
        .createConnectionRequest()
        .flatMap(connection ->
            connection.writeString(Observable.just("Hello World!"))
                .cast(ByteBuf.class)
                .concatWith(connection.getInput())
        )
        .take(1)
        .map(bb -> bb.toString(Charset.defaultCharset()))
        .toBlocking()
        .forEach(System.out::println);
  }
}
