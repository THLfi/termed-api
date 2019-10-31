package fi.thl.termed;

import static fi.thl.termed.util.io.ResourceUtils.resourceToString;
import static fi.thl.termed.util.query.Specifications.matchAll;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.ApplicationReadyEvent;
import fi.thl.termed.domain.event.ApplicationShutdownEvent;
import fi.thl.termed.util.collect.StreamUtils;
import fi.thl.termed.util.service.Service;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * If no users found, adds default user (admin). Adds default properties (defined in
 * src/resources/default/properties.json. Runs full re-indexing if index is empty e.g. in the case
 * of it been deleted
 */
@Component
public class ApplicationBootstrap implements ApplicationListener<ContextRefreshedEvent> {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private EventBus eventBus;

  @Autowired
  private Gson gson;
  @Autowired
  private SecurityProperties security;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private Service<String, User> userService;
  @Autowired
  private Service<String, Property> propertyService;

  private User initializer = User.newSuperuser("initializer", "");

  private Type propertyListType = new TypeToken<List<Property>>() {
  }.getType();

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    eventBus.post(new ApplicationReadyEvent());
    saveDefaultUser();
    saveDefaultProperties();
  }

  private void saveDefaultUser() {
    if (userService.count(matchAll(), initializer) == 0) {
      SecurityProperties.User defaultUser = security.getUser();

      userService.save(
          User.newSuperuser(
              defaultUser.getName(),
              passwordEncoder.encode(defaultUser.getPassword())),
          INSERT, defaultOpts(), initializer);

      log.warn("Created new default SUPERUSER: {}", defaultUser.getName());
    }
  }

  private void saveDefaultProperties() {
    List<Property> propertyList = gson
        .fromJson(resourceToString("default/properties.json"), propertyListType);

    Stream<Property> properties = propertyList.stream();

    Stream<Property> propertiesWithIndices = StreamUtils.zipIndex(
        properties, (p, i) -> Property.builderFromCopyOf(p).index(i).build());

    Stream<Property> modifiedOrMissingProperties =
        propertiesWithIndices.filter(
            p -> !p.equals(propertyService.get(p.getId(), initializer).orElse(null)));

    propertyService.save(modifiedOrMissingProperties, UPSERT, defaultOpts(), initializer);
  }

  @PreDestroy
  public void destroy() {
    eventBus.post(new ApplicationShutdownEvent());
  }

}
