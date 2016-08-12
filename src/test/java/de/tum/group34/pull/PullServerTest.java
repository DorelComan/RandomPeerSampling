package de.tum.group34.pull;

import de.tum.group34.Brahms;
import de.tum.group34.model.Peer;
import de.tum.group34.serialization.SerializationUtils;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import org.junit.*;
import org.mockito.Mockito;
import rx.Observable;

public class PullServerTest {

  static class ResultHolder {
    List<Peer> result;
  }

  @Test
  public void respondWitLocalView() throws IOException {

    int port = 7726;

    Brahms brahms = Mockito.mock(Brahms.class);
    List<Peer> peerList = MockPeers.getPeerList(16);
    Mockito.when(brahms.getLocalView()).thenReturn(peerList);

    PullServer pullServer = new PullServer(brahms, TcpServer.newServer(port));

    ResultHolder resultHolder = new ResultHolder();

    TcpClient.newClient(new InetSocketAddress("127.0.0.1", port))
        .createConnectionRequest()
        .flatMap(connection ->
            connection.writeString(Observable.just("Hello"))
                .cast(ByteBuf.class)
                .concatWith(connection.getInput())
        )
        .take(1)
        .doOnNext(byteBuf -> System.out.println("Tcp Client received an answer"))
        .map(byteBuf -> {
          List<Peer> peers = SerializationUtils.fromByteBuf(byteBuf);
          return peers;
        })
        .subscribe(o -> {
              resultHolder.result = o;
              pullServer.shutDown();
            },
            t -> {
              pullServer.shutDown();
              t.printStackTrace();
              Assert.fail("Exception thrown");
            });

    pullServer.awaitShutdown();

    Assert.assertEquals(peerList, resultHolder.result);
  }
}
