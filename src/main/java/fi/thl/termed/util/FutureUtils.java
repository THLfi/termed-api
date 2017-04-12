package fi.thl.termed.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public final class FutureUtils {

  private FutureUtils() {
  }

  public static void waitFor(Future<?> future, long timeout, TimeUnit unit,
      Consumer<Exception> exceptionConsumer) {
    try {
      future.get(timeout, unit);
    } catch (InterruptedException | TimeoutException | ExecutionException e) {
      exceptionConsumer.accept(e);
    }
  }

}
