package de.tum.group34;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.channel.Connection;
import io.reactivex.netty.client.ConnectionRequest;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.util.concurrent.TimeUnit;
import module.Peer;
import org.junit.Test;
import org.mockito.Mockito;
import rx.Observable;

/**
 * @author Hannes Dorfmann
 */
public class GossipSenderTest {

  @Test
  public void sendOwnPeerPeriodically() throws InterruptedException {

    int ttl = 20;
    Peer ownIdentity = new Peer();
    TcpClient<ByteBuf, ByteBuf> tcpClient = Mockito.mock(TcpClient.class);
    Connection<ByteBuf, ByteBuf> connection = Mockito.spy(new MockWriteAndFlushOnEachConnection());
    ConnectionRequest<ByteBuf, ByteBuf> connectionRequest = new MockConnectionRequest(connection);

    Mockito.when(tcpClient.createConnectionRequest()).thenReturn(connectionRequest);

    GossipSender sender = new GossipSender(ownIdentity, tcpClient);
    sender.sendOwnPeerPeriodically(500, TimeUnit.MILLISECONDS, ttl).toBlocking().first();

    Mockito
        .verify(connection, Mockito.only())
        .writeAndFlushOnEach(
            Mockito.any(Observable.class));
  }
}
