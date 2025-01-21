package fi.thl.termed.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class DurationUtilsTest {

  @Test
  void shouldPrettyPrintDurationInNanos() {
    assertExpectedPattern("1h 0m 0.000s", DurationUtils.prettyPrintNanos(TimeUnit.HOURS.toNanos(1)));
    assertExpectedPattern("1m 0.000s", DurationUtils.prettyPrintNanos(TimeUnit.MINUTES.toNanos(1)));
    assertExpectedPattern("1.000s", DurationUtils.prettyPrintNanos(TimeUnit.SECONDS.toNanos(1)));
    assertExpectedPattern("0.001s", DurationUtils.prettyPrintNanos(TimeUnit.MILLISECONDS.toNanos(1)));

  }

  @Test
  void shouldPrettyPrintDurationInMillis() {
    assertExpectedPattern("1h 0m 0.000s", DurationUtils.prettyPrintMillis(TimeUnit.HOURS.toMillis(1)));
    assertExpectedPattern("1m 0.000s", DurationUtils.prettyPrintMillis(TimeUnit.MINUTES.toMillis(1)));
    assertExpectedPattern("1.000s", DurationUtils.prettyPrintMillis(TimeUnit.SECONDS.toMillis(1)));
    assertExpectedPattern("0.001s", DurationUtils.prettyPrintMillis(TimeUnit.MILLISECONDS.toMillis(1)));
  }

  private void assertExpectedPattern(String expected, String actual) {
    String expectedPattern = generateExpectedPattern(expected);
    assertTrue(actual.matches(expectedPattern),
            "Expected pattern: " + expectedPattern + " but was: " + actual);
  }

  private String generateExpectedPattern(String template) {
    // Korvaa kiinteä desimaalierotin (.) regexillä, joka hyväksyy sekä pisteen että pilkun
    return template.replace(".", "[.,]");
  }

}