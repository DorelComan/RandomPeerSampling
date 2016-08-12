package de.tum.group34.nse;

import de.tum.group34.mock.MockTcpClient;
import de.tum.group34.mock.MockTcpClientFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.*;

/**
 * @author Hannes Dorfmann
 */
public class NseClientTest {

  @Test
  public void queryPeriodically() throws InterruptedException {

    ByteBuf answer1 = Unpooled.buffer();
    answer1.setShort(0, 23);

    ByteBuf answer2 = Unpooled.buffer();
    answer2.setShort(1, 42);

    MockTcpClient tcpClient1 = MockTcpClient.create();
    MockTcpClient tcpClient2 = MockTcpClient.create();

    tcpClient1.deliverIncomingMessage(answer1);
    tcpClient2.deliverIncomingMessage(answer1);

    NseClient nseClient = new NseClient(MockTcpClientFactory.withClients(tcpClient1, tcpClient2),
        new InetSocketAddress("192.168.0.20", 4123), 100, TimeUnit.MILLISECONDS);

    List<Integer> networkSizes = new ArrayList<>();
    nseClient.getNetworkSize().take(2).toBlocking().forEach(networkSizes::add);

    Assert.assertEquals(2, networkSizes.size());
    Assert.assertEquals(Arrays.asList(23, 42), networkSizes);
  }
}
