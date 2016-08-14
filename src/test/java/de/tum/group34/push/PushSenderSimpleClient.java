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

    Peer peer = new Peer(new InetSocketAddress("127.0.0.1", 1234), 56555, 56556,("").getBytes()); // ME CLIENT

    PushSender pushSender = new PushSender(peer, new RxTcpClientFactory("SIMPLE PUSH CLIENT"));

    ArrayList<Peer> peers = new ArrayList<>();
    peers.add(new Peer(new InetSocketAddress("127.0.0.1", 33234))); //Push to the Rps
    pushSender.sendMyId(peers).toBlocking().first();
  }
}
