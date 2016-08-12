package de.tum.group34.realsockets;

import de.tum.group34.TcpClientFactory;
import de.tum.group34.nse.NseClient;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author Hannes Dorfmann
 */
public class NseClientRunner {

  public static void main(String[] args) {

    NseClient nseClient =
        new de.tum.group34.nse.NseClient(new TcpClientFactory("NseClient"),
            new InetSocketAddress("127.0.0.1", NseServerRunner.PORT), 1,
            TimeUnit.SECONDS);

    nseClient.getNetworkSize()
        .toBlocking()
        .forEach(networkSize -> System.out.println("Network Size " + networkSize));
  }
}

