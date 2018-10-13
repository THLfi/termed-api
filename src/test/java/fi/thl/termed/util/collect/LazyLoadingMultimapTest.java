package fi.thl.termed.util.collect;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LazyLoadingMultimapTest {

  private Multimap<String, Integer> map;

  @BeforeEach
  void setUp() {
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
  void shouldNotBeEmpty() {
    assertFalse(map.isEmpty());
  }

  @Test
  void shouldGetSize() {
    assertEquals(3, map.size());
  }

  @Test
  void shouldGetValuesForKey() {
    assertEquals(of(1, 2), map.get("a"));
    assertEquals(of(), map.get("does not exists"));
  }

  @Test
  void shouldContainKey() {
    assertTrue(map.containsKey("a"));
    assertFalse(map.containsKey("does not exist"));
  }

  @Test
  void shouldContainEntry() {
    assertTrue(map.containsEntry("a", 1));
    assertTrue(map.containsEntry("a", 2));
    assertFalse(map.containsEntry("a", 3));
    assertFalse(map.containsEntry("a", of(1)));
    assertFalse(map.containsEntry("b", 1));
    assertFalse(map.containsEntry(true, 1));
  }

  @Test
  void shouldContainValue() {
    assertTrue(map.containsValue(1));
    assertTrue(map.containsValue(2));
    assertTrue(map.containsValue(3));
    assertFalse(map.containsValue(4));
  }

  @Test
  void shouldGetAllValues() {
    assertEquals(of(1, 2, 3), map.values());
  }

  @Test
  void shouldGetAllKeys() {
    assertEquals(ImmutableMultiset.of("a", "a", "b"), map.keys());
  }

  @Test
  void shouldGetKeySet() {
    assertEquals(ImmutableSet.of("a", "b"), map.keySet());
  }

  @Test
  void shouldGetAsMap() {
    assertEquals(ImmutableMap.of("a", of(1, 2), "b", of(3)), map.asMap());
  }

  @Test
  void shouldFailOnPut() {
    assertThrows(UnsupportedOperationException.class, () -> map.put("foo", 1));
  }

  @Test
  void shouldFailOnRemove() {
    assertThrows(UnsupportedOperationException.class, () -> map.remove("foo", 1));
  }

}