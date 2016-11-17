package fi.thl.termed;

import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Arrays;
import java.util.List;

import fi.thl.termed.util.csv.GsonCsvMessageConverter;
import fi.thl.termed.util.jena.JenaModelMessageConverter;
import fi.thl.termed.util.rdf.RdfMediaTypes;
import fi.thl.termed.util.spring.http.MediaTypes;
import fi.thl.termed.util.xml.GsonXmlMessageConverter;
import fi.thl.termed.web.external.node.dto.NodeDtoListRdfMessageConverter;
import fi.thl.termed.web.external.node.dto.NodeDtoRdfMessageConverter;

@Configuration
public class ApplicationWebConfiguration extends WebMvcConfigurerAdapter {

  @Autowired
  private Gson gson;

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer config) {
    config
        .favorParameter(true)
        .favorPathExtension(true)
        .mediaType("json", MediaType.APPLICATION_JSON_UTF8)
        .mediaType("xml", MediaTypes.TEXT_XML)
        .mediaType("csv", MediaTypes.TEXT_CSV)
        .mediaType("jsonld", RdfMediaTypes.LD_JSON)
        .mediaType("rdf", RdfMediaTypes.RDF_XML)
        .mediaType("ttl", RdfMediaTypes.TURTLE)
        .mediaType("n3", RdfMediaTypes.N3)
        .mediaType("nt", RdfMediaTypes.N_TRIPLES);
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
    gsonHttpMessageConverter.setGson(gson);
    converters.addAll(Arrays.asList(
        new JenaModelMessageConverter(),
        new NodeDtoRdfMessageConverter(),
        new NodeDtoListRdfMessageConverter(),
        new GsonXmlMessageConverter(gson),
        new GsonCsvMessageConverter(gson),
        gsonHttpMessageConverter));
    super.configureMessageConverters(converters);
  }

}
