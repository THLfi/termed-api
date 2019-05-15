package fi.thl.termed.util.collect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class StreamUtilsTest {

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

  @Test
  void shouldProcessEmptyStreamInPartitions() {
    StreamUtils.partitionedMap(Stream.of(), 100, (list) -> {
      fail("should not process anything on empty stream");
      return list.stream();
    });
  }

  @Test
  void shouldProcessSmallStreamInOnePartition() {
    StreamUtils.partitionedMap(Stream.of("a", "b"), 100, (list) -> {
      assertEquals(Arrays.asList("a", "b"), list);
      return list.stream();
    });
  }

  @Test
  void shouldProcessStreamInPartitions() {
    AtomicInteger counter = new AtomicInteger();
    AtomicBoolean isClosed = new AtomicBoolean();

    Stream<String> aToZ = "abcdefghijklmnopqrstuvwxyz".chars()
        .mapToObj(c -> "" + (char) c)
        .onClose(() -> isClosed.set(true));

    // convert to upper and lower in chunks of 3
    aToZ = StreamUtils.partitionedMap(aToZ, 3, list -> {
      if (counter.getAndIncrement() % 2 == 0) {
        return list.stream().map(String::toUpperCase);
      } else {
        return list.stream().map(String::toLowerCase);
      }
    });

    assertEquals("ABCdefGHIjklMNOpqrSTUvwxYZ", aToZ.collect(Collectors.joining()));

    aToZ.close();

    assertTrue(isClosed.get());
  }

  class ExampleObject {

    Integer id;
    String value;

    ExampleObject(Integer id, String value) {
      this.id = id;
      this.value = value;
    }

  }

}