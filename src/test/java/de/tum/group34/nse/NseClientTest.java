package de.tum.group34.nse;

import de.tum.group34.RxTcpClientFactory;
import de.tum.group34.protocol.Message;
import de.tum.group34.protocol.nse.EstimateMessage;
import de.tum.group34.protocol.nse.QueryMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import rx.Observable;
import rx.functions.Func1;

/**
 * @author Hannes Dorfmann
 */
public class NseClientTest {

  @Test
  @Ignore
  public void queryPeriodically() throws InterruptedException {

    int port = 8883;
    List<Message> serverReceivedMessages = new ArrayList<>();

    EstimateMessage firstResponse = new EstimateMessage(20, 30);
    ByteBuffer firstByteBuffer = ByteBuffer.allocate(firstResponse.getSize());
    firstResponse.send(firstByteBuffer);
    EstimateMessage secondResponse = new EstimateMessage(80, 100);
    ByteBuffer secondResponseBuffer = ByteBuffer.allocate(firstResponse.getSize());
    secondResponse.send(secondResponseBuffer);

    TcpServer<ByteBuf, ByteBuf> server = TcpServer.newServer(port)
        .enableWireLogging("NseServer", LogLevel.DEBUG)
        .start(connection -> connection.getInput()
            .map(byteBuf -> QueryMessage.parse(byteBuf.nioBuffer()))
            .doOnNext(System.out::println)
            .doOnNext(serverReceivedMessages::add)
            .flatMap(new Func1<Message, Observable<Void>>() {
              @Override public Observable<Void> call(Message msg) {

                if (serverReceivedMessages.size() == 0) {
                  return connection.writeBytes(
                      Observable.just(firstByteBuffer.array()));
                }

                if (serverReceivedMessages.size() == 1) {
                  return connection.writeBytes(
                      Observable.just(secondResponseBuffer.array()));
                }
                return Observable.error(new RuntimeException("Oops, unexpected message"));
              }
            })
        );

    NseClient nseClient = new NseClient(new RxTcpClientFactory("NseClientTest"),
        new InetSocketAddress("127.0.0.1", port), 500, TimeUnit.MILLISECONDS);

    List<Integer> networkSizes = new ArrayList<>();
    nseClient.getNetworkSize().subscribe(size -> {
          networkSizes.add(size);
          server.shutdown();
        },
        t -> {
          t.printStackTrace();
          Assert.fail("Unexpected exception has been thrown");
          server.shutdown();
        });

    server.awaitShutdown();

    Assert.assertEquals(2, networkSizes.size());
    Assert.assertEquals(Arrays.asList(20, 80), networkSizes);
  }
}
