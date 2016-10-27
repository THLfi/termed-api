package fi.thl.termed.web.class_;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * SchemeService published as a JSON/REST service.
 */
@RestController
@RequestMapping(value = "/api/schemes/{schemeId}/classes")
public class ClassWriteController {

  @Autowired
  private Service<SchemeId, Scheme> schemeService;

  @Autowired
  private Service<ClassId, Class> classService;

  @PostJsonMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Class save(
      @PathVariable("schemeId") UUID schemeId,
      @RequestBody Class cls,
      @AuthenticationPrincipal User user) {
    schemeService.get(new SchemeId(schemeId), user).orElseThrow(NotFoundException::new);
    cls.setScheme(new SchemeId(schemeId));
    return classService.get(classService.save(cls, user), user).orElseThrow(NotFoundException::new);
  }

  @PostJsonMapping(params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void save(
      @PathVariable("schemeId") UUID schemeId,
      @RequestBody List<Class> classes,
      @AuthenticationPrincipal User currentUser) {
    classes.forEach(cls -> cls.setScheme(new SchemeId(schemeId)));
    classService.save(classes, currentUser);
  }

  @PutJsonMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Class save(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("id") String id,
      @RequestBody Class cls,
      @AuthenticationPrincipal User user) {
    cls.setScheme(new SchemeId(schemeId));
    cls.setId(id);
    return classService.get(classService.save(cls, user), user).orElseThrow(NotFoundException::new);
  }

  @DeleteMapping(path = "/{id}")
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("id") String id,
      @AuthenticationPrincipal User user) {
    classService.delete(new ClassId(id, schemeId), user);
  }

}
