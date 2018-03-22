package fi.thl.termed.util.json;

import static com.google.common.collect.Iterables.elementsEqual;
import static fi.thl.termed.util.json.JsonElementFactory.array;
import static fi.thl.termed.util.json.JsonElementFactory.primitive;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.stream.Stream;
import org.junit.Test;

public class StreamTypeAdapterFactoryTest {

  private Type streamOfIntegersType = new TypeToken<Stream<Integer>>() {
  }.getType();

  private Gson gsonPlain = new GsonBuilder()
      .create();

  private Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new StreamTypeAdapterFactory())
      .create();

  @Test(expected = RuntimeException.class)
  public void plainGsonShouldNotDeserializeStreams() {
    gsonPlain.fromJson("[1,2,3]", streamOfIntegersType);
  }

  @Test
  public void shouldDeserializeToStream() {
    Stream<Integer> actual = gson.fromJson("[1,2,3]", streamOfIntegersType);

    assertTrue(elementsEqual(asList(1, 2, 3), actual.collect(toList())));
  }

  @Test
  public void shouldSerializeStreams() {
    JsonElement expected = array(primitive(1), primitive(2), primitive(3));

    Stream<Integer> values = Stream.of(1, 2, 3);
    JsonElement actual = gson.toJsonTree(values, streamOfIntegersType);

    assertEquals(expected, actual);
  }

}