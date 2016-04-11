package fi.thl.termed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import java.util.Date;

import fi.thl.termed.util.DateTypeAdapter;
import fi.thl.termed.util.MultimapTypeAdapterFactory;
import fi.thl.termed.util.rdf.JenaModelMessageConverter;
import fi.thl.termed.web.JsonResponseFilter;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  @Autowired
  public HttpMessageConverters messageConverters(Gson gson) {
    GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
    gsonHttpMessageConverter.setGson(gson);
    JenaModelMessageConverter jenaModelMessageConverter = new JenaModelMessageConverter();
    return new HttpMessageConverters(gsonHttpMessageConverter, jenaModelMessageConverter);
  }

  @Bean
  public Gson gson() {
    return new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Date.class, new DateTypeAdapter().nullSafe())
        .registerTypeAdapterFactory(new MultimapTypeAdapterFactory())
        .create();
  }

  @Profile("dev")
  @Bean
  public FilterRegistrationBean jsonResponseFilter() {
    FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(new JsonResponseFilter());
    return registration;
  }

}
