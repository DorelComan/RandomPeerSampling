package de.tum.group34.pull;

import de.tum.group34.model.Peer;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class MockPeers {

    public static Peer getPeer(){

        Peer peer = new Peer();
        Double val1, val2;
        val1 = Math.random()* 255;
        val2 = Math.random()* 255;
        peer.setIpAddress(new InetSocketAddress("127.0."+ val1.intValue()+ "." +val2.intValue(), 2555+val1.intValue()));

        return peer;
    }

    public static ArrayList<Peer> getPeerList(Integer n){
        ArrayList<Peer> peers = new ArrayList<>();
        Double val1, val2;
        Peer peer;

        for (int i=0; i < n; i++){
            val1 = Math.random()* 255;
            val2 = Math.random()* 255;
            peer = new Peer();
            peer.setIpAddress(new InetSocketAddress("127.0." + val1.intValue() + "." + val2.intValue(), val1.intValue() + 5000));
            peers.add(peer);
        }

        return peers;
    }
}
