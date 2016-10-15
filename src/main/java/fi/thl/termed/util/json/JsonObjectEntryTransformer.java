package fi.thl.termed.util.json;

import com.google.gson.JsonElement;

public interface JsonObjectEntryTransformer {

  JsonElement transformEntry(String key, JsonElement value);

}
