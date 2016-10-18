package fi.thl.termed;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.event.ApplicationReadyEvent;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.io.ResourceUtils;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.MatchAll;

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
  private Service<String, User> userComponent;
  @Autowired
  private Service<String, Property> propertyComponent;

  private User initializer = new User("initializer", "", AppRole.SUPERUSER);

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    eventBus.post(new ApplicationReadyEvent());
    saveDefaultUser();
    saveDefaultProperties();
  }

  private void saveDefaultUser() {
    if (userComponent.get(new Query<>(new MatchAll<>()), initializer).isEmpty()) {
      String password = !defaultPassword.isEmpty() ? defaultPassword : UUIDs.randomUUIDString();
      User admin = new User("admin", passwordEncoder.encode(password), AppRole.ADMIN);
      userComponent.save(admin, initializer);
      log.info("Created new admin user with password: {}", password);
    }
  }

  private void saveDefaultProperties() {
    List<Property> properties = gson.fromJson(ResourceUtils.getResourceToString(
        "default/properties.json"), new TypeToken<List<Property>>() {
    }.getType());
    int index = 0;
    for (Property property : properties) {
      property.setIndex(index++);
    }
    propertyComponent.save(properties, initializer);
  }

}
