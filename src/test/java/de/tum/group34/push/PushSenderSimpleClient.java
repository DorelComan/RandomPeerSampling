package de.tum.group34.push;

import de.tum.group34.RxTcpClientFactory;
import de.tum.group34.model.Peer;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Use this to do some direct Push to the RPS
 */
public class PushSenderSimpleClient {

  public static void main(String[] args) {

    Peer peer = new Peer(new InetSocketAddress("127.0.0.1", 1235), 54555, 54556,("").getBytes()); // ME CLIENT

    PushSender pushSender = new PushSender(peer, new RxTcpClientFactory("SIMPLE PUSH CLIENT"));

    ArrayList<Peer> peers = new ArrayList<>();
    peers.add(new Peer(new InetSocketAddress("127.0.0.1", 33234),55554,55553, ("").getBytes())); //Push to the Rps
    pushSender.sendMyId(peers).toBlocking().first();
  }
}
