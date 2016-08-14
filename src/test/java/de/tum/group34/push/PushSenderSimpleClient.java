package de.tum.group34.push;

import de.tum.group34.RxTcpClientFactory;
import de.tum.group34.TcpClientFactory;
import de.tum.group34.model.Peer;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class PushSenderSimpleClient {

    public static void main(String[] args) {

        Peer peer = new Peer(new InetSocketAddress("127.0.0.1",1234));

        PushSender pushSender = new PushSender(peer, new RxTcpClientFactory("SIMPLE PUSH CLIENT"), 33234);

        ArrayList<Peer> peers = new ArrayList<>();
        peers.add(new Peer(new InetSocketAddress("127.0.0.1", 5555)));
        pushSender.sendMyId(peers).toBlocking().first();
    }
}
