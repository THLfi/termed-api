package fi.thl.termed;

import com.google.gson.Gson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import fi.thl.termed.util.rdf.JenaModelMessageConverter;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public HttpMessageConverters messageConverters(Gson gson) {
    GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
    gsonHttpMessageConverter.setGson(gson);
    JenaModelMessageConverter jenaModelMessageConverter = new JenaModelMessageConverter();
    return new HttpMessageConverters(gsonHttpMessageConverter, jenaModelMessageConverter);
  }

}
