package de.tum.group34.pull;

import de.tum.group34.model.Peer;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class MockPeers {

    public static Peer getPeer(){

        Peer peer = new Peer();
        peer.setIpAddress(new InetSocketAddress("127.0.0.33",2555));

        return peer;
    }

    public static ArrayList<Peer> getPeerList(){
        ArrayList<Peer> peers = new ArrayList<>();
        Integer val1, val2;
        Peer peer;

        if(((int)Math.random()% 4) < 2)
            return peers;

        for (int i=0; i < 20; i++){
            val1 = (int)(Math.random()%255);
            val2 = (int)(Math.random()%255);
            peer = new Peer();
            peer.setIpAddress(new InetSocketAddress("127.0." + val1 + "." + val2, val1 + 5000));
            peers.add(peer);
        }

        return peers;
    }
}
