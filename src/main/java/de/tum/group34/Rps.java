package de.tum.group34;

import de.tum.group34.gossip.GossipSender;
import de.tum.group34.model.Peer;
import de.tum.group34.nse.NseClient;
import de.tum.group34.pull.PullClient;
import de.tum.group34.pull.PullServer;
import de.tum.group34.push.PushReceiver;
import de.tum.group34.push.PushSender;
import de.tum.group34.query.QueryServer;
import de.tum.group34.serialization.FileParser;
import de.tum.group34.test.MockPullClient;
import de.tum.group34.test.RandomData;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.mockito.Mockito;

/**
 * Execute the main with first argument the path of the configutation file
 *
 * @author Hannes Dorfmann
 */
public class Rps {

  private static final Logger log = Logger.getLogger(Rps.class.getName());

  private static final int PULL_SERVER_PORT = 5222;

  // Variables for the Gossip
  private static final int DELAY_GOSSIP_SENDER = 40;
  private static final TimeUnit UNIT_TIME_GOSSIP_SENDER = TimeUnit.SECONDS;
  private static final int TTL_GOSSIP_SENDER = 20;

  public static void main(final String[] args)
      throws InterruptedException, IOException, ConfigurationException, URISyntaxException {

    if (args.length != 1) {
      System.out.println("Usage of Rps: 'java Rps [path of config_file.conf]'");
      return;
    }

    FileParser fileParser = new FileParser(args[0]);
    Peer ownIdentity = new Peer(); // TODO: should set our address
    ownIdentity.setPullServerPort(PULL_SERVER_PORT);
    ownIdentity.setPushServerPort(fileParser.getPushServerPort());
    ownIdentity.setHostkey(fileParser.getHostkey());

    // PullClient pullClient = new PullClient(); // todo: commented for the tests
    PullClient pullClient =
        new MockPullClient(); //TODO: to be taken down along with next row after test
    ((MockPullClient) pullClient).setSize(10);

   /* PushSender pushSender =
        new PushSender(ownIdentity, new RxTcpClientFactory(PushSender.class.getName()),
            PUSH_SERVER_PORT); */ //todo: commented for the tests

    PushSender pushSender = Mockito.mock(PushSender.class);
    Mockito.when(pushSender.sendMyId(Mockito.any()))
        .thenReturn(rx.Observable.just(RandomData.getPeerList(5)));

    PushReceiver pushReceiver =
        new PushReceiver(TcpServer.newServer(fileParser.getPushServerPort()), 10, TimeUnit.SECONDS);

    pushReceiver.registerToGossip(fileParser.getGossipAddress())
        .subscribe(aVoid -> {
            },
            throwable -> {
              throwable.printStackTrace();
              System.out.println(
                  "Shutting down RPS Module because no connection to Gossip Module is available");
              System.exit(1);
            });

    NseClient nseClient =
        new NseClient(new RxTcpClientFactory("NseClient"), fileParser.getNseAddress(),
            30, TimeUnit.SECONDS);

    GossipSender gossipSender =
        new GossipSender(ownIdentity, TcpClient.newClient(fileParser.getGossipAddress()));
    gossipSender.sendOwnPeerPeriodically(DELAY_GOSSIP_SENDER, UNIT_TIME_GOSSIP_SENDER,
        TTL_GOSSIP_SENDER).subscribe();

    //List<Peer> initialList = pushReceiver.gossipSocket().toBlocking().first(); TODO: commented for the tests
    List<Peer> initialList = RandomData.getPeerList(1);

    Brahms brahms =
        new Brahms(initialList, nseClient, pullClient, pushReceiver,
            pushSender, new RxTcpClientFactory("Brahms"));

    new Thread(brahms::start).start();

    QueryServer queryServer =
        new QueryServer(TcpServer.newServer(fileParser.getQueryServerPort()), brahms);
    PullServer pullServer =
        new PullServer(brahms, TcpServer.newServer(PULL_SERVER_PORT));

    queryServer.awaitShutdown();
    pullServer.awaitShutdown();
    pushReceiver.awaitShutdown();
  }
}
