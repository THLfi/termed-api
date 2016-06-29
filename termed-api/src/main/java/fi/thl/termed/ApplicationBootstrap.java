package fi.thl.termed;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.Resource;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.util.ResourceUtils;
import fi.thl.termed.util.UUIDs;

@Component
public class ApplicationBootstrap implements ApplicationListener<ContextRefreshedEvent> {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Resource
  private Repository<String, User> userRepository;
  @Resource
  private Repository<String, Property> propertyRepository;
  @Resource
  private Gson gson;
  @Resource
  private PasswordEncoder passwordEncoder;

  @Value("${security.user.password:}")
  private String defaultPassword;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    saveDefaultUser();
    saveDefaultProperties();
  }

  private void saveDefaultUser() {
    if (userRepository.get().isEmpty()) {
      String password = !defaultPassword.isEmpty() ? defaultPassword : UUIDs.randomUUIDString();
      userRepository.save(new User("admin", passwordEncoder.encode(password), AppRole.ADMIN));
      log.info("Created new admin user with password: {}", password);
    }
  }

  private void saveDefaultProperties() {
    Type propertyListType = new TypeToken<List<Property>>() {
    }.getType();

    List<Property> properties = gson.fromJson(ResourceUtils.getResourceToString(
        "default/properties.json"), propertyListType);

    int index = 0;
    for (Property property : properties) {
      property.setIndex(index++);
    }

    propertyRepository.save(properties);
  }

}
