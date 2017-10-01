package fi.thl.termed.util.json;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

public class ImmutableListDeserializerTest {

  private Gson gson = new GsonBuilder()
      .registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
      .create();

  @Test
  public void shouldSerializeImmutableList() {
    // serialization should be handled by default list serializer
    ImmutableList<Integer> list = ImmutableList.of(1, 2, 3);
    assertEquals("[1,2,3]", gson.toJson(list));
  }

  @Test
  public void shouldDeserializeImmutableList() {
    ImmutableList<Integer> list = gson.fromJson("[1,2,3]", new TypeToken<ImmutableList<Integer>>() {
    }.getType());

    assertEquals(ImmutableList.of(1, 2, 3), list);
  }


}