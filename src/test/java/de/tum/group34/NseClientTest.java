package de.tum.group34;

import de.tum.group34.nse.NseClient;
import java.util.concurrent.TimeUnit;

/**
 * @author Hannes Dorfmann
 */
public class NseClientTest {

  public static void main(String[] args) {

    NseClient nseClient =
        new NseClient(new TcpClientFactory("127.0.0.1", NseServer.PORT, "NseClient"), 1,
            TimeUnit.SECONDS);

    nseClient.getNetworkSize()
        .toBlocking()
        .forEach(networkSize -> System.out.println("Network Size " + networkSize));
  }
}
