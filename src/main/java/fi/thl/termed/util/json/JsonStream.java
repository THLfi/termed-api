package fi.thl.termed.util.json;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
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

  public static <T> Stream<T> readArray(Gson gson, Class<T> valueType, InputStream in) {
    JsonReader reader = new JsonReader(new InputStreamReader(in, UTF_8));

    try {
      reader.beginArray();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }

    return Streams.stream(new Iterator<T>() {
      @Override
      public boolean hasNext() {
        try {
          return reader.hasNext();
        } catch (IOException e) {
          throw new JsonIOException(e);
        }
      }

      @Override
      public T next() {
        return gson.fromJson(reader, valueType);
      }
    }).onClose(() -> {
      try {
        reader.endArray();
        reader.close();
      } catch (IOException e) {
        throw new JsonIOException(e);
      }
    });
  }

  public static <T> void writeArray(OutputStream out, Gson gson, Stream<T> values,
      Class<T> valueType) {

    try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, UTF_8));
        Stream<T> closeable = values) {
      writer.setIndent("  ");
      writer.beginArray();
      closeable.forEach(value -> gson.toJson(value, valueType, writer));
      writer.endArray();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

}
