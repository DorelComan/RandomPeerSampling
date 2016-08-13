package de.tum.group34.query;

import de.tum.group34.realsockets.SimpleServer;
import de.tum.group34.serialization.MessageParser;
import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import rx.Observable;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * @author Hannes Dorfmann
 */
public class QueryClient {

  public static void main(String args[]) {

    TcpClient.newClient(new InetSocketAddress("127.0.0.1", 3555))
        .enableWireLogging("echo-client", LogLevel.DEBUG)
        .createConnectionRequest()
        .flatMap(connection ->
            connection.writeBytes(Observable.just(MessageParser.getRpsQuery()))
                .cast(ByteBuf.class)
                .concatWith(connection.getInput())
        )
        .take(1)
        .map(bb -> bb.getShort(32))
        .toBlocking()
        .forEach(System.out::println);
  }
}
