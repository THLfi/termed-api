package fi.thl.termed.util.json;

import static com.google.common.collect.Iterables.elementsEqual;
import static fi.thl.termed.util.json.JsonElementFactory.array;
import static fi.thl.termed.util.json.JsonElementFactory.primitive;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class StreamTypeAdapterFactoryTest {

  private Type streamOfIntegersType = new TypeToken<Stream<Integer>>() {
  }.getType();

  private Gson gsonPlain = new GsonBuilder()
      .create();

  private Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new StreamTypeAdapterFactory())
      .create();

  @Test
  void plainGsonShouldNotDeserializeStreams() {
    assertThrows(RuntimeException.class, () -> gsonPlain.fromJson("[1,2,3]", streamOfIntegersType));
  }

  @Test
  void shouldDeserializeToStream() {
    Stream<Integer> actual = gson.fromJson("[1,2,3]", streamOfIntegersType);

    assertTrue(elementsEqual(asList(1, 2, 3), actual.collect(toList())));
  }

  @Test
  void shouldSerializeStreams() {
    JsonElement expected = array(primitive(1), primitive(2), primitive(3));

    Stream<Integer> values = Stream.of(1, 2, 3);
    JsonElement actual = gson.toJsonTree(values, streamOfIntegersType);

    assertEquals(expected, actual);
  }

}