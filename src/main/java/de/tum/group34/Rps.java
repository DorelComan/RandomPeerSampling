package de.tum.group34;

import de.tum.group34.nse.NseClient;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.protocol.tcp.server.TcpServer;

/**
 * @author Hannes Dorfmann
 */
public class Rps {

  public static void main(final String[] args) {

    // TODO read config

    QueryServer queryServer = new QueryServer(TcpServer.newServer((11001)));
    PullLocalViewServer pullLocalViewServer = new PullLocalViewServer(TcpServer.newServer(11002));

    NseClient nseClient = new NseClient(TcpClient.newClient("127.0.0.1", 9899));
    nseClient.startQueryingPeriodically();

    queryServer.awaitShutdown();
    pullLocalViewServer.awaitShutdown();
  }
}
