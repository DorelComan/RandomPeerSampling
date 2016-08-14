package de.tum.group34.pull;

import de.tum.group34.Brahms;
import de.tum.group34.model.Peer;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Single server used for testing the pull client
 */

public class PullTest_server {

    private static final int PULL_SERVER_PORT = 55555;

    public static void main(String[] args) {

        Brahms brahms = Mockito.mock(Brahms.class);
        Mockito.when(brahms.getLocalView()).thenReturn(RandomData.getPeerList(2));

        PullServer server = new PullServer(brahms, TcpServer.newServer(new InetSocketAddress("127.0.0.1", PULL_SERVER_PORT)));
        new Thread(server::awaitShutdown).start();

        //peer of server to be pull by client
        Peer peer = new Peer(new InetSocketAddress("127.0.0.1", PULL_SERVER_PORT), 1212, PULL_SERVER_PORT, ("").getBytes());

        ArrayList<Peer> peers = new ArrayList<>();
        peers.add(peer);

        PullClient pullClient = new PullClient();

        ArrayList<Peer> list = pullClient.makePullRequests(peers).toBlocking().first();

        list.forEach(peer1 -> System.out.println(peer1.getIpAddress()));
    }
}
