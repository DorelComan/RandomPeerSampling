package de.tum.group34.gossip;

import de.tum.group34.serialization.MessageParser;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.client.ConnectionRequest;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import de.tum.group34.model.Peer;
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
  private ConnectionRequest<ByteBuf, ByteBuf> connectionRequest;

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
                .doOnNext(messageId -> log.info("Broadcasting my own identity"))
                .flatMap(interval -> connection.writeAndFlushOnEach(
                    Observable.just(MessageParser.buildGossipPush(ownIdentity, ttl))))
        );
  }
}
