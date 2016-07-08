package de.tum.group34;

import de.tum.group34.serialization.SerializationUtils;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import module.Peer;
import rx.Observable;

/**
 * Responsible to execute PullRequests to all clients and to receive the answers
 */
public class PullClient {

  public Observable<ArrayList<Peer>> makePullRequests(List<Peer> peers) {

    List<Observable<List<Peer>>> requestObservables =
        peers.stream().map((peer -> executePullRequest(peer))).collect(Collectors.toList());

    return Observable.combineLatest(requestObservables, responses -> {

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

    return TcpClient.newClient(peer.getIpAddress())
        .createConnectionRequest()
        .flatMap(
            connection ->
                connection.write(Observable.just(MessageParser.getPullLocalView()))
                    .cast(ByteBuf.class)
                    .concatWith(connection.getInput())
        )
        .first()
        .map(byteBuf -> SerializationUtils.fromByteBuf(byteBuf));
  }
}