package fi.thl.termed;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;

import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.PropertyRepository;
import fi.thl.termed.repository.UserRepository;
import fi.thl.termed.util.ResourceUtils;

@Component
public class ApplicationBootstrap implements ApplicationListener<ContextRefreshedEvent> {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PropertyRepository propertyRepository;

  @Autowired
  private Gson gson;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    saveDefaultUser();
    saveDefaultProperties();
  }

  private void saveDefaultUser() {
    userRepository.save(new User("admin", new BCryptPasswordEncoder().encode("admin"), "ADMIN"));
  }

  private void saveDefaultProperties() {
    Type propertyListType = new TypeToken<List<Property>>() {
    }.getType();

    List<Property> properties = gson.fromJson(ResourceUtils.getResourceToString("properties.json"),
                                              propertyListType);

    int index = 0;
    for (Property property : properties) {
      property.setIndex(index++);
    }

    propertyRepository.save(properties);
  }

}
