package fi.thl.termed.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.thl.termed.util.json.DateTypeAdapter;
import java.util.Date;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

class DateTypeAdapterTest {

  private Gson testGson = new GsonBuilder()
      .setPrettyPrinting()
      .registerTypeAdapter(Date.class, new DateTypeAdapter(DateTimeZone.UTC).nullSafe())
      .create();

  @Test
  void shouldSerializeDateAsISO8601String() {
    Date exampleDate = new Date(0);
    assertEquals("\"1970-01-01T00:00:00.000Z\"", testGson.toJson(exampleDate));
  }

  @Test
  void shouldDeserializeDateFromISO8601String() {
    String dateString = "\"1970-01-01T00:00:00.000Z\"";
    assertEquals(new Date(0), testGson.fromJson(dateString, Date.class));
  }

  @Test
  void shouldSerializeNullDateAsJsonNull() {
    assertEquals("null", testGson.toJson(null, Date.class));
  }

  @Test
  void shouldDeserializeNullDateStringAsNull() {
    assertNull(testGson.fromJson("null", Date.class));
  }

}
