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
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Hannes Dorfmann
 */
public class Rps {

  private static final Logger log = Logger.getLogger(Rps.class.getName());

  public static void main(final String[] args) throws InterruptedException, IOException, ConfigurationException, URISyntaxException {


    FileParser fileParser =  new FileParser("Insert file path");

    Peer ownIdentity = new Peer();
    ownIdentity.setHostkey(fileParser.getHostkey());

    PullClient pullClient = new PullClient();
    PushSender pushSender = new PushSender();
    PushReceiver pushReceiver = initPushReceiver();

    NseClient nseClient =
        new NseClient(new RxTcpClientFactory("NseClient"), fileParser.getNseAddress(),
            30, TimeUnit.SECONDS);

    GossipSender gossipSender =
        new GossipSender(ownIdentity, TcpClient.newClient(fileParser.getGossipAddress()));
    gossipSender.sendOwnPeerPeriodically(30, TimeUnit.MINUTES, 20).subscribe();

    List<Peer> initialList = pushReceiver.gossipSocket().toBlocking().first();

    // TODO: add peer list from file? I guess Nein
    Brahms brahms =
        new Brahms(initialList, nseClient, pullClient, pushReceiver,
            pushSender, new RxTcpClientFactory("Brahms"));

    new Thread(()->{
      try {
        brahms.start();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }).start();

    QueryServer queryServer = new QueryServer(TcpServer.newServer(fileParser.getQueryServerPort()), brahms);
    PullServer pullServer = new PullServer(brahms, TcpServer.newServer(fileParser.getPullServerPort()));

    queryServer.awaitShutdown();
    pullServer.awaitShutdown();
    pushReceiver.awaitShutdown();
  }

  private static PushReceiver initPushReceiver() {
    return new PushReceiver(
        InetSocketAddress.createUnresolved("127.0.0.1", 11003),
        TcpServer.newServer(11004),
        TcpServer.newServer(11005));
  }
}
