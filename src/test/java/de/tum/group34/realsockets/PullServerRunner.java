package de.tum.group34.realsockets;

import de.tum.group34.Brahms;
import de.tum.group34.model.Peer;
import de.tum.group34.pull.MockPeers;
import de.tum.group34.pull.PullServer;
import de.tum.group34.serialization.SerializationUtils;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import org.mockito.Mockito;
import rx.Observable;

public class PullServerRunner {

  static class ResultHolder {
    Object result;
  }

  public static void main(String[] args) throws IOException {

    int port = 7726;

    Brahms brahms = Mockito.mock(Brahms.class);
    List<Peer> peerList = MockPeers.getPeerList(20);
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
        .map(SerializationUtils::fromByteBuf)
        .subscribe(o -> {
              resultHolder.result = o;
              pullServer.shutDown();
            },
            t -> {
              pullServer.shutDown();
              t.printStackTrace();
            });

    pullServer.awaitShutdown();

    System.out.println("Here we are");
  }
}
