package fi.thl.termed.web.property;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties")
public class PropertyWriteController {

  @Autowired
  private Service2<String, Property> propertyService;

  @PostJsonMapping(produces = {})
  @ResponseStatus(NO_CONTENT)
  public void post(@RequestBody Property property,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User currentUser) {
    propertyService.save(property, saveMode(mode), opts(sync), currentUser);
  }

  @PutJsonMapping(path = "/{id}", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void put(@PathVariable("id") String id, @RequestBody Property property,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User currentUser) {
    property = Property.builder().id(id).copyOptionalsFrom(property).build();
    propertyService.save(property, saveMode(mode), opts(sync), currentUser);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(NO_CONTENT)
  public void delete(@PathVariable("id") String id,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    propertyService.delete(id, opts(sync), user);
  }

}
