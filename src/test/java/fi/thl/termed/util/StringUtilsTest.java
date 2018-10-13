package fi.thl.termed.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Test
  void shouldNormalizeString() {
    assertEquals("oljy", StringUtils.normalize("öljy"));
    assertEquals("Cafe", StringUtils.normalize("Café"));
  }

}