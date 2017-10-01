package fi.thl.termed.util.json;

import com.google.common.collect.ImmutableMultimap;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Adds GSON support for serializing and de-serializing Guava ImmutableMultimap. Serializes e.g.
 * ImmutableMultimap<String, Integer> as e.g. { 'foo': [1, 2, 3], 'bar': [2, 9] }.
 */
public class ImmutableMultimapTypeAdapterFactory implements TypeAdapterFactory {

  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {

    if (!ImmutableMultimap.class.isAssignableFrom(typeToken.getRawType())) {
      return null;
    }

    Type type = typeToken.getType();

    Type[] keyAndValueTypes = (type instanceof ParameterizedType) ?
        ((ParameterizedType) type).getActualTypeArguments() :
        new Type[]{Object.class, Object.class};

    @SuppressWarnings("unchecked")
    TypeAdapter<T> result = new Adapter(
        gson.getAdapter(TypeToken.get(keyAndValueTypes[0])),
        gson.getAdapter(TypeToken.get(keyAndValueTypes[1]))).nullSafe();

    return result;
  }

  private final class Adapter<K, V> extends TypeAdapter<ImmutableMultimap<K, V>> {

    private TypeAdapter<K> keyAdapter;
    private TypeAdapter<V> valueAdapter;

    public Adapter(TypeAdapter<K> keyAdapter, TypeAdapter<V> valueAdapter) {
      this.keyAdapter = keyAdapter;
      this.valueAdapter = valueAdapter;
    }

    @Override
    public ImmutableMultimap<K, V> read(JsonReader in) throws IOException {
      ImmutableMultimap.Builder<K, V> builder = ImmutableMultimap.builder();

      in.beginObject();
      while (in.hasNext()) {
        JsonReaderInternalAccess.INSTANCE.promoteNameToValue(in);
        K name = keyAdapter.read(in);
        in.beginArray();
        while (in.hasNext()) {
          V value = valueAdapter.read(in);
          if (value != null) {
            builder.put(name, value);
          }
        }
        in.endArray();
      }
      in.endObject();

      return builder.build();
    }

    @Override
    public void write(JsonWriter out, ImmutableMultimap<K, V> multimap) throws IOException {
      out.beginObject();
      for (Map.Entry<K, Collection<V>> entry : multimap.asMap().entrySet()) {
        out.name(String.valueOf(entry.getKey()));
        out.beginArray();
        for (V value : entry.getValue()) {
          valueAdapter.write(out, value);
        }
        out.endArray();
      }
      out.endObject();
    }
  }

}
