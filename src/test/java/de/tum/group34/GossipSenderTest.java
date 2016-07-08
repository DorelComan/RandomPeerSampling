package de.tum.group34;

import de.tum.group34.mock.MockTcpClient;
import java.util.concurrent.TimeUnit;
import module.Peer;
import org.junit.Test;

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

    tcpClient.assertMessagesSent(1);
    tcpClient.assertLastSentMessageEquals(MessageParser.buildGossipPush(ownIdentity, ttl));
  }
}
