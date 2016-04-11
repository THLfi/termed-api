package fi.thl.termed.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LangValueMultimapTypeAdapterTest {

  private Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new MultimapTypeAdapterFactory())
      .registerTypeAdapter(LangValueMultimapTypeAdapter.TYPE, new LangValueMultimapTypeAdapter())
      .setPrettyPrinting()
      .create();

  @Test
  public void shouldSerializeAndDeserializeLangValueMultimap() {
    Multimap<String, LangValue> langValues = LinkedHashMultimap.create();
    langValues.put("a", new LangValue("fi", "v1"));
    langValues.put("a", new LangValue("en", "v2"));
    langValues.put("b", new LangValue("en", "v3"));
    langValues.put("b", new LangValue("en", "v4"));

    JsonElement jsonLangValues = new JsonParser().parse(
        "{ a: { fi: ['v1'], en: ['v2'] }, b: { en: ['v3', 'v4'] }}");

    assertEquals(jsonLangValues, gson.toJsonTree(langValues, LangValueMultimapTypeAdapter.TYPE));
    assertEquals(langValues, gson.fromJson(jsonLangValues, LangValueMultimapTypeAdapter.TYPE));
  }

}
