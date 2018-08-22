package fi.thl.termed.util.concurrent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public final class ExecutorUtils {

  private ExecutorUtils() {
  }

  /**
   * Creates {@link ScheduledExecutorService} that removes canceled tasks.
   */
  public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize);
    executor.setRemoveOnCancelPolicy(true);
    return executor;
  }

}
