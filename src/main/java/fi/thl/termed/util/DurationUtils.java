package fi.thl.termed.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public final class DurationUtils {

  private DurationUtils() {
  }

  /**
   * Formats duration given in nanoseconds to human readable form.
   *
   * @param durationInNanos duration in nanoseconds
   * @return pretty formatted string of duration (e.g. 9h 1m 10.091s)
   */
  public static String prettyPrintNanos(long durationInNanos) {
    long h = NANOSECONDS.toHours(durationInNanos);
    long m = NANOSECONDS.toMinutes(durationInNanos) % 60;
    double s = (durationInNanos / 1_000_000_000.0) % 60;

    if (h > 0) {
      return String.format("%dh %dm %.3fs", h, m, s);
    } else if (m > 0) {
      return String.format("%dm %.3fs", m, s);
    } else {
      return String.format("%.3fs", s);
    }
  }

  /**
   * Formats duration given in milliseconds to human readable form.
   *
   * @param durationInMillis duration in milliseconds
   * @return pretty formatted string of duration (e.g. 9h 1m 10.091s)
   */
  public static String prettyPrintMillis(long durationInMillis) {
    long h = MILLISECONDS.toHours(durationInMillis);
    long m = MILLISECONDS.toMinutes(durationInMillis) % 60;
    double s = (durationInMillis / 1_000.0) % 60;

    if (h > 0) {
      return String.format("%dh %dm %.3fs", h, m, s);
    } else if (m > 0) {
      return String.format("%dm %.3fs", m, s);
    } else {
      return String.format("%.3fs", s);
    }
  }

}
