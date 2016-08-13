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
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * @author Hannes Dorfmann
 */
public class Rps {

  private static final Logger log = Logger.getLogger(Rps.class.getName());
  private static final int PULL_SERVER_PORT = 5222;
  private static final int PUSH_SERVER_PORT = 5223;

  public static void main(final String[] args)
      throws InterruptedException, IOException, ConfigurationException, URISyntaxException {

    if (args.length != 1) {
      System.out.println("Usage of Rps: 'java Rps [path of config_file.conf]'");
      return;
    }

    //
    // Config
    //
    int pushServerPort = 33234;

    FileParser fileParser = new FileParser(args[0]);

    Peer ownIdentity = new Peer();
    ownIdentity.setHostkey(fileParser.getHostkey());

    PullClient pullClient = new PullClient();
    PushSender pushSender =
        new PushSender(ownIdentity, new RxTcpClientFactory(PushSender.class.getName()),
            pushServerPort);

    PushReceiver pushReceiver =
        new PushReceiver(TcpServer.newServer(pushServerPort), 10, TimeUnit.SECONDS);

    pushReceiver.registerToGossip(InetSocketAddress.createUnresolved("127.0.0.1",
        fileParser.getGossipAddress().getPort())) //TODO: UNRESOLVED?
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
    gossipSender.sendOwnPeerPeriodically(30, TimeUnit.MINUTES, 20).subscribe();

    List<Peer> initialList = pushReceiver.gossipSocket().toBlocking().first();

    Brahms brahms =
        new Brahms(initialList, nseClient, pullClient, pushReceiver,
            pushSender, new RxTcpClientFactory("Brahms"));

    new Thread(() -> {
      try {
        brahms.start();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }).start();

    QueryServer queryServer =
        new QueryServer(TcpServer.newServer(fileParser.getQueryServerPort()), brahms);
    PullServer pullServer =
        new PullServer(brahms, TcpServer.newServer(PULL_SERVER_PORT));

    queryServer.awaitShutdown();
    pullServer.awaitShutdown();
    pushReceiver.awaitShutdown();
  }
}
