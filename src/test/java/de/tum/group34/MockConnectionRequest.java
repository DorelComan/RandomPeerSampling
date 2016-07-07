package de.tum.group34;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.channel.Connection;
import io.reactivex.netty.client.ConnectionRequest;
import rx.Subscriber;

class MockConnectionRequest extends ConnectionRequest {

    public MockConnectionRequest(Connection<ByteBuf, ByteBuf> connection) {

      super(new OnSubscribe<Connection>() {
        @Override public void call(Subscriber<? super Connection> subscriber) {
          subscriber.onNext(connection);
        }
      });
    }
  }