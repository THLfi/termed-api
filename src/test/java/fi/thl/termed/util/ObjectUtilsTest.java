package fi.thl.termed.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObjectUtilsTest {

  @Test
  @SuppressWarnings("NullArgumentToVariableArgMethod")
  public void shouldFindFirstNonnullValue() {
    assertEquals("A", ObjectUtils.coalesce("A", "B", null, "C"));
    assertEquals("A", ObjectUtils.coalesce(null, "A", "B", null, "C"));
    assertEquals("A", ObjectUtils.coalesce(null, null, "A", "B", null, "C"));
    assertEquals("A", ObjectUtils.coalesce(null, null, null, "A", "B", null, "C"));
    assertEquals("A", ObjectUtils.coalesce(null, null, null, null, "A", "B", null, "C"));
    assertEquals("A", ObjectUtils.coalesce("A", "B", null, "C"));
    assertEquals(null, ObjectUtils.coalesce(null));
    assertEquals(null, ObjectUtils.coalesce(null, null));
    assertEquals(null, ObjectUtils.coalesce(null, null, null));
    assertEquals(null, ObjectUtils.coalesce(null, null, null, null));
  }

}