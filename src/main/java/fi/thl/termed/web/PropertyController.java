package fi.thl.termed.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.util.TrueSpecification;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RequestMapping(value = "/api/properties")
public class PropertyController {

  private Service<String, Property> propertyService;

  public PropertyController(Service<String, Property> propertyService) {
    this.propertyService = propertyService;
  }

  @RequestMapping(method = GET, produces = "application/json;charset=UTF-8")
  @ResponseBody
  public List<Property> get(@AuthenticationPrincipal User user) {
    return propertyService.get(
        new SpecificationQuery<String, Property>(new TrueSpecification<String, Property>()), user);
  }

  @RequestMapping(method = GET, value = "/{id}", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public Property get(@PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return propertyService.get(id, user).orNull();
  }

  @RequestMapping(method = POST, consumes = "application/json;charset=UTF-8")
  @ResponseStatus(NO_CONTENT)
  public void save(@RequestBody Property property, @AuthenticationPrincipal User currentUser) {
    propertyService.save(property, currentUser);
  }

  @RequestMapping(method = PUT, value = "/{id}", consumes = "application/json;charset=UTF-8")
  @ResponseStatus(NO_CONTENT)
  public void save(@PathVariable("id") String id,
                   @RequestBody Property property,
                   @AuthenticationPrincipal User currentUser) {
    property.setId(id);
    propertyService.save(property, currentUser);
  }

  @RequestMapping(method = DELETE, value = "/{id}")
  @ResponseStatus(NO_CONTENT)
  public void delete(@PathVariable("id") String id, @AuthenticationPrincipal User user) {
    propertyService.delete(id, user);
  }

}
