package de.tum.group34;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import module.Peer;
import rx.Observable;

/**
 * This class is responsible to receive Peers, either over gossip or directly pushed by {@link
 * Brahms}
 */
public class PushReceiver {

  Observable<List<Peer>> gossipSocket;
  Observable<List<Peer>> pushReceivingSocket;

  public PushReceiver(Observable<Peer> gossipSocket,
      Observable<Peer> pushReceivingSocket) {
    this.gossipSocket = gossipSocket.buffer(1, TimeUnit.MINUTES).onBackpressureDrop();
    this.pushReceivingSocket = pushReceivingSocket.buffer(1, TimeUnit.MINUTES).onBackpressureDrop();
  }

  public Observable<ArrayList<Peer>> getPushList() {
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
