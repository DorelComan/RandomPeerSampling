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
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * Execute the main with first argument the path of the configutation file
 *
 * @author Hannes Dorfmann
 */
public class Rps {

  private static final Logger log = Logger.getLogger(Rps.class.getName());

  // Variables for the Gossip
  private static final long DELAY_GOSSIP_SENDER = 40;
  private static final TimeUnit TIME_UNIT_GOSSIP_SENDER = TimeUnit.SECONDS;
  private static final int TTL_GOSSIP_SENDER = 20;

  // Variables for NseClient
  private static final long DELAY_NSE_QUERY = 30;
  private static final TimeUnit TIME_UNIT_NSE_DELAY = TimeUnit.SECONDS;

  // Variables for PushReceiver
  private static final long DELAY_PUSH_RECEIVER = 10;
  private static final TimeUnit TIME_UNIT_DELAY_PUSH = TimeUnit.SECONDS;

  public static void main(final String[] args)
      throws InterruptedException, IOException, ConfigurationException, URISyntaxException {

    if (args.length != 1) {
      System.out.println("Usage of Rps: 'java Rps [path of config_file.conf]'");
      return;
    }

    FileParser fileParser = new FileParser(args[0]);
    Peer ownIdentity = new Peer(fileParser.getOnionAddress(), fileParser.getPushServerPort(),
        fileParser.getPullServerPort(), fileParser.getHostkey()); // TODO: should set our address

    PullClient pullClient = new PullClient();

    PushSender pushSender =
        new PushSender(ownIdentity, new RxTcpClientFactory(PushSender.class.getName()));

    PushReceiver pushReceiver =
        new PushReceiver(TcpServer.newServer(fileParser.getPushServerPort()), DELAY_PUSH_RECEIVER,
            TIME_UNIT_DELAY_PUSH);

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
            DELAY_NSE_QUERY, TIME_UNIT_NSE_DELAY);

    GossipSender gossipSender =
        new GossipSender(ownIdentity, TcpClient.newClient(fileParser.getGossipAddress()));
    gossipSender.sendOwnPeerPeriodically(DELAY_GOSSIP_SENDER, TIME_UNIT_GOSSIP_SENDER,
        TTL_GOSSIP_SENDER).subscribe();

    System.out.println("START");

    Brahms brahms =
        new Brahms(ownIdentity, nseClient, pullClient, pushReceiver,
            pushSender, new RxTcpClientFactory("Brahms"));

    QueryServer queryServer =
        new QueryServer(TcpServer.newServer(fileParser.getQueryServerPort()), brahms);
    PullServer pullServer =
        new PullServer(brahms, TcpServer.newServer(fileParser.getPullServerPort()));

    List<Peer> initialList = pushReceiver.gossipSocket()
        .filter(peers -> !peers.isEmpty())
        .toBlocking()
        .first();

    new Thread(() -> brahms.start(initialList)).start();

    queryServer.awaitShutdown();
    pullServer.awaitShutdown();
    pushReceiver.awaitShutdown();
  }
}
