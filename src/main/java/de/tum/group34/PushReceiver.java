package de.tum.group34;

import de.tum.group34.serialization.SerializationUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import module.Peer;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * This class is responsible to receive Peers, either over gossip or directly pushed by {@link
 * Brahms}
 */
public class PushReceiver {

  private static final Logger log = Logger.getLogger(PushReceiver.class.getName());

  private final PublishSubject<Peer> pushReceivingSocketBridge = PublishSubject.create();
  private final PublishSubject<Peer> gossipSocketBridge = PublishSubject.create();
  private final Observable<List<Peer>> gossipSocket;
  private final Observable<List<Peer>> pushReceivingSocket;

  private final TcpServer<ByteBuf, ByteBuf> gossipServerSocket;
  private final TcpServer<ByteBuf, ByteBuf> pushReceivingServerSocket;

  public PushReceiver(
      SocketAddress gossipSocketAddress,
      TcpServer<ByteBuf, ByteBuf> gossipServerSocket,
      TcpServer<ByteBuf, ByteBuf> pushReceivingServerSocket) {

    gossipSocket = gossipSocketBridge.buffer(1, TimeUnit.MINUTES).onBackpressureDrop();
    pushReceivingSocket =
        pushReceivingSocketBridge.buffer(1, TimeUnit.MINUTES).onBackpressureDrop();

    this.gossipServerSocket = gossipServerSocket;
    this.pushReceivingServerSocket = pushReceivingServerSocket;

    registerToGossip(gossipSocketAddress).map(registerd ->

        gossipServerSocket.
            enableWireLogging(LogLevel.DEBUG)
            .start(
                connection ->
                    connection.writeBytesAndFlushOnEach(connection.getInput()
                        .doOnNext(byteBuf -> log.info("Gossip (Push) Message Received"))
                        .map(byteBuf -> {
                          Peer peer = SerializationUtils.fromBytes(byteBuf.array());
                          return peer;
                        })
                        .doOnNext(peer -> pushReceivingSocketBridge.onNext(peer))
                        .map(peer -> new byte[0]) // TODO what should the answer be?
                    )
            )
    ).subscribe();

    pushReceivingServerSocket.
        enableWireLogging(LogLevel.DEBUG)
        .start(
            connection ->
                connection.writeBytesAndFlushOnEach(connection.getInput()
                    .doOnNext(byteBuf -> log.info("Push Responding Socket received a Message"))
                    .map(byteBuf -> {
                      Peer peer = SerializationUtils.fromBytes(byteBuf.array());
                      return peer;
                    })
                    .doOnNext(peer -> pushReceivingSocketBridge.onNext(peer))
                    .map(peer -> new byte[0]) // TODO what should the answer be?
                )
        );
  }

  public Observable<List<Peer>> gossipSocket() {
    return gossipSocket;
  }

  /**
   * Get the merged (gossip socket and push server socket) list of "pushlist" for Brahms
   */
  public Observable<ArrayList<Peer>> getPushList() {
    return Observable.combineLatest(gossipSocket, pushReceivingSocket,
        (gossipResonse, pushSocketResponse) -> {
          ArrayList<Peer> mergedPeers = new ArrayList<Peer>();
          mergedPeers.addAll(gossipResonse);
          mergedPeers.addAll(pushSocketResponse);
          // Ensure that a Peer is only represented once in the list
          return new ArrayList<Peer>(mergedPeers.stream().distinct().collect(Collectors.toList()));
        }
    );
  }

  /**
   * Registers to the gossip module to receive further norification of incoming PUSH announcements
   * of a gossip.
   */
  private Observable<Boolean> registerToGossip(SocketAddress gossipSocketAddress) {

    return TcpClient.newClient(gossipSocketAddress)
        .enableWireLogging(LogLevel.DEBUG)
        .createConnectionRequest()
        .flatMap(connection ->
            connection.writeBytes(Observable.just(MessageParser.getGossipNotifyForPush().array()))
        )
        .take(1)
        .map(byteBuf -> true);
  }

  public void awaitShutdown() {
    gossipServerSocket.awaitShutdown();
    pushReceivingServerSocket.awaitShutdown();
  }
}
