package fi.thl.termed.web.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

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
    return propertyService.get(new Query<>(new MatchAll<>()), user).getValues();
  }

  @GetJsonMapping("/{id}")
  public Property get(@PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return propertyService.get(id, user).orElseThrow(NotFoundException::new);
  }

}
