/**
 * @author Hannes Dorfmann
 */
public class Rps {

  public static void main(final String[] args) {

    // TODO read config

    QueryServer queryServer = new QueryServer(11001);
    PullServer pullServer = new PullServer(11002);

    queryServer.start();
    pullServer.start();

    queryServer.awaitShutdown();
    pullServer.awaitShutdown();
  }
}
