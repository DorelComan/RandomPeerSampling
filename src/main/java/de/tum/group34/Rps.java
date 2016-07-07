package de.tum.group34;

import de.tum.group34.nse.NseClient;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import module.Peer;
import rx.Observable;

/**
 * @author Hannes Dorfmann
 */
public class Rps {

  private static final Logger log = Logger.getLogger(Rps.class.getName());

  public static void main(final String[] args) {

    // TODO read config

    PullClient pullClient = new PullClient();
    PushSender pushSender = new PushSender();
    PushReceiver pushReceiver = initPushReceiver();

    NseClient nseClient = new NseClient(TcpClient.newClient("127.0.0.1", 9899));

    GossipSender gossipSender = new GossipSender();
    Observable.interval(0, 30, TimeUnit.MINUTES)
        .flatMap(gossipSender::sendMessage)
        .subscribe((result) -> {
        }, (error) -> {
          error.printStackTrace();
        });

    List<Peer> initialList = pushReceiver.gossipSocket().toBlocking().first();

    // TODO: add peer list from file?
    Brahms brahms =
        new Brahms(initialList, nseClient, pullClient, pushReceiver,
            pushSender);

    QueryServer queryServer = new QueryServer(TcpServer.newServer((11001)), brahms);
    PullLocalViewServer pullLocalViewServer = new PullLocalViewServer(TcpServer.newServer(11002));

    queryServer.awaitShutdown();
    pullLocalViewServer.awaitShutdown();
    pushReceiver.awaitShutdown();
  }

  private static PushReceiver initPushReceiver() {
    return new PushReceiver(
        InetSocketAddress.createUnresolved("127.0.0.1", 11003)
        TcpServer.newServer(11004),
        TcpServer.newServer(11005));
  }
}
