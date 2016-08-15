package de.tum.group34.push;

import de.tum.group34.RxTcpClientFactory;
import de.tum.group34.model.Peer;
import de.tum.group34.protocol.Message;
import de.tum.group34.protocol.MessageParserException;
import de.tum.group34.protocol.gossip.ApiMessage;
import de.tum.group34.protocol.gossip.NotificationMessage;
import de.tum.group34.protocol.gossip.NotifyMessage;
import de.tum.group34.protocol.gossip.ValidationMessage;
import de.tum.group34.pull.RandomData;
import de.tum.group34.serialization.SerializationUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author Hannes Dorfmann
 */
public class PushReceiverTest {

  @Test
  @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
  public void registerToGossipAndDeliverNotification() {
    int port = 7728;

    PushReceiver receiver = new PushReceiver(TcpServer.newServer(3030), 1, TimeUnit.SECONDS);
    List<Message> serverReceivedMessages = new ArrayList<>();
    List<Peer> receivedGossipPeers = new ArrayList<>();

    Peer pushingPeer = RandomData.getPeer();
    //System.out.println("Peer sent: " + pushingPeer.getIpAddress().toString());

    int msgId = 123;
    NotificationMessage notificationMessage = new NotificationMessage(msgId,
        de.tum.group34.serialization.Message.GOSSIP_PUSH, SerializationUtils.toBytes(pushingPeer));
    ByteBuffer notificationMessageBuffer = ByteBuffer.allocate(notificationMessage.getSize());
    notificationMessage.send(notificationMessageBuffer);
    byte[] notificationMessageBytes = notificationMessageBuffer.array();

    //byte[] notificationMessageBytes = MessageParser.buildGossipAnnouncePush(pushingPeer, 500).array();

    TcpServer<ByteBuf, ByteBuf> server = TcpServer.newServer(port)
        .enableWireLogging("PushReceiverTest", LogLevel.DEBUG)
        .start(connection -> connection.getInput()
            .map(byteBuf -> {

              // System.out.println("\nServer rcv dataType: " + MessageParser.unsignedIntFromShort(byteBuf.getShort(6)));
              //System.out.println("\nServer rcv msgId: " + MessageParser.unsignedIntFromShort(byteBuf.getShort(4)));
              // System.out.println("Server rcv messageType: " + MessageParser.unsignedIntFromShort(byteBuf.getShort(2)));

              if (serverReceivedMessages.isEmpty()) {
                return NotifyMessage.parse(byteBuf.nioBuffer());
              }
              if (serverReceivedMessages.size() == 1) {
                return ValidationMessage.parse(byteBuf.nioBuffer());
              }

              throw new MessageParserException("Unexpected message type");
            })
            .doOnNext(System.out::println)
            .doOnNext(serverReceivedMessages::add)
            .flatMap(new Func1<ApiMessage, Observable<Void>>() {
              @Override public Observable<Void> call(ApiMessage s) {
                if (serverReceivedMessages.size() == 1) {
                  return connection.writeBytes(
                      Observable.just(notificationMessageBytes));
                } else {
                  return Observable.empty();
                }
              }
            })
        );

    receiver.registerToGossip(new InetSocketAddress("127.0.0.1", port))
        .doOnNext(byteBuf -> System.out.println("Tcp Client received an answer"))
        .subscribe(o -> {
              server.shutdown();
            },
            t -> {
              server.shutdown();
              t.printStackTrace();
              Assert.fail("Exception thrown");
            });

    receiver.gossipSocket()
        .doOnNext(peers -> System.out.println("Gossip notified us about peers " + peers))
        .take(1)
        .subscribe(peers -> {
              receivedGossipPeers.addAll(peers);
              server.shutdown();
            },
            t -> {
              server.shutdown();
              t.printStackTrace();
              Assert.fail("Exception thrown");
            });

    server.awaitShutdown();

    Assert.assertEquals(1, receivedGossipPeers.size());
    Assert.assertEquals(Arrays.asList(pushingPeer), receivedGossipPeers);

    Assert.assertEquals(2, serverReceivedMessages.size());

    NotifyMessage registerForNotificationsMessage = (NotifyMessage) serverReceivedMessages.get(0);
    ValidationMessage validationMessage = (ValidationMessage) serverReceivedMessages.get(1);
    Assert.assertEquals(de.tum.group34.serialization.Message.GOSSIP_PUSH,
        registerForNotificationsMessage.getDatatype());
    Assert.assertTrue(validationMessage.isValid());
  }

