package de.tum.group34.brahms;

import de.tum.group34.Brahms;
import de.tum.group34.model.Peer;
import de.tum.group34.nse.NseClient;
import de.tum.group34.pull.MockPeers;
import de.tum.group34.pull.PullClient;
import de.tum.group34.push.PushReceiver;
import de.tum.group34.push.PushSender;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.configuration.injection.MockInjection;
import rx.Observable;

import java.util.List;

public class BrahmsTest {

    @Test
    public void test() throws InterruptedException {

        //MockPeers.getPeerList(20).forEach(peer -> System.out.println(peer.getIpAddress().toString()));

        PullClient pullClient = new MockPullClient();


        // Class clazz = List.class;
        // ArgumentCaptor<List<Peer>> argumentCaptor = ArgumentCaptor.forClass(clazz);
        Mockito.when(pullClient.makePullRequests(Mockito.any()))
                .thenReturn(Observable.just(MockPeers.getPeerList(5)));

        PushSender pushSender = Mockito.mock(PushSender.class);

        PushReceiver pushReceiver = Mockito.mock(PushReceiver.class);
        Mockito.when(pushReceiver.getPushList()).thenReturn(Observable.just(MockPeers.getPeerList(5)));

        NseClient nseClient = Mockito.mock(NseClient.class);
        Mockito.when(nseClient.getNetworkSize()).thenReturn(Observable.just(1000));

        List<Peer> initialList = MockPeers.getPeerList(1);

        Brahms brahms = new Brahms(initialList, nseClient, pullClient, pushReceiver, pushSender);

        brahms.start();
    }

}

