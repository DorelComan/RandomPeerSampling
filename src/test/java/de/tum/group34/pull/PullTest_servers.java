package de.tum.group34.pull;

import de.tum.group34.Brahms;
import de.tum.group34.model.Peer;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import org.junit.Test;
import org.mockito.Mockito;
import rx.Observable;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class PullTest_servers {

    @Test
    public void test(){

        Brahms brahms = Mockito.mock(Brahms.class);
        Mockito.when(brahms.getLocalView()).thenReturn(MockPeers.getPeerList(20));

        List<Peer> serverList = new ArrayList<>();
        List<Peer> tempList;
        ArrayList<Observable<ArrayList<Peer>>> response = new ArrayList<>();

        //creating servers
        for(int i=0; i < 50; i++){
            PullServer pullServer = new PullServer(brahms, TcpServer.newServer(1102+i));
            Peer peer = new Peer(new InetSocketAddress("127.0.0.1", 1102 + i));

            serverList.add(peer);
            System.out.println("server "+ i + " ON\n");
        }

        for(int i = 0; i < 50; i++){
            System.out.println(serverList.get(i).getIpAddress().toString());
        }

        //creating clients
        for(int i=0; i < 10; i++){
            System.out.println("Starting client: " + i + "\n");
            PullClient pullClient = new PullClient();
            tempList = Brahms.rand(serverList, 20);
            response.add(pullClient.makePullRequests(tempList));
            System.out.println("END with client: " + i +"\n");
        }

        //Reading first peer of every client list
        for(int i=0; i < 10; i++){
            Observable<ArrayList<Peer>> list;
            list = response.get(i);

            System.out.println("reading: " + i);
            System.out.println(i + " - " + list.toBlocking().first().get(0).getIpAddress().toString());
        }
    }
}
