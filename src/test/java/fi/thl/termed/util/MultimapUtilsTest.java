package fi.thl.termed.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ArrayListMultimap;
import fi.thl.termed.util.collect.MultimapUtils;
import org.junit.jupiter.api.Test;

class MultimapUtilsTest {

  @Test
  void shouldTransformNullToEmptyMultimap() {
    assertEquals(ArrayListMultimap.create(), MultimapUtils.nullToEmpty(null));
  }

}
