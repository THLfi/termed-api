package fi.thl.termed.util;

import com.google.common.base.Function;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * GSON serializer/de-serializer for Multimap<String, LangValue>.
 *
 * Serializes for example multimap {@code { "label": [("fi", "A"), ("fi", "B")] } into json {@code {
 * "label": {"fi": ["A", "B"] }} }
 */
public class LangValueMultimapTypeAdapter extends TypeAdapter<Multimap<String, LangValue>> {

  public static final Type TYPE = new TypeToken<Multimap<String, LangValue>>() {
  }.getType();

  private Function<Collection<LangValue>, Map<String, Collection<String>>> langValuesToMap =
      new Function<Collection<LangValue>, Map<String, Collection<String>>>() {
        public Map<String, Collection<String>> apply(Collection<LangValue> input) {
          Multimap<String, String> langValues = LinkedHashMultimap.create();
          for (LangValue langValue : input) {
            langValues.put(langValue.getLang(), langValue.getValue());
          }
          return langValues.asMap();
        }
      };

  @Override
  public void write(JsonWriter out, Multimap<String, LangValue> langValues) throws IOException {
    Map<String, Map<String, Collection<String>>> langValueMap =
        Maps.transformValues(langValues.asMap(), langValuesToMap);

    out.beginObject();
    for (Map.Entry<String, Map<String, Collection<String>>> outerEntry : langValueMap.entrySet()) {
      out.name(outerEntry.getKey());
      out.beginObject();
      for (Map.Entry<String, Collection<String>> innerEntry : outerEntry.getValue().entrySet()) {
        out.name(innerEntry.getKey());
        out.beginArray();
        for (String value : innerEntry.getValue()) {
          out.value(value);
        }
        out.endArray();
      }
      out.endObject();
    }
    out.endObject();
  }

  @Override
  public Multimap<String, LangValue> read(JsonReader in) throws IOException {
    Multimap<String, LangValue> multimap = LinkedHashMultimap.create();

    in.beginObject();
    while (in.hasNext()) {
      String key = in.nextName();
      in.beginObject();
      while (in.hasNext()) {
        String lang = in.nextName();
        in.beginArray();
        while (in.hasNext()) {
          String value = in.nextString();
          if (value != null) {
            multimap.put(key, new LangValue(lang, value));
          }
        }
        in.endArray();
      }
      in.endObject();
    }
    in.endObject();

    return multimap;
  }

}
