package de.tum.group34.nse;

import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.util.concurrent.TimeUnit;
import rx.Observable;

/**
 * class that is responsible to communicate with the NSE Module periodically
 *
 * @author Hannes Dorfmann
 */
public class NseClient {

  private TcpClient<ByteBuf, ByteBuf> client;

  public NseClient(
      TcpClient<ByteBuf, ByteBuf> client) {
    this.client = client;
    this.client.enableWireLogging(LogLevel.DEBUG)
        .createConnectionRequest()
        .flatMap(connection ->
            Observable.interval(10, TimeUnit.SECONDS)
                .flatMap(interval -> connection.write(NseQueryMessageBuilder.newMessage())
                    .cast(ByteBuf.class)
                    .concatWith(connection.getInput())
                )
        );
  }


  public Observable<Integer> getNetworkSize(){
    return Observable.just(5);
  }


}
