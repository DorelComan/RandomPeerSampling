package de.tum.group34.nse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import rx.Observable;

/**
 * This class is responsible to create a new
 *
 * @author Hannes Dorfmann
 */
public class NseQueryMessageBuilder {

  private NseQueryMessageBuilder() {
  }

  public static Observable<ByteBuf> newMessage() {
    return Observable.just(
        PooledByteBufAllocator.DEFAULT.buffer().writeBytes("TO BE DONE ".getBytes()));
  }
}
