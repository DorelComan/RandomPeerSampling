package de.tum.group34.push;

import de.tum.group34.TcpClientFactory;
import de.tum.group34.model.Peer;
import de.tum.group34.serialization.SerializationUtils;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import rx.Observable;

/**
 * This class is responsible to push the own ID ({@link Peer}) to a remote Peer
 *
 * @author Hannes Dorfmann
 */
public class PushSender {

  private Peer ownPeer;
  private TcpClientFactory clientFactory;
  private int pushServerPort;

  public PushSender(Peer ownPeer, TcpClientFactory clientFactory, int pushServerPort) {
    this.ownPeer = ownPeer;
    this.clientFactory = clientFactory;
    this.pushServerPort = pushServerPort;
  }

  /**
   * This methods sends the own Peer to the list of peers
   *
   * @param receivers The receivers, who will receive the own Peer id
   * @return A list of peers that has failed. If successful, this will return an empty list.
   */
  public Observable<List<Peer>> sendMyId(List<Peer> receivers) {
    List<Observable<PushResult>> sendToPeersObservables =
        receivers.stream().map(this::sendMyIdTo).collect(
            Collectors.toList());

    return Observable.combineLatest(sendToPeersObservables, pushResults ->
        Arrays.stream((PushResult[]) pushResults)
            .filter(PushResult::hasFailed)
            .map(result -> result.peer)
            .collect(Collectors.toList())
    );
  }

  /**
   * Sends the own peer to a specific peer
   *
   * @param to the peer who should receive this message
   * @return Observable of {@link PushResult}
   */
  Observable<PushResult> sendMyIdTo(Peer to) {
    // TODO exponential retry?
    return clientFactory.newClient(
        new InetSocketAddress(to.getIpAddress().getAddress(), pushServerPort))
        .createConnectionRequest()
        .flatMap(
            connection -> connection.writeBytes(
                Observable.just(SerializationUtils.toBytes(ownPeer)))
                .concatWith(connection.close(true))
                .map(aVoid -> new PushResult(to))
        ).onErrorReturn(throwable -> new PushResult(to, throwable));
  }

  /**
   * Tiny class that represents whether pushing the own peer to the remote peer was successful or
   * not. If there was an error, {@link PushResult#error} will contain the error cause. In case that
   * it was successful, {@link PushResult#error} will be null
   */
  private static class PushResult {
    Peer peer;
    Throwable error;

    /**
     * Constructor for successful
     */
    public PushResult(Peer peer) {
      this.peer = peer;
    }

    /**
     * Constructor for error
     */
    public PushResult(Peer peer, Throwable error) {
      this.peer = peer;
      this.error = error;
    }

    boolean isSuccessful() {
      return error != null;
    }

    boolean hasFailed() {
      return !isSuccessful();
    }
  }
}
