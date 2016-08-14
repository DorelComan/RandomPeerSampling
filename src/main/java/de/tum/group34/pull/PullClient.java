package de.tum.group34.pull;

import de.tum.group34.ByteBufAggregatorOperator;
import de.tum.group34.model.Peer;
import de.tum.group34.serialization.MessageParser;
import de.tum.group34.serialization.SerializationUtils;
import de.tum.group34.test.MockPullServer;
import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import rx.Observable;

/**
 * Responsible to execute PullRequests to all clients and to receive the answers
 */
public class PullClient {

  public Observable<ArrayList<Peer>> makePullRequests(List<Peer> peers) {

    //TODO: row used to Mock the other Peers, creating them dinamically
    //peers.forEach(MockPullServer::new);

   // System.out.println("Pulling " + peers);

    List<Observable<List<Peer>>> requestObservables =
        peers.stream().map((peer -> executePullRequest(peer))).collect(Collectors.toList());

   // System.out.println("Aftergame");
    return Observable.combineLatest(requestObservables, responses -> {

    //  System.out.println("wait");
      ArrayList<Peer> result = new ArrayList<Peer>();
      for (Object resp : responses) {
        List<Peer> response = (List<Peer>) resp;
        result.addAll(response);
      }
      return result;
    });
  }

  /**
   * Executes the real request to a client
   *
   * @param peer The Peer we want to send a pull request to
   * @return the local view of the contacted peer
   */
  private Observable<List<Peer>> executePullRequest(Peer peer) {

    //System.out.println("Pulling " + peer.);

    return TcpClient.newClient(peer.getPullServerAdress())
        .enableWireLogging("PullClient", LogLevel.DEBUG)
        .createConnectionRequest()
        .flatMap(
            connection ->
                connection.write(Observable.just(MessageParser.getPullLocalView()))
                    .cast(ByteBuf.class)
                    .concatWith(connection.getInput())
        )
        .lift(ByteBufAggregatorOperator.create())
        .map(SerializationUtils::fromByteArrays);
  }
}
