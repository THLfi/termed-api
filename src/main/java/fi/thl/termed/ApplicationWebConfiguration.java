package fi.thl.termed;

import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Arrays;
import java.util.List;

import fi.thl.termed.util.csv.GsonCsvMessageConverter;
import fi.thl.termed.util.jena.JenaModelMessageConverter;
import fi.thl.termed.util.xml.GsonXmlMessageConverter;
import fi.thl.termed.web.external.node.dto.NodeDtoRdfMessageConverter;

@Configuration
public class ApplicationWebConfiguration extends WebMvcConfigurerAdapter {

  @Autowired
  private Gson gson;

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer config) {
    config.favorParameter(true).favorPathExtension(true);
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
    gsonHttpMessageConverter.setGson(gson);
    converters.addAll(Arrays.asList(
        new JenaModelMessageConverter(),
        new NodeDtoRdfMessageConverter(),
        new GsonXmlMessageConverter(gson),
        new GsonCsvMessageConverter(gson),
        gsonHttpMessageConverter));
    super.configureMessageConverters(converters);
  }

}
