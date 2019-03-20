package fi.thl.termed.util.jena;

import static org.apache.jena.riot.RDFFormat.RDFXML_PLAIN;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import fi.thl.termed.util.rdf.RdfMediaTypes;
import java.io.IOException;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
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

  private final Map<MediaType, RDFFormat> mediaTypes = ImmutableMap.<MediaType, RDFFormat>builder()
      .put(RdfMediaTypes.N_TRIPLES, RDFFormat.NTRIPLES)
      .put(RdfMediaTypes.RDF_XML, RDFFormat.RDFXML_PLAIN)
      .put(RdfMediaTypes.LD_JSON, RDFFormat.JSONLD)
      .put(RdfMediaTypes.TURTLE, RDFFormat.TURTLE)
      .put(RdfMediaTypes.N3, RDFFormat.TURTLE)
      .build();

  public JenaModelMessageConverter() {
    setSupportedMediaTypes(Lists.newArrayList(mediaTypes.keySet()));
  }

  @Override
  protected boolean supports(Class<?> cls) {
    return Model.class.isAssignableFrom(cls);
  }

  @Override
  protected Model readInternal(Class<? extends Model> cls, HttpInputMessage input)
      throws IOException, HttpMessageNotReadableException {
    Model model = ModelFactory.createDefaultModel();
    RDFFormat format = mediaTypes.getOrDefault(input.getHeaders().getContentType(), RDFXML_PLAIN);
    RDFDataMgr.read(model, input.getBody(), null, format.getLang());
    return model;
  }

  @Override
  protected void writeInternal(Model model, HttpOutputMessage output)
      throws IOException, HttpMessageNotWritableException {
    RDFFormat format = mediaTypes.getOrDefault(output.getHeaders().getContentType(), RDFXML_PLAIN);
    RDFDataMgr.write(output.getBody(), model, format);
  }

}
