package de.tum.group34.gossip;

import de.tum.group34.RxTcpClientFactory;
import de.tum.group34.model.Peer;
import de.tum.group34.protocol.gossip.AnnounceMessage;
import de.tum.group34.pull.RandomData;
import de.tum.group34.serialization.Message;
import de.tum.group34.serialization.SerializationUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.*;

/**
 * @author Hannes Dorfmann
 */
public class GossipSenderTest {

  @Test
  public void sendOwnPeerPeriodicallyMultipleTimes() throws InterruptedException {

    List<AnnounceMessage> serverReceivedMessages = new ArrayList<>();

    int port = 4455;
    TcpServer<ByteBuf, ByteBuf> server = TcpServer.newServer(port)
        .enableWireLogging("gossip-test-server", LogLevel.DEBUG)
        .start(connection ->
            connection.getInput()
                .doOnNext(byteBuf -> System.out.println("Message received"))
                .map(incommingMsg -> AnnounceMessage.parse(incommingMsg.nioBuffer()))
                .doOnNext(serverReceivedMessages::add)
                .doOnNext(announceMessage -> System.out.println("Receivend "
                    + announceMessage
                    + " totalMsgs: "
                    + serverReceivedMessages.size()))
                .doOnNext(announceMessage -> {
                  if (serverReceivedMessages.size() == 3) {
                    connection.closeNow();
                  }
                })
                .map(announceMessage -> null)
        );

    int ttl = 20;
    Peer ownIdentity = new Peer();
    ownIdentity.setHostkey(RandomData.getHostKey());

    GossipSender sender = new GossipSender(ownIdentity,
        new RxTcpClientFactory("GossipSender").newClient(new InetSocketAddress("127.0.0.1", port)));

    sender.sendOwnPeerPeriodically(100, TimeUnit.MILLISECONDS, ttl)
        .doOnNext(aVoid -> System.out.println("onNext"))
        .take(3)
        .subscribe(aVoid -> {
              System.out.println("Here");
            },
            t -> {
              server.shutdown();
            },
            server::shutdown);

    server.awaitShutdown();
    Thread.sleep(1000);

    Assert.assertEquals(3, serverReceivedMessages.size());

    for (AnnounceMessage msg : serverReceivedMessages) {
      Assert.assertEquals(ttl, msg.getTtl());
      Assert.assertEquals(Message.GOSSIP_ANNOUNCE, msg.getType().getNumVal());
        Assert.assertEquals(Message.GOSSIP_PUSH, msg.getDatatype());
      Assert.assertEquals(ownIdentity,
          SerializationUtils.fromByteArrays(Arrays.asList(msg.getData())));
    }
  }
}
