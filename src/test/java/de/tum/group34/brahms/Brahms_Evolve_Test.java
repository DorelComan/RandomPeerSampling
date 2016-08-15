package de.tum.group34.brahms;

import de.tum.group34.Brahms;
import de.tum.group34.RxTcpClientFactory;
import de.tum.group34.model.Peer;
import de.tum.group34.nse.NseClient;
import de.tum.group34.pull.RandomData;
import de.tum.group34.pull.PullClient;
import de.tum.group34.push.PushReceiver;
import de.tum.group34.push.PushSender;
import de.tum.group34.realsockets.NseServerRunner;
import org.mockito.Mockito;
import rx.Observable;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Brahms_Evolve_Test {

    public static void main(String[] args) throws InterruptedException {

        //MockPeers.getPeerList(20).forEach(peer -> System.out.println(peer.getIpAddress().toString()));

        PullClient pullClient = new MockPullClient();
        ((MockPullClient)pullClient).setSize(5);

        NseServerRunner server = new NseServerRunner();
        new Thread(server::start).start();

        NseClient nseClient = new de.tum.group34.nse.NseClient(new RxTcpClientFactory("NseClient"),
                new InetSocketAddress("127.0.0.1", NseServerRunner.PORT),1, TimeUnit.SECONDS);

        PushSender pushSender = Mockito.mock(PushSender.class);

        PushReceiver pushReceiver = Mockito.mock(PushReceiver.class);
        Mockito.when(pushReceiver.getPushList()).thenReturn(Observable.just(RandomData.getPeerList(5)));

        List<Peer> initialList = RandomData.getPeerList(1);

        Thread t1 = new Thread(() -> {
            while (true){
                System.out.println("Insert value");
                Scanner in = new Scanner(System.in);
                int size = in.nextInt();

                server.setSize(size);

                Double temp = Math.cbrt(size);

                ((MockPullClient)pullClient).setSize(temp.intValue());

                temp = temp * 0.45;

                Mockito.when(pushReceiver.getPushList()).thenReturn(Observable.just(RandomData.getPeerList(temp.intValue())));
                System.out.println("NEW SIZE: " + size);
            }
        });
        t1.start();

        Peer own = new Peer();
        own.setIpAddress(new InetSocketAddress("127.0.0.1", 30700));
        Brahms brahms = new Brahms(own,  nseClient, pullClient, pushReceiver, pushSender, new RxTcpClientFactory("Brahms"));
        brahms.start(initialList);
    }
}

