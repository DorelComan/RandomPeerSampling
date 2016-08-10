package de.tum.group34.nse;

import de.tum.group34.mock.MockTcpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import rx.observers.TestSubscriber;

/**
 * @author Hannes Dorfmann
 */
public class NseClientTest {

  @Test
  public void queryPeriodically() throws InterruptedException {

    ByteBuf answer1 = Unpooled.buffer();
    answer1.setShort(0, 23);

    MockTcpClient tcpClient = MockTcpClient.create();
    NseClient nseClient = new NseClient(tcpClient, 200, TimeUnit.MILLISECONDS);
    tcpClient.deliverIncomingMessage(answer1);

    TestSubscriber subscriber = new TestSubscriber();

    nseClient.getNetworkSize().subscribe(subscriber);

    Thread.sleep(500);

    subscriber.assertNoErrors();
    subscriber.assertNotCompleted();
    subscriber.assertValueCount(1);
    subscriber.assertValues(23);
  }
}
