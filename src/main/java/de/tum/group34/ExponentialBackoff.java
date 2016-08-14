package de.tum.group34;

import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.functions.Func1;

/**
 * Factory that creates Exponential Backoff retry functions
 *
 * @author Hannes Dorfmann
 */
public class ExponentialBackoff {
  private ExponentialBackoff() {
  }

  /**
   * Creates a Function that will do exponential backoff retry
   *
   * @param maxRetries The number of maximal retries
   * @param initialDelay The delay before starting the second retry. This is will grow exponentially
   * with every retry
   * @param timeUnit The TimeUnit for inital delay
   * @return A Retry Observale
   */
  public static Func1<Observable<? extends Throwable>, Observable<?>> create(int maxRetries,
      long initialDelay, TimeUnit timeUnit) {
    return errors -> errors.zipWith(Observable.range(1, maxRetries), (n, i) -> i)
        .flatMap(
            retryCount -> Observable.timer((long) Math.pow(initialDelay, retryCount), timeUnit));
  }
}
