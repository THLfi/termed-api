package fi.thl.termed.util.collect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class OptionalUtilsTest {

  @Test
  void shouldFindFirstNonEmptyOptional() {
    Optional<String> firstNonEmptyOptional = OptionalUtils.findFirst(
        Optional.empty(),
        Optional.of("foo"),
        Optional.of("bar"));

    assertEquals("foo", firstNonEmptyOptional.orElseThrow(AssertionError::new));
  }

  @Test
  void shouldLazyFindFirstNonEmptyOptional() {
    Optional<String> firstNonEmptyOptional = OptionalUtils.lazyFindFirst(
        () -> Optional.empty(),
        () -> Optional.of("foo"),
        () -> {
          throw new AssertionError("Lazy loading failed");
        });

    assertEquals("foo", firstNonEmptyOptional.orElseThrow(AssertionError::new));
  }


}