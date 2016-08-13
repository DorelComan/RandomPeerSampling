package de.tum.group34.query;

import de.tum.group34.realsockets.SimpleServer;
import de.tum.group34.serialization.MessageParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import rx.Observable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * @author Hannes Dorfmann
 */
public class QueryClient {

  public static void main(String args[]) {

    TcpClient.newClient(new InetSocketAddress("127.0.0.1", 3558))
        .enableWireLogging("echo-client", LogLevel.DEBUG)
        .createConnectionRequest()
        .flatMap(connection ->
                connection.writeBytes(Observable.just(MessageParser.getRpsQuery()))
                .cast(ByteBuf.class)
                .concatWith(connection.getInput()
                )

        ).take(1)
            .map(byteBuf -> {

                ByteBuf addBuf = Unpooled.buffer(4);
                byteBuf.getBytes(8, addBuf, 4);
                InetAddress address = null;

                try {
                    address =  InetAddress.getByAddress(addBuf.array());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

                System.out.println("address: " + address.toString() + " port: " + byteBuf.getShort(4));

                return byteBuf.getShort(4);
            })
            .toBlocking()
            .forEach(System.out::println);
  }
}
