package de.tum.group34.brahms;

import de.tum.group34.Brahms;
import de.tum.group34.RxTcpClientFactory;
import de.tum.group34.model.Peer;
import de.tum.group34.nse.NseClient;
import de.tum.group34.pull.PullClient;
import de.tum.group34.pull.RandomData;
import de.tum.group34.push.PushReceiver;
import de.tum.group34.push.PushSender;
import java.net.InetSocketAddress;
import java.util.List;
import org.mockito.Mockito;
import rx.Observable;

public class BrahmsTest {

  // @Test
  public void test() throws InterruptedException {

    //MockPeers.getPeerList(20).forEach(peer -> System.out.println(peer.getIpAddress().toString()));

    PullClient pullClient = new MockPullClient();

    // Class clazz = List.class;
    // ArgumentCaptor<List<Peer>> argumentCaptor = ArgumentCaptor.forClass(clazz);
    Mockito.when(pullClient.makePullRequests(Mockito.any()))
        .thenReturn(Observable.just(RandomData.getPeerList(5)));

    PushSender pushSender = Mockito.mock(PushSender.class);

    PushReceiver pushReceiver = Mockito.mock(PushReceiver.class);
    Mockito.when(pushReceiver.getPushList()).thenReturn(Observable.just(RandomData.getPeerList(5)));

    NseClient nseClient = Mockito.mock(NseClient.class);
    Mockito.when(nseClient.getNetworkSize()).thenReturn(Observable.just(1000));

    List<Peer> initialList = RandomData.getPeerList(1);

    Peer own = new Peer();
    own.setIpAddress(new InetSocketAddress("127.0.0.1", 30700));

    Brahms brahms = new Brahms(own, nseClient, pullClient, pushReceiver, pushSender,
        new RxTcpClientFactory("Brahms"));

    brahms.start(initialList);
  }
}

