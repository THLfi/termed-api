package fi.thl.termed.util;

import com.google.common.util.concurrent.FutureCallback;
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

  public static <V> FutureCallback<V> errorHandler(Consumer<Throwable> errorHandler) {
    return new FutureCallback<V>() {
      @Override
      public void onSuccess(V result) {
      }

      @Override
      public void onFailure(Throwable t) {
        errorHandler.accept(t);
      }
    };
  }

}
