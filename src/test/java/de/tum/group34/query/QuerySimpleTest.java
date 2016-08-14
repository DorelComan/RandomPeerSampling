package de.tum.group34.query;

import de.tum.group34.Brahms;
import de.tum.group34.pull.RandomData;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import org.mockito.Mockito;
import rx.Observable;

public class QuerySimpleTest {


    public static void main(String[] args){
        Brahms brahms = Mockito.mock(Brahms.class);
        Mockito.when(brahms.getRandomPeerObservable()).thenReturn(Observable.just(RandomData.getPeer()));

        QueryServer queryServer = new QueryServer(TcpServer.newServer(3558), brahms);
        queryServer.awaitShutdown();
    }
}
