package de.tum.group34.query;

import de.tum.group34.Brahms;
import de.tum.group34.RxTcpClientFactory;
import de.tum.group34.brahms.MockPullClient;
import de.tum.group34.model.Peer;
import de.tum.group34.nse.NseClient;
import de.tum.group34.pull.MockPeers;
import de.tum.group34.pull.PullClient;
import de.tum.group34.push.PushReceiver;
import de.tum.group34.push.PushSender;
import de.tum.group34.realsockets.NseServerRunner;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import org.mockito.Mockito;
import rx.Observable;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class QueryServerTest {

    public static void main(String[] args) throws InterruptedException {

        //MockPeers.getPeerList(20).forEach(peer -> System.out.println(peer.getIpAddress().toString()));

        PullClient pullClient = new MockPullClient();
        ((MockPullClient)pullClient).setSize(5);

        NseServerRunner server = new NseServerRunner();
        new Thread(server::start).start();

        NseClient nseClient = new NseClient(new RxTcpClientFactory("NseClient"), //Todo Modify
                new InetSocketAddress("127.0.0.1", NseServerRunner.PORT),1, TimeUnit.SECONDS);

        PushSender pushSender = Mockito.mock(PushSender.class);

        PushReceiver pushReceiver = Mockito.mock(PushReceiver.class);
        Mockito.when(pushReceiver.getPushList()).thenReturn(Observable.just(MockPeers.getPeerList(5)));

        List<Peer> initialList = MockPeers.getPeerList(1);

        Brahms brahms = new Brahms(initialList, nseClient, pullClient, pushReceiver, pushSender, new RxTcpClientFactory("Brahms"));
        new Thread(() -> {
            try {
                brahms.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        QueryServer queryServer = new QueryServer(TcpServer.newServer(3555), brahms);
        new Thread(queryServer::awaitShutdown).start();

        while (true){
            System.out.println("Insert value");
            Scanner in = new Scanner(System.in);
            int size = in.nextInt();

            server.setSize(size);

            Double temp = Math.cbrt(size);

            ((MockPullClient)pullClient).setSize(temp.intValue());

            temp = temp * 0.45;

            Mockito.when(pushReceiver.getPushList()).thenReturn(Observable.just(MockPeers.getPeerList(temp.intValue())));
            System.out.println("NEW SIZE: " + size);
        }
    }
}
