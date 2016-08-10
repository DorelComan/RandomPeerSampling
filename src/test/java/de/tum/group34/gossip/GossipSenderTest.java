package de.tum.group34.gossip;

import de.tum.group34.mock.MockTcpClient;
import de.tum.group34.model.Peer;
import de.tum.group34.serialization.MessageParser;
import io.netty.buffer.ByteBuf;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import rx.Subscription;
import rx.observers.TestSubscriber;

import static de.tum.group34.serialization.MessageParser.buildGossipPush;

/**
 * @author Hannes Dorfmann
 */
public class GossipSenderTest {

  @Test
  public void sendOwnPeerPeriodically() throws InterruptedException {

    int ttl = 20;
    Peer ownIdentity = new Peer();
    MockTcpClient tcpClient = MockTcpClient.create();

    GossipSender sender = new GossipSender(ownIdentity, tcpClient);
    sender.sendOwnPeerPeriodically(500, TimeUnit.MILLISECONDS, ttl).toBlocking().first();

    tcpClient.assertMessagesSent(1)
        .assertLastSentMessageEquals(buildGossipPush(ownIdentity, ttl));
  }

  @Test
  public void sendOwnPeerPeriodicallyMultipleTimes() throws InterruptedException {

    int ttl = 20;
    Peer ownIdentity = new Peer();
    MockTcpClient tcpClient = MockTcpClient.create();

    GossipSender sender = new GossipSender(ownIdentity, tcpClient);
    TestSubscriber<Void> subscriber = new TestSubscriber<>();

    Subscription sub =
        sender.sendOwnPeerPeriodically(250, TimeUnit.MILLISECONDS, ttl).subscribe(subscriber);

    Thread.sleep(650); // Assume 3 times sent in the mean time (at 0, 250, 500)
    sub.unsubscribe();

    ByteBuf ownPeerMessage = MessageParser.buildGossipPush(ownIdentity, ttl);
    tcpClient.assertMessagesSent(3)
        .assertMessagesSent(ownPeerMessage, ownPeerMessage, ownPeerMessage);
  }
}
