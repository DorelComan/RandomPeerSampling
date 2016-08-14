package de.tum.group34.query;

import de.tum.group34.Brahms;
import de.tum.group34.model.Peer;
import de.tum.group34.pull.RandomData;
import de.tum.group34.serialization.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.junit.*;
import org.mockito.Mockito;
import rx.Observable;

/**
 * @author Hannes Dorfmann
 */
public class QueryServerTest {

  private static class QueryResult {
    ByteBuf byteBuf;
  }

  @Test
  public void query() throws UnknownHostException {

    Peer peer = RandomData.getPeer();
    Brahms brahms = Mockito.mock(Brahms.class);
    Mockito.when(brahms.getRandomPeerObservable()).thenReturn(Observable.just(peer));

    QueryResult result = new QueryResult();

    int port = 7678;
    TcpServer<ByteBuf, ByteBuf> tcpServer = TcpServer.newServer(port);
    QueryServer server = new QueryServer(tcpServer, brahms);

    QueryClient.query("127.0.0.1", port)
        .take(1)
        .doOnNext(byteBuf -> result.byteBuf = byteBuf)
        .subscribe(
            b -> server.shutdown(),
            t -> server.shutdown()
        );

    server.awaitShutdown();

    ByteBuf byteBuf = result.byteBuf;

    ByteBuf addBuf = Unpooled.buffer(4);
    byteBuf.getBytes(8, addBuf, 4);
    InetSocketAddress receivedAddress =
        new InetSocketAddress(InetAddress.getByAddress(addBuf.array()), byteBuf.getShort(4));

    int msgSize = byteBuf.getShort(0);
    int msgType = byteBuf.getShort(2);

    byte[] hostKey = new byte[msgSize - 12];
    byteBuf.getBytes(12, hostKey);

    Assert.assertNotNull(result.byteBuf);
    Assert.assertEquals(peer.getIpAddress(), receivedAddress);
    Assert.assertEquals(Message.TYPE_RPS_PEER, msgType);
    Assert.assertArrayEquals(peer.getHostkey(), hostKey);
  }
}