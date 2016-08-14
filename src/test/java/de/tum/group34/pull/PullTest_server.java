package de.tum.group34.pull;

import de.tum.group34.Brahms;
import de.tum.group34.model.Peer;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.mockito.Mockito;

/**
 * Single server used for testing the pull client
 */

public class PullTest_server {

  private static final int PULL_SERVER_PORT = 12341;

  public static void main(String[] args) {

    Brahms brahms = Mockito.mock(Brahms.class);
    Mockito.when(brahms.getLocalView()).thenReturn(RandomData.getPeerList(12));

    PullServer server = new PullServer(brahms,
        TcpServer.newServer(new InetSocketAddress("127.0.0.1", PULL_SERVER_PORT)));

    //peer of server to be pull by client
    Peer peer =
        new Peer(new InetSocketAddress("127.0.0.1", PULL_SERVER_PORT), 1212, PULL_SERVER_PORT,
            ("").getBytes(StandardCharsets.UTF_8));

    ArrayList<Peer> peers = new ArrayList<>();
    peers.add(peer);

    PullClient pullClient = new PullClient();

    pullClient.makePullRequests(peers)
        .toBlocking()
        .forEach(peers1 -> System.out.println("" + peers1));

    server.awaitShutdown();
  }
}
