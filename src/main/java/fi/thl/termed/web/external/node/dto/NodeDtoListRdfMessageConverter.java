package fi.thl.termed.web.external.node.dto;

import com.google.common.reflect.TypeToken;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import fi.thl.termed.domain.NodeDto;
import fi.thl.termed.util.rdf.RdfMediaTypes;

import static com.google.common.base.Charsets.UTF_8;

/**
 * Converts lists of node dto objects to RDF
 */
public class NodeDtoListRdfMessageConverter
    extends AbstractGenericHttpMessageConverter<List<NodeDto>> {

  private Type nodeDtoListType = new TypeToken<List<NodeDto>>() {
  }.getType();

  public NodeDtoListRdfMessageConverter() {
    super(RdfMediaTypes.N_TRIPLES,
          RdfMediaTypes.RDF_XML,
          RdfMediaTypes.LD_JSON,
          RdfMediaTypes.TURTLE,
          RdfMediaTypes.N3);
  }

  @Override
  public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
    return nodeDtoListType.equals(type) && canRead(mediaType);
  }

  @Override
  public boolean canWrite(Type type, Class<?> contextClass, MediaType mediaType) {
    return nodeDtoListType.equals(type) && canRead(mediaType);
  }

  @Override
  protected boolean supports(Class<?> cls) {
    // should not be called, since we override canRead/Write instead
    throw new UnsupportedOperationException();
  }

  @Override
  protected List<NodeDto> readInternal(Class<? extends List<NodeDto>> cls, HttpInputMessage input)
      throws IOException, HttpMessageNotReadableException {
    return read(cls, null, input);
  }

  @Override
  public List<NodeDto> read(Type type, Class<?> contextClass, HttpInputMessage input)
      throws IOException, HttpMessageNotReadableException {

    Optional<MediaType> mediaType = input.getHeaders().getAccept().stream().findFirst();
    String lang = mediaType.isPresent() ? mediaTypeToLang(mediaType.get()).getLabel() : "TTL";

    Model model = ModelFactory.createDefaultModel();
    model.read(new InputStreamReader(input.getBody(), UTF_8), "", lang);

    return new JenaModelToNodeDtoList().apply(model);
  }

  @Override
  protected void writeInternal(List<NodeDto> nodes, Type type, HttpOutputMessage output)
      throws IOException, HttpMessageNotWritableException {

    Optional<MediaType> mediaType = Optional.of(output.getHeaders().getContentType());
    String lang = mediaType.isPresent() ? mediaTypeToLang(mediaType.get()).getLabel() : "TTL";

    Model model = new NodeDtoListToJenaModel().apply(nodes);

    Writer writer = new OutputStreamWriter(output.getBody(), UTF_8);
    model.write(writer, lang, "");
    writer.close();
  }

  private Lang mediaTypeToLang(MediaType mediaType) {
    return RDFLanguages.contentTypeToLang(ContentType.create(mediaType.toString()));
  }

}
