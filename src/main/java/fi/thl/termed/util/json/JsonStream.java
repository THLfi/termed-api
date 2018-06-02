package fi.thl.termed.util.json;

import static com.google.common.collect.Iterators.transform;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.JsonStreamParser;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.stream.Stream;

public final class JsonStream {

  private JsonStream() {
  }

  public static <T> Stream<T> read(Gson gson, Class<T> valueType, InputStream in) {
    JsonStreamParser parser = new JsonStreamParser(new InputStreamReader(in, UTF_8));

    Iterator<T> iterator = transform(parser, input -> gson.fromJson(parser.next(), valueType));

    return Streams.stream(iterator).onClose(() -> {
      try {
        in.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public static <T> void write(OutputStream out, Gson gson, Stream<T> values, Class<T> valueType)
      throws IOException {

    try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, UTF_8));
        Stream<T> closeable = values) {
      writer.setIndent("  ");
      writer.beginArray();
      closeable.forEach(value -> gson.toJson(value, valueType, writer));
      writer.endArray();
    }
  }

}
