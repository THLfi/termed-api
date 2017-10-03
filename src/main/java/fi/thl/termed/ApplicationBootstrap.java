package fi.thl.termed;

import static fi.thl.termed.util.io.ResourceUtils.resourceToString;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.ApplicationReadyEvent;
import fi.thl.termed.domain.event.ApplicationShutdownEvent;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.Service;
import java.lang.reflect.Type;
import java.util.List;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${security.user.password:}")
  private String defaultPassword;

  @Autowired
  private EventBus eventBus;

  @Autowired
  private Gson gson;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private Service<String, User> userService;
  @Autowired
  private Service<String, Property> propertyService;

  private User initializer = new User("initializer", "", AppRole.SUPERUSER);

  private Type propertyListType = new TypeToken<List<Property>>() {
  }.getType();

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    eventBus.post(new ApplicationReadyEvent());
    saveDefaultUser();
    saveDefaultProperties();
  }

  private void saveDefaultUser() {
    if (!userService.getKeys(initializer).findAny().isPresent()) {
      String password = !defaultPassword.isEmpty() ? defaultPassword : UUIDs.randomUUIDString();
      User admin = new User("admin", passwordEncoder.encode(password), AppRole.ADMIN);
      userService.save(admin, UPSERT, defaultOpts(), initializer);
      log.info("Created new admin user with password: {}", password);
    }
  }

  private void saveDefaultProperties() {
    List<Property> props = gson.fromJson(resourceToString("default/properties.json"),
        propertyListType);
    propertyService.save(props, UPSERT, defaultOpts(), initializer);
  }

  @PreDestroy
  public void destroy() {
    eventBus.post(new ApplicationShutdownEvent());
  }

}
