package fi.thl.termed.util.collect;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.junit.Before;
import org.junit.Test;

public class LazyLoadingMultimapTest {

  private Multimap<String, Integer> map;

  @Before
  public void setUp() {
    map = new LazyLoadingMultimap<>(ImmutableSet.of("a", "b"), key -> {
      switch (key) {
        case "a":
          return of(1, 2);
        case "b":
          return of(3);
        default:
          return of();
      }
    });
  }

  @Test
  public void shouldNotBeEmpty() {
    assertFalse(map.isEmpty());
  }

  @Test
  public void shouldGetSize() {
    assertEquals(3, map.size());
  }

  @Test
  public void shouldGetValuesForKey() {
    assertEquals(of(1, 2), map.get("a"));
    assertEquals(of(), map.get("does not exists"));
  }

  @Test
  public void shouldContainKey() {
    assertTrue(map.containsKey("a"));
    assertFalse(map.containsKey("does not exist"));
  }

  @Test
  public void shouldContainEntry() {
    assertTrue(map.containsEntry("a", 1));
    assertTrue(map.containsEntry("a", 2));
    assertFalse(map.containsEntry("a", 3));
    assertFalse(map.containsEntry("a", of(1)));
    assertFalse(map.containsEntry("b", 1));
    assertFalse(map.containsEntry(true, 1));
  }

  @Test
  public void shouldContainValue() {
    assertTrue(map.containsValue(1));
    assertTrue(map.containsValue(2));
    assertTrue(map.containsValue(3));
    assertFalse(map.containsValue(4));
  }

  @Test
  public void shouldGetAllValues() {
    assertEquals(of(1, 2, 3), map.values());
  }

  @Test
  public void shouldGetAllKeys() {
    assertEquals(ImmutableMultiset.of("a", "a", "b"), map.keys());
  }

  @Test
  public void shouldGetKeySet() {
    assertEquals(ImmutableSet.of("a", "b"), map.keySet());
  }

  @Test
  public void shouldGetAsMap() {
    assertEquals(ImmutableMap.of("a", of(1, 2), "b", of(3)), map.asMap());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldFailOnPut() {
    map.put("foo", 1);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldFailOnRemove() {
    map.remove("foo", 1);
  }

}