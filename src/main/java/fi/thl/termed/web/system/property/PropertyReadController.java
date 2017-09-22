package fi.thl.termed.web.system.property;

import static java.util.stream.Collectors.toList;

import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties")
public class PropertyReadController {

  private Service<String, Property> propertyService;

  @Autowired
  public PropertyReadController(Service<String, Property> propertyService) {
    this.propertyService = propertyService;
  }

  @GetJsonMapping
  public List<Property> get(@AuthenticationPrincipal User user) {
    return propertyService.getValues(user).collect(toList());
  }

  @GetJsonMapping("/{id}")
  public Property get(@PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return propertyService.get(id, user).orElseThrow(NotFoundException::new);
  }

}
