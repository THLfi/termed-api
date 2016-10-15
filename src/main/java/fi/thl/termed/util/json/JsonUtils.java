package fi.thl.termed.util.json;

import com.google.common.base.Charsets;
import java.util.function.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonUtils {

  private static final String ARRAY_INDEX = "\\[(\\d+)\\]";
  private static final String OBJECT_KEY_DOT_NOTATION = "([^\\.\\[\\]]+)";
  private static final String OBJECT_KEY_BRACKET_NOTATION = "\\[[\"\']([^\"\'\\]]*)[\"\']\\]";

  private static final Pattern KEY_PATTERN = Pattern.compile(OBJECT_KEY_DOT_NOTATION + "|" +
                                                             OBJECT_KEY_BRACKET_NOTATION + "|" +
                                                             ARRAY_INDEX);

  private static final JsonParser jsonParser = new JsonParser();
  private static final Gson gson = new Gson();

  private JsonUtils() {
  }

  public static <T> T getJsonResource(String resourceName, Class<T> type) throws IOException {
    return gson.fromJson(getJsonResource(resourceName), type);
  }

  public static JsonElement getJsonResource(String resourceName) throws IOException {
    return jsonParser.parse(
        Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8));
  }

  /**
   * Transform JSON object entries using JsonObjectEntryTransformer. Traverses tree recursively.
   *
   * @return new transformed element
   */
  public static JsonElement transformEntries(JsonElement element,
                                             JsonObjectEntryTransformer transformer) {

    if (element.isJsonObject()) {
      return transformEntries(element.getAsJsonObject(), transformer);
    }
    if (element.isJsonArray()) {
      return transformEntries(element.getAsJsonArray(), transformer);
    }
    return element;
  }

  public static JsonArray transformEntries(JsonArray array,
                                           JsonObjectEntryTransformer transformer) {

    JsonArray result = new JsonArray();

    for (JsonElement element : array) {
      result.add(transformEntries(element, transformer));
    }

    return result;

  }

  public static JsonObject transformEntries(JsonObject object,
                                            JsonObjectEntryTransformer transformer) {

    JsonObject result = new JsonObject();

    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      result.add(entry.getKey(),
                 transformer.transformEntry(entry.getKey(),
                                            transformEntries(entry.getValue(), transformer)));
    }

    return result;
  }

  /**
   * Transform JSON object keys using provided function. Traverses tree recursively.
   *
   * @return new transformed element
   */
  public static JsonElement transformKeys(JsonElement element, Function<String, String> function) {
    if (element.isJsonObject()) {
      return transformKeys(element.getAsJsonObject(), function);
    }
    if (element.isJsonArray()) {
      return transformKeys(element.getAsJsonArray(), function);
    }
    return element;
  }

  public static JsonArray transformKeys(JsonArray array, Function<String, String> function) {
    JsonArray result = new JsonArray();

    for (JsonElement element : array) {
      result.add(transformKeys(element, function));
    }

    return result;
  }

  public static JsonObject transformKeys(JsonObject object, Function<String, String> function) {
    JsonObject result = new JsonObject();

    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      result.add(function.apply(entry.getKey()), transformKeys(entry.getValue(), function));
    }

    return result;
  }

  /**
   * Filters JSON object keys using provided predicate. Traverses tree recursively.
   *
   * @return new element with fields accepted by predicate
   */
  public static JsonElement filterKeys(JsonElement element, Predicate<String> predicate) {
    if (element.isJsonObject()) {
      return filterKeys(element.getAsJsonObject(), predicate);
    }
    if (element.isJsonArray()) {
      return filterKeys(element.getAsJsonArray(), predicate);
    }
    return element;
  }

  public static JsonArray filterKeys(JsonArray array, Predicate<String> predicate) {
    JsonArray result = new JsonArray();

    for (JsonElement element : array) {
      result.add(filterKeys(element, predicate));
    }

    return result;
  }

  public static JsonObject filterKeys(JsonObject object, Predicate<String> predicate) {
    JsonObject result = new JsonObject();

    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      if (predicate.apply(entry.getKey())) {
        result.add(entry.getKey(), filterKeys(entry.getValue(), predicate));
      }
    }

    return result;
  }

  /**
   * Filters JSON array and object values using provided predicates. Traverses tree recursively.
   *
   * @return new element with fields accepted by predicate
   */
  public static JsonElement filterValues(JsonElement element,
                                         Predicate<JsonElement>... predicates) {
    JsonElement result = element;
    for (Predicate<JsonElement> predicate : predicates) {
      result = filterValues(result, predicate);
    }
    return result;
  }

  public static JsonElement filterValues(JsonElement value, Predicate<JsonElement> predicate) {
    if (value.isJsonObject()) {
      return filterValues(value.getAsJsonObject(), predicate);
    }
    if (value.isJsonArray()) {
      return filterValues(value.getAsJsonArray(), predicate);
    }
    return value;
  }

  private static JsonElement filterValues(JsonObject value, Predicate<JsonElement> predicate) {
    JsonObject filteredObject = new JsonObject();

    for (Map.Entry<String, JsonElement> entry : value.entrySet()) {
      JsonElement filteredValue = filterValues(entry.getValue(), predicate);

      if (predicate.apply(filteredValue)) {
        filteredObject.add(entry.getKey(), filteredValue);
      }
    }

    return filteredObject;
  }

  private static JsonElement filterValues(JsonArray values,
                                          Predicate<JsonElement> predicate) {
    JsonArray filteredValues = new JsonArray();

    for (JsonElement value : values) {
      JsonElement filteredValue = filterValues(value, predicate);

      if (predicate.apply(filteredValue)) {
        filteredValues.add(filteredValue);
      }
    }

    return filteredValues;
  }

  /**
   * Create map from json element where nested structures are flattened to multi part keys separated
   * with dot. E.g. "{"foo":{"bar":"value"}}" is flattened to "foo.bar": "value".
   *
   * @param element to be flattened
   * @return map containing flattened json element
   */
  public static Map<String, String> flatten(JsonElement element) {
    Map<String, String> results = Maps.newLinkedHashMap();
    flatten(element, new ArrayDeque<String>(), results);
    return results;
  }

  private static void flatten(JsonElement element, Deque<String> path, Map<String, String> map) {
    if (element.isJsonObject()) {
      flatten(element.getAsJsonObject(), path, map);
    } else if (element.isJsonArray()) {
      flatten(element.getAsJsonArray(), path, map);
    } else if (element.isJsonPrimitive()) {
      map.put(Joiner.on("").join(path), element.getAsString());
    }
  }

  private static void flatten(JsonObject object, Deque<String> path, Map<String, String> map) {
    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      String key = entry.getKey();
      path.addLast(key.matches("[a-zA-Z_]+") ? (path.isEmpty() ? "" : ".") + key
                                             : "[\"" + key + "\"]");
      flatten(entry.getValue(), path, map);
      path.removeLast();
    }
  }

  private static void flatten(JsonArray array, Deque<String> path, Map<String, String> map) {
    int i = 0;
    for (JsonElement element : array) {
      path.addLast("[" + i++ + "]");
      flatten(element, path, map);
      path.removeLast();
    }
  }

  /**
   * Create JsonObject from map where keys are (nested) json object keys and values are object
   * values. E.g. mapping form "foo.bar" => "value" returns json object "{"foo":{"bar":"value"}}.
   *
   * @param map of key value pairs, where keys can represent nested objects separated by dots
   * @return un-flattened json object
   */
  public static JsonElement unflatten(Map<String, String> map) {
    // root element that is returned
    JsonElement result = null;

    for (Map.Entry<String, String> entry : map.entrySet()) {

      // tokenize into list of object field names and array indices
      Iterator<Object> path = tokenize(entry.getKey()).iterator();

      Object property = path.next();

      // In the first round, init the root object/array
      if (result == null) {
        result = newElementFor(property);
      }

      JsonElement target = result;

      // init nested object/array structure and find the target to add the value
      while (path.hasNext()) {
        Object nextProperty = path.next();
        target = ensure(target, property, nextProperty);
        property = nextProperty;
      }

      set(target, property, new JsonPrimitive(entry.getValue()));
    }

    return result;
  }

  private static List<Object> tokenize(String property) {
    Matcher m = KEY_PATTERN.matcher(property);
    List<Object> tokens = Lists.newArrayList();
    while (m.find()) {
      if (m.groupCount() == 3) {
        String objectKey = m.group(1) != null ? m.group(1) : m.group(2);
        tokens.add(objectKey != null ? objectKey : new Integer(m.group(3)));
      }
    }
    return tokens;
  }

  private static JsonElement newElementFor(Object property) {
    return property instanceof Integer ? new JsonArray() : new JsonObject();
  }

  private static JsonElement set(JsonElement target, Object property, JsonElement value) {
    if (target.isJsonObject()) {
      return set(target.getAsJsonObject(), (String) property, value);
    }
    if (target.isJsonArray()) {
      return set(target.getAsJsonArray(), (Integer) property, value);
    }

    throw new IllegalStateException("Failed to set: " + target + ", " + property + ", " + value);
  }

  private static JsonElement ensure(JsonElement target, Object property, Object nextProperty) {
    JsonElement initValue = newElementFor(nextProperty);

    if (target.isJsonObject()) {
      return ensure(target.getAsJsonObject(), (String) property, initValue);
    }
    if (target.isJsonArray()) {
      return ensure(target.getAsJsonArray(), (Integer) property, initValue);
    }

    throw new IllegalStateException(
        "Failed to ensure: " + target + ", " + property + ", " + nextProperty);
  }

  public static JsonElement ensure(JsonObject target, String field, JsonElement initValue) {
    return has(target, field) ? get(target, field) : set(target, field, initValue);
  }

  public static boolean has(JsonObject target, String field) {
    return !JsonNull.INSTANCE.equals(get(target, field));
  }

  public static JsonElement get(JsonObject object, String field) {
    return nullToJsonNull(object.get(field));
  }

  public static JsonElement set(JsonObject object, String field, JsonElement value) {
    object.add(field, value);
    return value;
  }

  private static JsonElement ensure(JsonArray target, Integer index, JsonElement initValue) {
    return has(target, index) ? get(target, index) : set(target, index, initValue);
  }

  public static boolean has(JsonArray array, Integer index) {
    return !JsonNull.INSTANCE.equals(get(array, index));
  }

  public static JsonElement get(JsonArray array, Integer index) {
    return index < array.size() ? nullToJsonNull(array.get(index)) : JsonNull.INSTANCE;
  }

  public static JsonElement set(JsonArray array, Integer index, JsonElement value) {
    while (index >= array.size()) {
      array.add(JsonNull.INSTANCE);
    }
    array.set(index, value);
    return value;
  }

  public static JsonElement nullToJsonNull(JsonElement element) {
    return element == null ? JsonNull.INSTANCE : element;
  }

}
