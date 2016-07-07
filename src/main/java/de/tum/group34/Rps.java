package de.tum.group34;

import de.tum.group34.nse.NseClient;
import de.tum.group34.serialization.SerializationUtils;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import module.Peer;
import rx.Observable;
import rx.subjects.PublishSubject;

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

    List<Peer> initialList = pushReceiver.incomingPeersFromGossip().toBlocking().first();

    // TODO: add peer list from file
    Brahms brahms =
        new Brahms(initialList, nseClient, pullClient, pushReceiver,
            pushSender);

    QueryServer queryServer = new QueryServer(TcpServer.newServer((11001)), brahms);
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

  private static PushReceiver initPushReceiver() {

    PublishSubject<Peer> pushReceivingSocket = PublishSubject.create();
    PublishSubject<Peer> gossipReceivingSocket = PublishSubject.create();

    TcpServer.newServer(11003)
        .enableWireLogging(LogLevel.DEBUG)
        .start(
            connection ->
                connection.writeBytesAndFlushOnEach(connection.getInput()
                    .doOnNext(byteBuf -> log.info("PULL REQUEST local view  received"))
                    .map(byteBuf -> {
                      Peer peer = SerializationUtils.fromBytes(byteBuf.array());
                      return peer;
                    })
                    .doOnNext(peer -> pushReceivingSocket.onNext(peer))
                    .map(peer -> "ok".getBytes())
                )
        );

    return new PushReceiver(gossipReceivingSocket, pushReceivingSocket);
  }
}
