package fi.thl.termed;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.thl.termed.util.json.DateTypeAdapter;
import fi.thl.termed.util.json.FastUUIDTypeAdapter;
import fi.thl.termed.util.json.ImmutableListDeserializer;
import fi.thl.termed.util.json.ImmutableMultimapTypeAdapterFactory;
import fi.thl.termed.util.json.MultimapTypeAdapterFactory;
import fi.thl.termed.util.json.StreamTypeAdapterFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public Gson gson() {
    return new GsonBuilder().setPrettyPrinting()
        .registerTypeAdapter(UUID.class, new FastUUIDTypeAdapter().nullSafe())
        .registerTypeAdapter(Date.class, new DateTypeAdapter().nullSafe())
        .registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
        .registerTypeAdapterFactory(new StreamTypeAdapterFactory())
        .registerTypeAdapterFactory(new MultimapTypeAdapterFactory())
        .registerTypeAdapterFactory(new ImmutableMultimapTypeAdapterFactory())
        .create();
  }

  @Bean
  public EventBus eventBus() {
    return new EventBus();
  }

  @Bean
  @ConfigurationProperties(prefix = "fi.thl.termed.nsprefixes")
  public Map<String, String> defaultNamespacePrefixes() {
    return new HashMap<>();
  }

}
