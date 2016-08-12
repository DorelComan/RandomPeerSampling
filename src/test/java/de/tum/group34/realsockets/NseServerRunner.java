package de.tum.group34.realsockets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * A very simply Mock NSE Module that answers with some random / or predefined NetworkSize numbers
 *
 * @author Hannes Dorfmann
 */
public class NseServerRunner {

    public static final int PORT = 9944;

    private ByteBuf buf;

    public static void main(String[] args) {

        NseServerRunner server = new NseServerRunner();
        server.start();
    }

    public void start(){

        setBuf(Unpooled.buffer());
        setSize(1000);

        TcpServer<ByteBuf, ByteBuf> server =
                TcpServer.newServer(PORT).enableWireLogging("Mock NSE Module", LogLevel.DEBUG)
                        .start(connection -> connection.writeBytesAndFlushOnEach(
                                connection.getInput()
                                        .doOnNext(
                                                byteBuf -> System.out.println(
                                                        "NSE: Incoming Query " + byteBuf.toString(Charset.defaultCharset())))
                                        .map(byteBuf -> getBuf().array())
                                )
                        );

        System.out.println("NSE module server started");
        server.awaitShutdown();
    }

    private synchronized ByteBuf getBuf() {
        return buf;
    }

    public synchronized void setSize(Integer size) {

        buf.setInt(32, size);
    }

    private synchronized void setBuf(ByteBuf buf){
        this.buf = buf;
    }
}
