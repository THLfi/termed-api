package fi.thl.termed.web;

import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.annotation.Resource;

import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class PropertyController {

  @Resource
  private Service<String, Property> propertyService;

  @RequestMapping(method = GET, value = "/properties")
  public List<Property> get(@AuthenticationPrincipal User user) {
    return propertyService.get(user);
  }

  @RequestMapping(method = GET, value = "/properties/{id}")
  public Property get(@PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return propertyService.get(id, user);
  }

  @RequestMapping(method = POST, value = "/properties")
  public Property save(@RequestBody Property property, @AuthenticationPrincipal User currentUser) {
    return propertyService.save(property, currentUser);
  }

  @RequestMapping(method = PUT, value = "/properties/{id}")
  public Property save(@PathVariable("id") String id,
                       @RequestBody Property property,
                       @AuthenticationPrincipal User currentUser) {
    property.setId(id);
    return propertyService.save(property, currentUser);
  }

  @RequestMapping(method = DELETE, value = "/properties/{id}")
  public void delete(@PathVariable("id") String id, @AuthenticationPrincipal User user) {
    propertyService.delete(id, user);
  }

}
