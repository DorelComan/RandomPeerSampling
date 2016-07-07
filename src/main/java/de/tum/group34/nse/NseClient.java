package de.tum.group34.nse;

import de.tum.group34.Message;
import de.tum.group34.MessageParser;
import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * class that is responsible to communicate with the NSE Module periodically
 *
 * @author Hannes Dorfmann
 */
public class NseClient {

  private TcpClient<ByteBuf, ByteBuf> client;
  private BehaviorSubject<Integer> networkSize = BehaviorSubject.create();

  public NseClient(
      TcpClient<ByteBuf, ByteBuf> client) {
    this.client = client;
    this.client.enableWireLogging(LogLevel.DEBUG)
        .createConnectionRequest()
        .flatMap(connection ->
            Observable.interval(10, TimeUnit.SECONDS)
                .flatMap(interval -> connection.write(Observable.just(MessageParser.getNseQuery()))
                    .cast(ByteBuf.class)
                    .concatWith(connection.getInput())
                )
            .map(byteBuf -> MessageParser.getSizeFromNseMessage(byteBuf))
        ).subscribe(networkSize);
  }

  public Observable<Integer> getNetworkSize() {
    // TODO implement
    return Observable.just(5);
  }
}
