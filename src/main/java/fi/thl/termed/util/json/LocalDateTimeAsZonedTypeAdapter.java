package fi.thl.termed.util.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * GSON serializer/de-serializer for LocalDateTime in ISO format. Serializes date in format like
 * '2011-12-03T10:15:30+01:00'. Offset is determined by system default timezone.
 */
public class LocalDateTimeAsZonedTypeAdapter extends TypeAdapter<LocalDateTime> {

  @Override
  public void write(JsonWriter out, LocalDateTime date) throws IOException {
    out.value(
        date.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
  }

  @Override
  public LocalDateTime read(JsonReader in) throws IOException {
    return ZonedDateTime
        .parse(in.nextString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        .toLocalDateTime();
  }

}
