package fi.thl.termed.util.collect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class StreamUtilsTest {

  private class ExampleObject {

    private Integer id;
    private String value;

    public ExampleObject(Integer id, String value) {
      this.id = id;
      this.value = value;
    }

  }

  @Test
  void shouldCreateDistinctStreamByKey() {
    assertEquals("foo, bar",
        StreamUtils.distinctByKey(Stream.of(
            new ExampleObject(0, "foo"),
            new ExampleObject(1, "bar"),
            new ExampleObject(1, "baz")),
            object -> object.id)
            .map(object -> object.value)
            .collect(Collectors.joining(", ")));
  }

}