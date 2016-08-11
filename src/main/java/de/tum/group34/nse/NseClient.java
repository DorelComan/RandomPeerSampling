package de.tum.group34.nse;

import de.tum.group34.TcpClientFactory;
import de.tum.group34.serialization.MessageParser;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * class that is responsible to communicate with the NSE Module periodically.
 * Basically it queries NSE Module periodically for Network size.
 *
 * @author Hannes Dorfmann
 */
public class NseClient {

  private BehaviorSubject<Integer> networkSize;

  /**
   * Creates a new instance
   *
   * @param clientFactory The Factory to create TcpClients on the fly
   * @param interval The time interval when to query the network size
   * @param timeUnit The intervals time unit
   */
  public NseClient(
      TcpClientFactory clientFactory, long interval, TimeUnit timeUnit) {

    networkSize = BehaviorSubject.create();

    Observable.interval(0, interval, timeUnit)
        .flatMap(aLong -> clientFactory.newClient().createConnectionRequest())
        .onBackpressureLatest()
        .flatMap(connection -> connection.getInput())
        .map(byteBuf -> MessageParser.getSizeFromNseMessage(byteBuf))
        .subscribe(networkSize);
  }

  public Observable<Integer> getNetworkSize() {
    return networkSize;
  }
}
