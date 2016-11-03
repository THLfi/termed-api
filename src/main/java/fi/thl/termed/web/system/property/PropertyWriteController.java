package fi.thl.termed.web.system.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/properties")
public class PropertyWriteController {

  private Service<String, Property> propertyService;

  @Autowired
  public PropertyWriteController(Service<String, Property> propertyService) {
    this.propertyService = propertyService;
  }

  @PostJsonMapping(produces = {})
  @ResponseStatus(NO_CONTENT)
  public void post(@RequestBody Property property, @AuthenticationPrincipal User currentUser) {
    propertyService.save(property, currentUser);
  }

  @PutJsonMapping(path = "/{id}", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void put(@PathVariable("id") String id, @RequestBody Property property,
                  @AuthenticationPrincipal User currentUser) {
    property.setId(id);
    propertyService.save(property, currentUser);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(NO_CONTENT)
  public void delete(@PathVariable("id") String id, @AuthenticationPrincipal User user) {
    propertyService.delete(id, user);
  }

}
