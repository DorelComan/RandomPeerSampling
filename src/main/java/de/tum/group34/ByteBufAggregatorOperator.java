package de.tum.group34;

import de.tum.group34.serialization.SerializationUtils;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.internal.producers.SingleDelayedProducer;

/**
 * @author Hannes Dorfmann
 */
public final class ByteBufAggregatorOperator
    implements Observable.Operator<List<ByteBuf>, ByteBuf> {
  /** Lazy initialization via inner-class holder. */
  static final class Holder {
    /** A singleton instance. */
    static final ByteBufAggregatorOperator INSTANCE = new ByteBufAggregatorOperator();
  }

  /**
   * @return a singleton instance of this stateless operator.
   */
  @SuppressWarnings({"unchecked"})
  public static ByteBufAggregatorOperator instance() {
    return Holder.INSTANCE;
  }

  ByteBufAggregatorOperator() {
    // singleton
  }

  @Override
  public Subscriber<? super ByteBuf> call(final Subscriber<? super List<ByteBuf>> o) {
    final SingleDelayedProducer<List<ByteBuf>> producer =
        new SingleDelayedProducer<List<ByteBuf>>(o);
    Subscriber<ByteBuf> result = new Subscriber<ByteBuf>() {

      boolean completed;
      List<ByteBuf> list = new LinkedList<ByteBuf>();

      @Override
      public void onStart() {
        request(Long.MAX_VALUE);
      }

      @Override
      public void onCompleted() {
        if (!completed) {
          completed = true;
          List<ByteBuf> result;
          try {
                        /*
                         * Ideally this should just return Collections.unmodifiableList(list) and not copy it,
                         * but, it ends up being a breaking change if we make that modification.
                         *
                         * Here is an example of is being done with these lists that breaks if we make it immutable:
                         *
                         * Caused by: java.lang.UnsupportedOperationException
                         *     at java.util.Collections$UnmodifiableList$1.set(Collections.java:1244)
                         *     at java.util.Collections.sort(Collections.java:221)
                         *     ...
                         * Caused by: rx.exceptions.OnErrorThrowable$OnNextValue: OnError while emitting onNext value: UnmodifiableList.class
                         *     at rx.exceptions.OnErrorThrowable.addValueAsLastCause(OnErrorThrowable.java:98)
                         *     at rx.internal.operators.OperatorMap$1.onNext(OperatorMap.java:56)
                         *     ... 419 more
                         */
            result = new ArrayList<ByteBuf>(list);
          } catch (Throwable t) {
            Exceptions.throwOrReport(t, this);
            return;
          }
          list = null;
          producer.setValue(result);
        }
      }

      @Override
      public void onError(Throwable e) {
        o.onError(e);
      }

      @Override
      public void onNext(ByteBuf value) {
        if (!completed) {

          byte[] bytes = SerializationUtils.byteBufToByteArray(value);
          int last = bytes.length - 1;
          char lastByte = (char) bytes[last];
          list.add(SerializationUtils.byteArrayToByteBuf(bytes));

          if (lastByte == SerializationUtils.END_DELIMITER) {
            onCompleted();
          }
        }
      }
    };
    o.add(result);
    o.setProducer(producer);
    return result;
  }
}
