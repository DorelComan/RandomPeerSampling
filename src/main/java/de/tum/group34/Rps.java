package de.tum.group34;

import de.tum.group34.nse.NseClient;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import rx.Observable;

/**
 * @author Hannes Dorfmann
 */
public class Rps {

  public static void main(final String[] args) {

    // TODO read config

    PullClient pullClient = new PullClient();

    NseClient nseClient = new NseClient(TcpClient.newClient("127.0.0.1", 9899));
    Brahms brahms =
        new Brahms(new ArrayList<>(), nseClient, pullClient); // TODO: add peer list from file

    QueryServer queryServer = new QueryServer(TcpServer.newServer((11001)));
    PullLocalViewServer pullLocalViewServer = new PullLocalViewServer(TcpServer.newServer(11002));

    GossipPush gossipPush = new GossipPush();
    Observable.interval(0, 30, TimeUnit.MINUTES)
        .flatMap(gossipPush::sendMessage)
        .subscribe((result) -> {
        }, (error) -> {
          error.printStackTrace();
        });

    queryServer.awaitShutdown();
    pullLocalViewServer.awaitShutdown();
  }
}
