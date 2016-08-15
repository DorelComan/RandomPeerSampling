package de.tum.group34.gossip;

import de.tum.group34.model.Peer;
import de.tum.group34.push.PushReceiver;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.protocol.tcp.server.TcpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Testing the PushReceiver alone with the Gossip
 */

public class InterfaceWithGossip {

    public static void main(String[] args) {


        PushReceiver pushReceiver = initPushReceiver();
        pushReceiver.registerToGossip(InetSocketAddress.createUnresolved("127.0.0.1", 7001))
                .subscribe(aVoid -> {
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            System.out.println(
                                    "Shutting down RPS Module because no connection to Gossip Module is available");
                            System.exit(1);
                        });

        pushReceiver.awaitShutdown();


    }

    private static PushReceiver initPushReceiver() {
        return new PushReceiver(TcpServer.newServer(11005), 10, TimeUnit.SECONDS);
    }
}
