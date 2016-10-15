package fi.thl.termed.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.junit.Test;

import java.util.Map;

import fi.thl.termed.util.json.JsonObjectEntryTransformer;
import fi.thl.termed.util.json.JsonPredicates;
import fi.thl.termed.util.json.JsonUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonUtilsTest {

  private JsonParser p = new JsonParser();

  private static JsonArray newArray(JsonElement... elements) {
    JsonArray array = new JsonArray();
    for (JsonElement element : elements) {
      array.add(element);
    }
    return array;
  }

  private static JsonObject newObject(String k1, String v1) {
    JsonObject object = new JsonObject();
    object.addProperty(k1, v1);
    return object;
  }

  private static JsonObject newObject(String k1, String v1,
                                      String k2, String v2) {
    JsonObject object = newObject(k1, v1);
    object.addProperty(k2, v2);
    return object;
  }

  @Test
  public void shouldTransformEntries() {
    JsonElement element = p.parse("{'id':1,'name':{'fi':'Testi','en':'Test'}}");

    JsonElement transformed = JsonUtils.transformEntries(element, new JsonObjectEntryTransformer() {
      public JsonElement transformEntry(String key, JsonElement value) {
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
          return new JsonPrimitive(value.getAsString().toUpperCase());
        }
        return value;
      }
    });

    assertEquals(p.parse("{'id':1,'name':{'fi':'TESTI','en':'TEST'}}"), transformed);
  }

  @Test
  public void shouldTransformKeys() {
    JsonElement element = p.parse("{'id':1,'name':{'fi':'Testi','en':'Test'}}");

    JsonElement transformed = JsonUtils.transformKeys(element, new java.util.function.Function<String, String>() {
      public String apply(String key) {
        return key.toUpperCase();
      }
    });

    assertEquals(p.parse("{'ID':1,'NAME':{'FI':'Testi','EN':'Test'}}"), transformed);
  }

  @Test
  public void shouldFilterMatchingKeys() {
    JsonElement element = p.parse("{'id':1,'name':{'fi':'Testi','en':'Test'}}");

    JsonElement filtered = JsonUtils.filterKeys(element.getAsJsonObject(), new Predicate<String>() {
      public boolean apply(String input) {
        return !input.equals("en");
      }
    });

    assertEquals(p.parse("{'id':1,'name':{'fi':'Testi'}}"), filtered);
  }

  @Test
  public void shouldFilterNullValues() {
    JsonElement element =
        p.parse("{'id':'1','name':{'fi':'Testi','en':null}," +
                "'data':[0,null,1,2,null]}");

    JsonElement filtered =
        JsonUtils.filterValues(element, JsonPredicates.notNull());

    assertEquals(p.parse("{'id':'1','name':{'fi':'Testi'},'data':[0,1,2]}"),
                 filtered);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFilterNullsAndEmptyValues() {
    JsonElement element =
        p.parse("{'id':'1','name':{'fi':'Testi','en':null, 'sv': ''}," +
                "'data':[0,null,1,2,null,'']}");

    JsonElement filtered = JsonUtils
        .filterValues(element, JsonPredicates.notNull(),
                      JsonPredicates.stringNotEmpty());

    assertEquals(p.parse("{'id':'1','name':{'fi':'Testi'},'data':[0,1,2]}"),
                 filtered);
  }

  @Test
  public void shouldFilterMatchingValues() {
    JsonElement element =
        p.parse("{'id':'1','name':{'fi':'    ','en':'Test','sv':'\t \n'}," +
                "'data':[0,null,1,2,null,'']}");

    JsonElement filtered = JsonUtils.filterValues(element,
                                                  JsonPredicates.stringDoesNotMatch("\\s*"));

    assertEquals(p.parse("{'id':'1','name':{'en':'Test'}," +
                         "'data':[0,null,1,2,null]}"), filtered);
  }

  @Test
  public void shouldFilterEmptyTrees() {
    JsonElement element =
        p.parse("{'id':'1','name':{'fi':'','en':'Test','sv':''}," +
                "'data':[],'matrix':[[],[],[]]," +
                "'nested':{'hello':{'world':{'array':[]}}}}");

    JsonElement filtered =
        JsonUtils.filterValues(element, JsonPredicates.notEmpty());

    assertEquals(p.parse("{'id':'1','name':{'en':'Test'}}"), filtered);
  }

  @Test
  public void shouldParseJsonObjectFromFlatMap() {
    Map<String, String> map = Maps.newHashMap();
    map.put("id", "123123");
    map.put("name.fi", "Testi");
    map.put("name.en", "Test");

    JsonElement object = JsonUtils.unflatten(map);

    assertEquals(
        p.parse("{'id':'123123','name':{'fi':'Testi','en':'Test'}}"),
        object);
  }

  @Test
  public void shouldParseJsonObjectFromFlatMapWithEmptyKeys() {
    Map<String, String> map = Maps.newHashMap();
    map.put("[\"\"][\"1\"][0][a].b.['c']['']", "Test1");
    map.put("[\"\"][\"1\"][1][a].b.['c']['']", "Test2");

    JsonElement object = JsonUtils.unflatten(map);

    assertEquals(
        p.parse("{'':{'1':[{'a':{'b':{'c':{'':'Test1'}}}},{'a':{'b':{'c':{'':'Test2'}}}}]}}"),
        object);
  }

  @Test
  public void shouldParseJsonFromFlatMap() {
    Map<String, String> map = Maps.newHashMap();
    map.put("id", "123123");
    map.put("greeting[0]", "Hello");
    map.put("greeting[1]", "Hi");

    JsonElement object = JsonUtils.unflatten(map);

    assertEquals(p.parse("{'id':'123123','greeting':['Hello','Hi']}"),
                 object);
  }

  @Test
  public void shouldParseComplexObjectFromMap() {
    Map<String, String> map = Maps.newHashMap();
    map.put("key", "value");
    map.put("array[0]", "array_value_0");
    map.put("array[1]", "array_value_1");
    map.put("test[0][0]", "01");
    map.put("test[0][1]", "01");
    map.put("test[1][0]", "10");
    map.put("test[1][2]", "12");
    map.put("test[1][1]", "11");
    map.put("super.extra[0].nested[0][1].value", "Hello, world!");

    assertEquals(p.parse("{'super':{'extra':[{'nested':[[null,{'value':" +
                         "'Hello, world!'}]]}]},'test':[['01','01']," +
                         "['10','11','12']],'key':'value','array':" +
                         "['array_value_0','array_value_1']}"),
                 JsonUtils.unflatten(map));
  }

  @Test
  public void shouldBeAbleToUnflattenFlattened() {
    JsonObject object = newObject("id", "1");
    object.add("label", newObject("fi", "Testi", "en", "Test"));
    object.add("altLabels",
               newArray(newObject("fi", "Testiolio", "en", "Test Object"),
                        newObject("fi", "Testattava olio", "en",
                                  "Testable Object")));
    assertEquals(object, JsonUtils.unflatten(JsonUtils.flatten(object)));
  }

  @Test
  public void shouldFlattenJsonObjectWithNestedObjects() {
    JsonElement object = p.parse("{'id':'123123','name':{'fi':'Testi'}," +
                                 "'altNames':{'fi':['Testing','Testing some more']}}");

    Map<String, String> flat = JsonUtils.flatten(object);

    assertEquals("123123", flat.get("id"));
    assertEquals("Testi", flat.get("name.fi"));
    assertTrue(flat.get("altNames.fi[0]").contains("Testing"));
    assertTrue(flat.get("altNames.fi[1]").contains("Testing some more"));
  }

  @Test
  public void shouldFlattenJsonObjectWithNestedArrays() {
    JsonElement object = p.parse("{'a':[{'b':{'c':[1,2,3],'d':[4,5,6]}}]}");

    Map<String, String> flat = JsonUtils.flatten(object);

    assertEquals("1", flat.get("a[0].b.c[0]"));
    assertEquals("2", flat.get("a[0].b.c[1]"));
    assertEquals("3", flat.get("a[0].b.c[2]"));
    assertEquals("4", flat.get("a[0].b.d[0]"));
    assertEquals("5", flat.get("a[0].b.d[1]"));
    assertEquals("6", flat.get("a[0].b.d[2]"));
  }

  @Test
  public void shouldFlattenNestedArrays() {
    JsonElement object = p.parse("[[1,2,3],[4,5,6]]");

    Map<String, String> flat = JsonUtils.flatten(object);

    assertEquals("1", flat.get("[0][0]"));
    assertEquals("2", flat.get("[0][1]"));
    assertEquals("3", flat.get("[0][2]"));
    assertEquals("4", flat.get("[1][0]"));
    assertEquals("5", flat.get("[1][1]"));
    assertEquals("6", flat.get("[1][2]"));
  }

  @Test
  public void shouldFlattenAndUnflattenJsonObjectWithWeirdKeys() {
    JsonElement object = p.parse("{'a b':{'':{'hello':{'.':{'123':['A','B']}}}}}");

    Map<String, String> flat = JsonUtils.flatten(object);

    assertEquals("A", flat.get("[\"a b\"][\"\"].hello[\".\"][\"123\"][0]"));
    assertEquals("B", flat.get("[\"a b\"][\"\"].hello[\".\"][\"123\"][1]"));

    JsonElement reversedObject = JsonUtils.unflatten(flat);

    assertEquals(object, reversedObject);
  }

}
