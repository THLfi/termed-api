package fi.thl.termed;

import com.google.gson.Gson;
import fi.thl.termed.util.jena.JenaModelMessageConverter;
import fi.thl.termed.util.rdf.RdfMediaTypes;
import fi.thl.termed.util.spring.http.MediaTypes;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApplicationWebConfiguration implements WebMvcConfigurer {

  @Autowired
  private Gson gson;

  @Override
  public void configurePathMatch(PathMatchConfigurer config) {
    config.setUseSuffixPatternMatch(true);
  }

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

  @Bean
  public HttpMessageConverters httpMessageConverters() {
    return new HttpMessageConverters(
        new JenaModelMessageConverter(),
        new GsonHttpMessageConverter(gson));
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    // don't split query strings by commas
    registry.removeConvertible(String.class, Collection.class);
  }

}