  @Test
  @Ignore
  @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
  public void registerToGossipButGossipDeliversInvalidMessages() {
    int port = 7729;

    PushReceiver receiver = new PushReceiver(TcpServer.newServer(3031), 1, TimeUnit.SECONDS);
    List<Message> serverReceivedMessages = new ArrayList<>();
    List<Peer> receivedGossipPeers = new ArrayList<>();

    TcpServer<ByteBuf, ByteBuf> server = TcpServer.newServer(port)
        .enableWireLogging("PushReceiverTest", LogLevel.DEBUG)
        .start(connection -> connection.getInput()
            .map(byteBuf -> {
              if (serverReceivedMessages.isEmpty()) {
                return NotifyMessage.parse(byteBuf.nioBuffer());
              }
              if (serverReceivedMessages.size() == 1) {
                return ValidationMessage.parse(byteBuf.nioBuffer());
              }

              throw new MessageParserException("Unexpected message type");
            })
            .doOnNext(System.out::println)
            .doOnNext(serverReceivedMessages::add)
            .flatMap(new Func1<ApiMessage, Observable<Void>>() {
              @Override public Observable<Void> call(ApiMessage s) {
                if (serverReceivedMessages.size() == 1) {
                  return connection.writeString(
                      Observable.just("Sending an invalid message to client"));
                } else {
                  return Observable.empty();
                }
              }
            })
        );

    receiver.registerToGossip(new InetSocketAddress("127.0.0.1", port))
        .doOnNext(byteBuf -> System.out.println("Tcp Client received an answer"))
        .subscribe(o -> {
              server.shutdown();
            },
            t -> {
              server.shutdown();
              t.printStackTrace();
              Assert.fail("Exception thrown");
            });

    receiver.gossipSocket()
        .doOnNext(peers -> System.out.println("Gossip notified us about peers " + peers))
        .take(1)
        .subscribe(peers -> {
              receivedGossipPeers.addAll(peers);
              server.shutdown();
            },
            t -> {
              server.shutdown();
              t.printStackTrace();
              Assert.fail("Exception thrown");
            });

    server.awaitShutdown();

    Assert.assertTrue(receivedGossipPeers.isEmpty());
    Assert.assertEquals(2, serverReceivedMessages.size());
    NotifyMessage registerForNotificationsMessage = (NotifyMessage) serverReceivedMessages.get(0);
    ValidationMessage validationMessage = (ValidationMessage) serverReceivedMessages.get(1);

    Assert.assertFalse(validationMessage.isValid());
    Assert.assertEquals(de.tum.group34.serialization.Message.GOSSIP_PUSH,
        registerForNotificationsMessage.getDatatype());
  }

  @Test
  public void receivePush() {

    int port = 3040;

    List<Peer> peerList = new ArrayList<>();

    Peer ownPeer = new Peer(new InetSocketAddress("127.0.0.1", port));
    ownPeer.setHostkey(RandomData.getHostKey());

    Peer receivingPeer = new Peer(new InetSocketAddress("127.0.0.1", port));
    receivingPeer.setPushServerPort(port);
    PushSender sender = new PushSender(ownPeer, new RxTcpClientFactory("Push-Sender-Test"));

    PushReceiver receiver = new PushReceiver(TcpServer.newServer(port), 1, TimeUnit.SECONDS);

    receiver.getPushList()
        .observeOn(Schedulers.newThread())
        .subscribeOn(Schedulers.newThread())
        .subscribe(
            peers -> {
              peerList.addAll(peers);
              receiver.shutdown();
            },
            throwable -> receiver.shutdown()
        );

    sender.sendMyId(Collections.singletonList(receivingPeer)).subscribe();

    receiver.awaitShutdown();

    Assert.assertEquals(1, peerList.size());
    Assert.assertEquals(Collections.singletonList(ownPeer), peerList);
  }
}
