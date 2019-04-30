package fi.thl.termed.util.json;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.stream.JsonWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public final class JsonWriters {

  private JsonWriters() {
  }

  public static JsonWriter from(OutputStream out, boolean pretty) {
    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(out, UTF_8));

    if (pretty) {
      jsonWriter.setIndent("  ");
    }

    return jsonWriter;
  }

}
