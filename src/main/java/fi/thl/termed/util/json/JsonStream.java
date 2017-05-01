package fi.thl.termed.util.json;

import static com.google.common.collect.Iterators.transform;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

import com.google.gson.Gson;
import com.google.gson.JsonStreamParser;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;

public final class JsonStream {

  private JsonStream() {
  }

  public static <T> Stream<T> read(Gson gson, Class<T> valueType, InputStream in)
      throws IOException {

    JsonStreamParser parser = new JsonStreamParser(new InputStreamReader(in, UTF_8));

    Iterator<T> iterator = transform(parser, input -> gson.fromJson(parser.next(), valueType));

    return stream(spliteratorUnknownSize(iterator, Spliterator.ORDERED), false).onClose(() -> {
      try {
        in.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public static <T> void write(OutputStream out, Gson gson, Stream<T> values, Class<T> valueType)
      throws IOException {

    try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, UTF_8))) {
      writer.setIndent("  ");
      writer.beginArray();
      values.forEach(value -> gson.toJson(value, valueType, writer));
      writer.endArray();
    }
  }

}
