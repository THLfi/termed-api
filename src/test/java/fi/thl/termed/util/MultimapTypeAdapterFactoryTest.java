package fi.thl.termed.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import fi.thl.termed.util.json.MultimapTypeAdapterFactory;
import org.junit.jupiter.api.Test;

class MultimapTypeAdapterFactoryTest {

  private Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new MultimapTypeAdapterFactory())
      .setPrettyPrinting()
      .create();

  @Test
  void shouldSerializeAndDeserializeMultimap() {
    Multimap<String, Integer> multimap = ArrayListMultimap.create();
    multimap.put("firstKey", 1);
    multimap.put("firstKey", 2);
    multimap.put("secondKey", 1);

    JsonObject jsonMultimap = new JsonObject();
    JsonArray firstKeyValues = new JsonArray();
    firstKeyValues.add(1);
    firstKeyValues.add(2);
    jsonMultimap.add("firstKey", firstKeyValues);
    JsonArray secondKeyValues = new JsonArray();
    secondKeyValues.add(1);
    jsonMultimap.add("secondKey", secondKeyValues);

    assertEquals(jsonMultimap, gson.toJsonTree(multimap));

    TypeToken<Multimap<String, Integer>> multimapTypeToken =
        new TypeToken<Multimap<String, Integer>>() {
        };
    assertEquals(multimap, gson.fromJson(jsonMultimap, multimapTypeToken.getType()));
  }

}
