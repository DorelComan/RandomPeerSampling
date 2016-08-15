package de.tum.group34.push;

import de.tum.group34.Brahms;
import de.tum.group34.ByteBufAggregatorOperator;
import de.tum.group34.ExponentialBackoff;
import de.tum.group34.model.Peer;
import de.tum.group34.model.PeerSharingMessage;
import de.tum.group34.serialization.MessageParser;
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
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * This class is responsible to receive Peers, either over gossip or directly pushed by {@link
 * Brahms}
 */
public class PushReceiver {

  private static final String LOG_TAG = PushReceiver.class.getName();
  private static final Logger log = Logger.getLogger(LOG_TAG);

  private final PublishSubject<Peer> pushReceivingSocketBridge = PublishSubject.create();
  private final PublishSubject<Peer> gossipSocketBridge = PublishSubject.create();
  private final Observable<List<Peer>> gossipSocket;
  private final Observable<List<Peer>> pushReceivingSocket;

  private final TcpServer<ByteBuf, ByteBuf> pushReceivingServerSocket;

  public PushReceiver(TcpServer<ByteBuf, ByteBuf> pushReceivingServerSocket, long timeInterval,
      TimeUnit timeUnit) {

    gossipSocket = gossipSocketBridge.buffer(timeInterval, timeUnit).onBackpressureDrop();
    pushReceivingSocket =
        pushReceivingSocketBridge.buffer(timeInterval, timeUnit).onBackpressureDrop();

    this.pushReceivingServerSocket = pushReceivingServerSocket.
        enableWireLogging(LOG_TAG, LogLevel.DEBUG)
        .start(
            connection ->
                connection.getInput()
                    .doOnNext(byteBuf -> log.info("Push Responding Socket received a Message"))
                    .lift(ByteBufAggregatorOperator.create())
                    .map(SerializationUtils::<Peer>fromByteArrays)
                    .doOnNext(pushReceivingSocketBridge::onNext)
                    .map(peer -> null)
        );
  }

  public Observable<List<Peer>> gossipSocket() {
    return gossipSocket;
  }

  /**
   * Get the merged (gossip socket and push server socket) list of "pushlist" for Brahms
   */
  public Observable<List<Peer>> getPushList() {
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
  public Observable<Void> registerToGossip(SocketAddress gossipSocketAddress) {

    // TODO exponential backoff

    return TcpClient.newClient(gossipSocketAddress)
        .enableWireLogging(LOG_TAG, LogLevel.DEBUG)
        .createConnectionRequest()
        .retryWhen(ExponentialBackoff.create(10, 3, TimeUnit.SECONDS))
        .flatMap(connection ->
            connection.writeBytes(
                Observable.just(MessageParser.buildRegisterForNotificationsMessages().array()))
                .cast(ByteBuf.class)
                .concatWith(connection.getInput())
                .lift(ByteBufAggregatorOperator.create())
                .map(SerializationUtils::byteArrayListToByteBuf)
                .flatMap(new Func1<ByteBuf, Observable<PeerSharingMessage>>() {
                  @Override public Observable<PeerSharingMessage> call(ByteBuf byteBuf) {

                    //todo: take out
                   /* System.out.println(
                        "\nClient rcv MessageType : " + MessageParser.unsignedIntFromShort(
                            byteBuf.getShort(2)));
                    System.out.println(
                        "Client rcv DataType received: " + MessageParser.unsignedIntFromShort(
                            byteBuf.getShort(6)));
                     System.out.println("Client rcv Peer received: " + byteBuf.getShort(0)); */

                    return Observable.fromCallable(
                        () -> MessageParser.buildPeerFromGossipPush(byteBuf))
                        .onErrorResumeNext(throwable -> Observable.just((PeerSharingMessage) null));
                  }
                })
                .doOnNext(peerSharingMessage -> {
                  if (peerSharingMessage != null) {
                    gossipSocketBridge.onNext(peerSharingMessage.getPeer());
                  }
                })
                .flatMap(peerSharingMessage -> connection.writeBytes(
                    Observable.fromCallable(() -> {
                      if (peerSharingMessage != null) {
                        return MessageParser.buildGossipPushValidationResponse(
                            peerSharingMessage.getMessageId(), true);
                      } else {
                        return MessageParser.buildGossipPushValidationResponse(0, false);
                      }
                    }).map(byteBuf -> byteBuf.array())))

        );
  }

  public void awaitShutdown() {
    pushReceivingServerSocket.awaitShutdown();
  }

  public void shutdown() {
    pushReceivingServerSocket.shutdown();
  }
}
