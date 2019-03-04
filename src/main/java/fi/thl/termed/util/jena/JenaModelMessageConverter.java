package fi.thl.termed.util.jena;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import fi.thl.termed.util.rdf.RdfMediaTypes;
import java.io.IOException;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * Spring message converter to parse RDF data into Jena Model.
 */
public class JenaModelMessageConverter extends AbstractHttpMessageConverter<Model> {

  private Map<MediaType, String> mediaTypes = ImmutableMap.<MediaType, String>builder()
      .put(RdfMediaTypes.N_TRIPLES, "N-TRIPLE")
      .put(RdfMediaTypes.RDF_XML, "RDF/XML")
      .put(RdfMediaTypes.LD_JSON, "JSON-LD")
      .put(RdfMediaTypes.TURTLE, "TURTLE")
      .put(RdfMediaTypes.N3, "N3")
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
