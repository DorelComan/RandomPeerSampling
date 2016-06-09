package de.tum.group34;

/**
 * @author Hannes Dorfmann
 */
public class Rps {

  public static void main(final String[] args) {

    // TODO read config

    QueryServer queryServer = new QueryServer(11001);
    PullLocalViewServer pullLocalViewServer = new PullLocalViewServer(11002);

    queryServer.start();
    pullLocalViewServer.start();

    queryServer.awaitShutdown();
    pullLocalViewServer.awaitShutdown();
  }
}
