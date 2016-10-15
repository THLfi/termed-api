package fi.thl.termed.util.json;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
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
 * Adds GSON support for serializing and de-serializing Guava Multimaps. Serializes e.g.
 * Multimap<String, Integer> as e.g. { 'foo': [1, 2, 3], 'bar': [2, 9] }.
 */
public class MultimapTypeAdapterFactory implements TypeAdapterFactory {

  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {

    if (!Multimap.class.isAssignableFrom(typeToken.getRawType())) {
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

  private final class Adapter<K, V> extends TypeAdapter<Multimap<K, V>> {

    private TypeAdapter<K> keyAdapter;
    private TypeAdapter<V> valueAdapter;

    public Adapter(TypeAdapter<K> keyAdapter, TypeAdapter<V> valueAdapter) {
      this.keyAdapter = keyAdapter;
      this.valueAdapter = valueAdapter;
    }

    @Override
    public Multimap<K, V> read(JsonReader in) throws IOException {
      Multimap<K, V> multimap = MultimapBuilder.linkedHashKeys().arrayListValues().build();

      in.beginObject();
      while (in.hasNext()) {
        JsonReaderInternalAccess.INSTANCE.promoteNameToValue(in);
        K name = keyAdapter.read(in);
        in.beginArray();
        while (in.hasNext()) {
          V value = valueAdapter.read(in);
          if (value != null) {
            multimap.put(name, value);
          }
        }
        in.endArray();
      }
      in.endObject();
      return multimap;
    }

    @Override
    public void write(JsonWriter out, Multimap<K, V> multimap) throws IOException {
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
