package de.tum.group34.test;


import de.tum.group34.Brahms;
import de.tum.group34.model.Peer;
import de.tum.group34.pull.PullServer;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import org.mockito.Mockito;

/**
 * This help to create a PullServer on the fly for testing of reliabily of the PullClient
 */
public class MockPullServer {

    public MockPullServer(Peer peer){

        Brahms brahms = Mockito.mock(Brahms.class);
        Mockito.when(brahms.getLocalView()).thenReturn(RandomData.getPeerListBound(12));

        new Thread(() -> {
            PullServer pullServer =
                    new PullServer(brahms, TcpServer.newServer(peer.getPullServerAdress()));
            System.out.println("IM ON");
            pullServer.awaitShutdown();
        }).start();
    }
}
