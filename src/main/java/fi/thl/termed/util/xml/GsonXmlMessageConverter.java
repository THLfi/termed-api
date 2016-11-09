package fi.thl.termed.util.xml;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;

import static com.google.common.base.Charsets.UTF_8;

/**
 * Converts objects to XML by first converting them to JSON (using provided Gson-instance) and then
 * running a simple JSON to XML conversion.
 */
public class GsonXmlMessageConverter extends AbstractGenericHttpMessageConverter<Object> {

  private Gson gson;

  public GsonXmlMessageConverter(Gson gson) {
    super(MediaType.APPLICATION_XML);
    this.gson = gson;
  }

  @Override
  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    return canRead(mediaType);
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return canWrite(mediaType);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    // should not be called, since we override canRead/Write instead
    throw new UnsupportedOperationException();
  }

  @Override
  protected Object readInternal(Class<?> cls, HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {
    return read(cls, null, inputMessage);
  }

  @Override
  public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {
    Document doc = XmlUtils.parseDocument(new InputStreamReader(inputMessage.getBody(), UTF_8));
    JsonElement jsonElement = new XmlToJsonConverter().apply(doc);
    return gson.fromJson(jsonElement, type);
  }

  @Override
  protected void writeInternal(Object o, Type type, HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {
    JsonElement jsonElement = type != null ? gson.toJsonTree(o, type) : gson.toJsonTree(o);
    Document doc = new JsonToXmlConverter().apply(jsonElement);
    XmlUtils.prettyPrint(doc, new OutputStreamWriter(outputMessage.getBody(), UTF_8));
  }

}
