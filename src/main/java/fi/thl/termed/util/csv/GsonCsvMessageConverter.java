package fi.thl.termed.util.csv;

import static com.google.common.base.Charsets.UTF_8;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import fi.thl.termed.util.TableUtils;
import fi.thl.termed.util.json.JsonUtils;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * Converts lists of objects to CSV-table by first converting them to JSON Array (using provided
 * Gson-instance) and then running simple json flattening to each array entry.
 */
public class GsonCsvMessageConverter extends AbstractGenericHttpMessageConverter<List<Object>> {

  private Gson gson;

  public GsonCsvMessageConverter(Gson gson) {
    super(new MediaType("text", "csv", Charsets.UTF_8));
    this.gson = gson;
  }

  @Override
  public boolean canRead(Class<?> cls, MediaType mediaType) {
    return List.class.isAssignableFrom(cls) && canRead(mediaType);
  }

  @Override
  public boolean canWrite(Class<?> cls, MediaType mediaType) {
    return List.class.isAssignableFrom(cls) && canWrite(mediaType);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    // should not be called, since we override canRead/Write instead
    throw new UnsupportedOperationException();
  }

  @Override
  protected List<Object> readInternal(Class<? extends List<Object>> cls, HttpInputMessage input)
      throws IOException, HttpMessageNotReadableException {
    return read(cls, null, input);
  }

  @Override
  public List<Object> read(Type type, Class<?> contextClass, HttpInputMessage input)
      throws IOException, HttpMessageNotReadableException {

    List<String[]> rows = new CSVReader(new InputStreamReader(input.getBody(), UTF_8)).readAll();

    JsonArray jsonArray = new JsonArray();
    for (Map<String, String> row : TableUtils.toMapped(rows)) {
      jsonArray.add(JsonUtils.unflatten(row));
    }

    return gson.fromJson(jsonArray, type);
  }

  @Override
  protected void writeInternal(List<Object> objects, Type type, HttpOutputMessage out)
      throws IOException, HttpMessageNotWritableException {

    List<Map<String, String>> rows = Lists.newArrayList();

    JsonArray jsonArray = gson.toJsonTree(objects, type).getAsJsonArray();
    for (JsonElement element : jsonArray) {
      rows.add(JsonUtils.flatten(element));
    }

    Writer writer = new OutputStreamWriter(out.getBody(), UTF_8);
    new CSVWriter(writer).writeAll(TableUtils.toTable(rows));
    writer.close();
  }

}
