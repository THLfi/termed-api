package fi.thl.termed.web.external.node.transform;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import fi.thl.termed.domain.NodeQuery;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.json.JsonUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class NodeQueryParser {

  public static Type stringListType = new TypeToken<List<String>>() {
  }.getType();

  public static Type stringMultimapType = new TypeToken<Multimap<String, String>>() {
  }.getType();

  public static Type uuidMultimapType = new TypeToken<Multimap<String, UUID>>() {
  }.getType();

  private static Gson gson = new GsonBuilder()
      .registerTypeAdapter(stringListType, new LenientStringListDeserializer())
      .registerTypeAdapter(stringMultimapType, new LenientStringMultimapDeserializer())
      .registerTypeAdapter(uuidMultimapType, new LenientUuidMultimapDeserializer())
      .create();

  private NodeQueryParser() {
  }

  /**
   * Parses property style key value map to query. Map could contain e.g. { "select.property":
   * ["prefLabel", "altLabel"], "where.reference.broader": ["animals"] }
   */
  public static NodeQuery parse(Map<String, List<String>> queryMap) {
    Map<String, String> map = new LinkedHashMap<>();

    queryMap.forEach((key, values) -> {
      if (values.size() == 1) {
        map.put(key, values.iterator().next());
      } else {
        // generate unique keys for multimap values using array index notation, these will be
        // un-flattened to json lists
        int i = 0;
        for (String value : values) {
          map.put(key + "[" + i++ + "]", value);
        }
      }
    });

    JsonElement json = JsonUtils.unflatten(map);

    return gson.fromJson(json != null ? json : new JsonObject(), NodeQuery.class);
  }

  /**
   * Allows deserialization from e.g. "foo" to a Java list of ["foo"]. A regular "non-lenient"
   * deserializer would not allow list to be created from a primitive string.
   */
  private static class LenientStringListDeserializer implements JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(
        JsonElement json, Type t, JsonDeserializationContext context) throws JsonParseException {
      if (json.isJsonArray()) {
        List<String> strings = new ArrayList<>();
        json.getAsJsonArray().forEach(e -> strings.add(e.getAsString()));
        return strings;
      } else if (json.isJsonPrimitive()) {
        return Collections.singletonList(json.getAsString());
      } else {
        throw new IllegalStateException();
      }
    }
  }

  /**
   * Allows conversion from e.g. { "a": ["x", "y"], "b": "z" } to a multimap. A regular
   * "non-lenient" deserializer would not allow multimap values to be primitives.
   */
  private static class LenientStringMultimapDeserializer
      implements JsonDeserializer<Multimap<String, String>> {

    @Override
    public Multimap<String, String> deserialize(
        JsonElement json, Type t, JsonDeserializationContext context) throws JsonParseException {

      Multimap<String, String> map = LinkedHashMultimap.create();

      for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
        JsonElement value = entry.getValue();

        if (value.isJsonArray()) {
          value.getAsJsonArray().forEach(v -> map.put(entry.getKey(), v.getAsString()));
        } else if (value.isJsonPrimitive()) {
          map.put(entry.getKey(), value.getAsString());
        } else {
          throw new IllegalStateException();
        }
      }

      return map;
    }
  }

  private static class LenientUuidMultimapDeserializer
      implements JsonDeserializer<Multimap<String, UUID>> {

    @Override
    public Multimap<String, UUID> deserialize(
        JsonElement json, Type t, JsonDeserializationContext context) throws JsonParseException {

      Multimap<String, UUID> map = LinkedHashMultimap.create();

      for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
        JsonElement value = entry.getValue();

        if (value.isJsonArray()) {
          value.getAsJsonArray()
              .forEach(v -> map.put(entry.getKey(), UUIDs.fromString(v.getAsString())));
        } else if (value.isJsonPrimitive()) {
          String s = value.getAsString();
          map.put(entry.getKey(), s.isEmpty() || s.equals("null") ? null : UUIDs.fromString(s));
        } else {
          throw new IllegalStateException();
        }
      }

      return map;
    }
  }

}
