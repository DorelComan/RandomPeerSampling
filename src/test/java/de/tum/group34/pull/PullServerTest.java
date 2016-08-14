package de.tum.group34.pull;

import de.tum.group34.Brahms;
import de.tum.group34.ByteBufAggregatorOperator;
import de.tum.group34.model.Peer;
import de.tum.group34.serialization.MessageParser;
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
    List<Peer> peerList = RandomData.getPeerList(15);
    Mockito.when(brahms.getLocalView()).thenReturn(peerList);

    PullServer pullServer = new PullServer(brahms, TcpServer.newServer(port));

    ResultHolder resultHolder = new ResultHolder();

    TcpClient.newClient(new InetSocketAddress("127.0.0.1", port))
        .createConnectionRequest()
        .flatMap(connection -> connection.writeBytes(
            Observable.just(MessageParser.getPullLocalView().array()))
            .cast(ByteBuf.class)
            .concatWith(connection.getInput())
            .lift(ByteBufAggregatorOperator.create())
            .map(byteBufs -> {
              List<Peer> peers = SerializationUtils.fromByteArrays(byteBufs);
              return peers;
            }))
        .subscribe(o -> {
              resultHolder.result = o;
              pullServer.shutdown();
            },
            t -> {
              t.printStackTrace();
              pullServer.shutdown();
            });

    pullServer.awaitShutdown();

    Assert.assertEquals(peerList, resultHolder.result);
  }
}
