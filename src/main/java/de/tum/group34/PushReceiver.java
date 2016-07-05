package de.tum.group34;

import java.util.ArrayList;
import java.util.stream.Collectors;
import module.Peer;
import rx.Observable;

/**
 * This class is responsible to receive Peers, either over gossip or directly pushed by {@link
 * Brahms}
 */
public class PushReceiver {

  Observable<ArrayList<Peer>> gossipSocket = Observable.empty();
  Observable<ArrayList<Peer>> pushReceivingSocket = Observable.empty();

  public PushReceiver(Observable<ArrayList<Peer>> gossipSocket,
      Observable<ArrayList<Peer>> pushReceivingSocket) {
    this.gossipSocket = gossipSocket;
    this.pushReceivingSocket = pushReceivingSocket;
  }

  public Observable<ArrayList<Peer>> getPushList() {

    // TODO implement
    return Observable.combineLatest(gossipSocket, pushReceivingSocket,
        (gossipResonse, pushSocketResponse) -> {
          ArrayList<Peer> mergedPeers = new ArrayList<Peer>();
          mergedPeers.addAll(gossipResonse);
          mergedPeers.addAll(pushSocketResponse);
          // Ensure that a Peer is only represented once in the list
          return new ArrayList<Peer>(mergedPeers.stream().distinct().collect(Collectors.toList()));
        }
    );
  }
}
