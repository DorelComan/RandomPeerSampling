package de.tum.group34.gossip;

import de.tum.group34.model.Peer;
import de.tum.group34.serialization.MessageParser;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import rx.Observable;

/**
 * This class is responsible to send a push message
 *
 * @author Hannes Dorfmann
 */
public class GossipSender {

  private static final Logger log = Logger.getLogger(GossipSender.class.getName());

  private TcpClient<ByteBuf, ByteBuf> client;
  private Peer ownIdentity;

  public GossipSender(Peer ownIdentity, TcpClient<ByteBuf, ByteBuf> client) {
    this.ownIdentity = ownIdentity;
    this.client = client;
  }

  /**
   * Starts sending periodically
   */
  public Observable<Void> sendOwnPeerPeriodically(long time, TimeUnit unit, int ttl) {
    return this.client.createConnectionRequest()
        .flatMap(connection ->
            Observable.interval(0, time, unit)
                .onBackpressureDrop()
                .doOnNext(messageId -> log.info("Staring broadcasting my own identity"))
                .flatMap(interval -> connection.writeBytes(
                    Observable.just(MessageParser.buildGossipAnnouncePush(ownIdentity, ttl).array()))
                    .doOnNext(aVoid -> log.info("Broadcastet successfully my own identity")
                    )
                    .onBackpressureDrop()

                )
        );
  }
}
