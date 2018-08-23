package fi.thl.termed.util.json;

import com.eatthepath.uuid.FastUUID;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.UUID;

public class FastUUIDTypeAdapter extends TypeAdapter<UUID> {

  @Override
  public void write(JsonWriter jsonWriter, UUID uuid) throws IOException {
    jsonWriter.value(FastUUID.toString(uuid));
  }

  @Override
  public UUID read(JsonReader jsonReader) throws IOException {
    return FastUUID.parseUUID(jsonReader.nextString());
  }

}
