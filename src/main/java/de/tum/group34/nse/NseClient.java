package de.tum.group34.nse;

import de.tum.group34.TcpClientFactory;
import de.tum.group34.pull.PullServer;
import de.tum.group34.serialization.MessageParser;
import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * class that is responsible to communicate with the NSE Module periodically.
 * Basically it queries NSE Module periodically for Network size.
 *
 * @author Hannes Dorfmann
 */
public class NseClient {

  private static final Logger log = Logger.getLogger(PullServer.class.getName());

  private BehaviorSubject<Integer> networkSize;

  /**
   * Creates a new instance
   *
   * @param clientFactory The Factory to create TcpClients on the fly
   * @param address The address to connect to
   * @param interval The time interval when to query the network size
   * @param timeUnit The intervals time unit
   */
  public NseClient(
      TcpClientFactory clientFactory, InetSocketAddress address, long interval, TimeUnit timeUnit) {

    networkSize = BehaviorSubject.create();

    // TODO exponentioal Backoff
    Observable.interval(0, interval, timeUnit)
        .onBackpressureLatest()
        .flatMap(aLong -> clientFactory.newClient(address).createConnectionRequest())
        .onBackpressureLatest()
        .flatMap(connection -> connection.writeBytes(
            Observable.just(MessageParser.getNseQuery().array()))
            .cast(ByteBuf.class)
            .concatWith(connection.getInput())
            .map(byteBuf -> MessageParser.getSizeFromNseMessage(byteBuf))
            .doOnNext(size -> log.info("NSE: network size: " + size))
            .take(1)
        )
        .subscribe(networkSize);
  }

  public Observable<Integer> getNetworkSize() {
    return networkSize;
  }
}
