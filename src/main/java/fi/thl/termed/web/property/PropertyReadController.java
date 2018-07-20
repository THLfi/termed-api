package fi.thl.termed.web.property;

import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.MatchAll;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties")
public class PropertyReadController {

  @Autowired
  private Service<String, Property> propertyService;

  @GetJsonMapping
  public Stream<Property> get(@AuthenticationPrincipal User user) {
    return propertyService.values(new Query<>(new MatchAll<>()), user);
  }

  @GetJsonMapping("/{id}")
  public Property get(@PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return propertyService.get(id, user).orElseThrow(NotFoundException::new);
  }

}
