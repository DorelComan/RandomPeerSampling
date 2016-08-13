package de.tum.group34.realsockets;

import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.nio.charset.Charset;

/**
 * @author Hannes Dorfmann
 */
public class SimpleServer {

  public static final int PORT = 7777;

  public static void main(String args[]) {

    TcpServer<ByteBuf, ByteBuf> server;

        /*Starts a new TCP server on an ephemeral port.*/
    server = TcpServer.newServer(PORT)
        .enableWireLogging("echo-server", LogLevel.DEBUG)
        .start(connection -> connection.getInput()
            .map(bb -> bb.toString(Charset.defaultCharset()))
            .doOnNext(System.out::println)
            .map(s -> null)
        );

    server.awaitShutdown();
  }
}
