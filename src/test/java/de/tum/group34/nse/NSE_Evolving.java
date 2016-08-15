package de.tum.group34.nse;

import de.tum.group34.Brahms;
import de.tum.group34.RxTcpClientFactory;
import de.tum.group34.model.Peer;
import de.tum.group34.nse.NseClient;
import de.tum.group34.pull.PullClient;
import de.tum.group34.push.PushReceiver;
import de.tum.group34.push.PushSender;
import de.tum.group34.realsockets.NseServerRunner;
import de.tum.group34.test.MockPullClient;
import de.tum.group34.test.RandomData;
import org.mockito.Mockito;
import rx.Observable;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Class for emulating the NSE module where you can also modify the value dinamically
 */
public class NSE_Evolving {

    public static void main(String[] args) throws InterruptedException {

        NseServerRunner server = new NseServerRunner();
        new Thread(server::start).start();


        Thread t1 = new Thread(() -> {
            while (true){
                System.out.println("Insert value");
                Scanner in = new Scanner(System.in);
                int size = in.nextInt();
                server.setSize(size);
                System.out.println("NEW SIZE: " + size);
            }
        });
        t1.start();
    }
}

