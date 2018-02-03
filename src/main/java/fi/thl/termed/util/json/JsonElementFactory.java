package fi.thl.termed.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Factory methods for building gson elements
 */
public final class JsonElementFactory {

  private JsonElementFactory() {
  }

  public static JsonObject object(String property, JsonElement value) {
    JsonObject object = new JsonObject();
    object.add(property, value);
    return object;
  }

  public static JsonObject object(
      String p1, JsonElement v1,
      String p2, JsonElement v2) {
    JsonObject object = new JsonObject();
    object.add(p1, v1);
    object.add(p2, v2);
    return object;
  }

  public static JsonObject object(
      String p1, JsonElement v1,
      String p2, JsonElement v2,
      String p3, JsonElement v3) {
    JsonObject object = new JsonObject();
    object.add(p1, v1);
    object.add(p2, v2);
    object.add(p3, v3);
    return object;
  }

  public static JsonObject object(
      String p1, JsonElement v1,
      String p2, JsonElement v2,
      String p3, JsonElement v3,
      String p4, JsonElement v4) {
    JsonObject object = new JsonObject();
    object.add(p1, v1);
    object.add(p2, v2);
    object.add(p3, v3);
    object.add(p4, v4);
    return object;
  }

  public static JsonPrimitive primitive(String string) {
    return new JsonPrimitive(string);
  }

  public static JsonPrimitive primitive(Boolean bool) {
    return new JsonPrimitive(bool);
  }

  public static JsonPrimitive primitive(Number number) {
    return new JsonPrimitive(number);
  }

  public static JsonArray array(JsonElement... elements) {
    JsonArray array = new JsonArray();
    for (JsonElement element : elements) {
      array.add(element);
    }
    return array;
  }

  public static JsonArray concat(JsonArray... arrays) {
    JsonArray result = new JsonArray();
    for (JsonArray array : arrays) {
      result.addAll(array);
    }
    return result;
  }

}
