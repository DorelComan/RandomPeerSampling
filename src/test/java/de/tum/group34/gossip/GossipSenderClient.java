package de.tum.group34.gossip;

import de.tum.group34.model.Peer;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class GossipSenderClient {

  public static void main(String[] args) {

    Peer peer = new Peer(new InetSocketAddress("127.0.0.1", 44599), 9991, 9992, ("").getBytes(
        StandardCharsets.UTF_8));
    GossipSender gossipSender = new GossipSender(peer, TcpClient.newClient("127.0.0.1", 7002));

    new Thread(() -> {
      gossipSender.sendOwnPeerPeriodically(5, TimeUnit.SECONDS, 5).toBlocking().first();
    }).start();
  }
}
