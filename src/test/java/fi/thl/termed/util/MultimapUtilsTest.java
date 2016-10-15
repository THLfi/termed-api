package fi.thl.termed.util;

import com.google.common.collect.ArrayListMultimap;

import org.junit.Test;

import fi.thl.termed.util.collect.MultimapUtils;

import static org.junit.Assert.assertEquals;

public class MultimapUtilsTest {

  @Test
  public void shouldTransformNullToEmptyMultimap() {
    assertEquals(ArrayListMultimap.create(), MultimapUtils.nullToEmpty(null));
  }

}
