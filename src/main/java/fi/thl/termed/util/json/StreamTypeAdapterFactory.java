package fi.thl.termed.util.json;

import static fi.thl.termed.util.collect.FunctionUtils.toUncheckedConsumer;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;

/**
 * GSON serializer/de-serializer for Stream<T>.
 */
public class StreamTypeAdapterFactory implements TypeAdapterFactory {

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
    if (!Stream.class.isAssignableFrom(typeToken.getRawType())) {
      return null;
    }

    Type type = typeToken.getType();
    Type valueType = (type instanceof ParameterizedType) ?
        ((ParameterizedType) type).getActualTypeArguments()[0] : Object.class;

    @SuppressWarnings("unchecked")
    TypeAdapter<T> result = new StreamAdapter(gson.getAdapter(TypeToken.get(valueType))).nullSafe();

    return result;
  }

  private final class StreamAdapter<T> extends TypeAdapter<Stream<T>> {

    private TypeAdapter<T> valueAdapter;

    private StreamAdapter(TypeAdapter<T> valueAdapter) {
      this.valueAdapter = valueAdapter;
    }

    @Override
    public Stream<T> read(JsonReader in) throws IOException {
      Stream.Builder<T> builder = Stream.builder();

      in.beginArray();
      while (in.hasNext()) {
        builder.add(valueAdapter.read(in));
      }
      in.endArray();

      return builder.build();
    }

    @Override
    public void write(JsonWriter out, Stream<T> stream) throws IOException {
      out.beginArray();

      try (Stream<T> closable = stream) {
        closable.forEach(toUncheckedConsumer(v -> valueAdapter.write(out, v)));
      }

      out.endArray();
    }

  }

}
