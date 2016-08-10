package de.tum.group34.nse;

import de.tum.group34.serialization.MessageParser;
import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
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

  private TcpClient<ByteBuf, ByteBuf> client;
  private BehaviorSubject<Integer> networkSize = BehaviorSubject.create();

  /**
   * Creates a new instance
   *
   * @param client The TcpClient
   * @param interval The time interval when to query the network size
   * @param timeUnit The intervals time unit
   */
  public NseClient(
      TcpClient<ByteBuf, ByteBuf> client, long interval, TimeUnit timeUnit) {
    this.client = client;
    this.client.enableWireLogging(LogLevel.DEBUG)
        .createConnectionRequest()
        .flatMap(connection ->
            Observable.interval(interval, timeUnit)
                .flatMap(
                    intervalCount -> connection.write(Observable.just(MessageParser.getNseQuery()))
                        .cast(ByteBuf.class)
                        .concatWith(connection.getInput())
                )
                .map(byteBuf -> MessageParser.getSizeFromNseMessage(byteBuf))
        )
        .subscribe(networkSize);
  }

  public Observable<Integer> getNetworkSize() {
    return networkSize;
  }
}
