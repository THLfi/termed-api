package fi.thl.termed.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;

import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DateTypeAdapterTest {

  private Gson testGson = new GsonBuilder()
      .setPrettyPrinting()
      .registerTypeAdapter(Date.class, new DateTypeAdapter(DateTimeZone.UTC).nullSafe())
      .create();

  @Test
  public void shouldSerializeDateAsISO8601String() {
    Date exampleDate = new Date(0);
    assertEquals("\"1970-01-01T00:00:00.000Z\"", testGson.toJson(exampleDate));
  }

  @Test
  public void shouldDeserializeDateFromISO8601String() {
    String dateString = "\"1970-01-01T00:00:00.000Z\"";
    assertEquals(new Date(0), testGson.fromJson(dateString, Date.class));
  }

  @Test
  public void shouldSerializeNullDateAsJsonNull() {
    assertEquals("null", testGson.toJson(null, Date.class));
  }

  @Test
  public void shouldDeserializeNullDateStringAsNull() {
    assertEquals(null, testGson.fromJson("null", Date.class));
  }

}
