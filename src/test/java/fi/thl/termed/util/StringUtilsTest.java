package fi.thl.termed.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

  @Test
  public void shouldNormalizeString() {
    Assert.assertEquals("oljy", StringUtils.normalize("öljy"));
    Assert.assertEquals("Cafe", StringUtils.normalize("Café"));
  }

}