package fi.thl.termed.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class DurationUtilsTest {

  @Test
  void shouldPrettyPrintDurationInNanos() {
    assertEquals("1h 0m 0.000s", DurationUtils.prettyPrintNanos(TimeUnit.HOURS.toNanos(1)));
    assertEquals("1m 0.000s", DurationUtils.prettyPrintNanos(TimeUnit.MINUTES.toNanos(1)));
    assertEquals("1.000s", DurationUtils.prettyPrintNanos(TimeUnit.SECONDS.toNanos(1)));
    assertEquals("0.001s", DurationUtils.prettyPrintNanos(TimeUnit.MILLISECONDS.toNanos(1)));
  }

  @Test
  void shouldPrettyPrintDurationInMillis() {
    assertEquals("1h 0m 0.000s", DurationUtils.prettyPrintMillis(TimeUnit.HOURS.toMillis(1)));
    assertEquals("1m 0.000s", DurationUtils.prettyPrintMillis(TimeUnit.MINUTES.toMillis(1)));
    assertEquals("1.000s", DurationUtils.prettyPrintMillis(TimeUnit.SECONDS.toMillis(1)));
    assertEquals("0.001s", DurationUtils.prettyPrintMillis(TimeUnit.MILLISECONDS.toMillis(1)));
  }

}