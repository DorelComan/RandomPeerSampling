package de.tum.group34.mock;

import de.tum.group34.nse.NseClient;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.util.concurrent.TimeUnit;

/**
 * @author Hannes Dorfmann
 */
public class NseClientTest {

  public static void main(String[] args) {

    NseClient nseClient = new NseClient(TcpClient.newClient("127.0.0.1", MockNseModule.PORT), 1,
        TimeUnit.SECONDS);

    nseClient.getNetworkSize()
        .toBlocking()
        .forEach(networkSize -> System.out.println("Network Size " + networkSize));
  }
}

