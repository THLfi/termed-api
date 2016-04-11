package fi.thl.termed.util.rdf;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.util.Map;

/**
 * Spring message converter to parse RDF data into Jena Model.
 */
public class JenaModelMessageConverter extends AbstractHttpMessageConverter<Model> {

  private Map<MediaType, String> mediaTypes = ImmutableMap.<MediaType, String>builder()
      .put(new MediaType("application", "n-triples", Charsets.UTF_8), "N-TRIPLE")
      .put(new MediaType("application", "rdf+xml", Charsets.UTF_8), "RDF/XML")
      .put(new MediaType("text", "turtle", Charsets.UTF_8), "TURTLE")
      .put(new MediaType("text", "n3", Charsets.UTF_8), "N3")
      .build();

  public JenaModelMessageConverter() {
    setSupportedMediaTypes(Lists.newArrayList(mediaTypes.keySet()));
  }

  @Override
  protected boolean supports(Class<?> cls) {
    return Model.class.isAssignableFrom(cls);
  }

  @Override
  protected Model readInternal(Class<? extends Model> cls, HttpInputMessage httpInputMessage)
      throws IOException, HttpMessageNotReadableException {

    Model model = ModelFactory.createDefaultModel();
    return model.read(httpInputMessage.getBody(), null,
                      mediaTypes.get(httpInputMessage.getHeaders().getContentType()));
  }

  @Override
  protected void writeInternal(Model model, HttpOutputMessage httpOutputMessage)
      throws IOException, HttpMessageNotWritableException {

    model.write(httpOutputMessage.getBody(),
                mediaTypes.get(httpOutputMessage.getHeaders().getContentType()));
  }

}
