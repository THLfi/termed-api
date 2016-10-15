package fi.thl.termed.web;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.util.TrueSpecification;

import static com.google.common.collect.Iterables.tryFind;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * SchemeService published as a JSON/REST service.
 */
@RequestMapping(value = "/api/schemes")
public class SchemeController {

  private Service<UUID, Scheme> schemeService;

  public SchemeController(Service<UUID, Scheme> schemeService) {
    this.schemeService = schemeService;
  }

  @RequestMapping(method = GET, produces = "application/json;charset=UTF-8")
  @ResponseBody
  public List<Scheme> get(@AuthenticationPrincipal User user) {
    return schemeService.get(
        new SpecificationQuery<UUID, Scheme>(new TrueSpecification<UUID, Scheme>()), user);
  }

  @RequestMapping(method = GET, value = "/{schemeId}", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public Scheme get(@PathVariable("schemeId") UUID schemeId,
                    @AuthenticationPrincipal User user) {
    return schemeService.get(schemeId, user).orNull();
  }

  @RequestMapping(method = GET, value = "/{schemeId}/classes/{classId}",
      produces = "application/json;charset=UTF-8")
  @ResponseBody
  public Class getClass(@PathVariable("schemeId") UUID schemeId,
                        @PathVariable("classId") String classId,
                        @AuthenticationPrincipal User user) {
    Optional<Scheme> o = schemeService.get(schemeId, user);
    return o.isPresent() ? Iterables.find(o.get().getClasses(), new ClassIdMatches(classId)) : null;
  }

  @RequestMapping(method = GET, value = "/{schemeId}/classes/{classId}/textAttributes",
      produces = "application/json;charset=UTF-8")
  @ResponseBody
  public List<TextAttribute> getTextAttributes(@PathVariable("schemeId") UUID schemeId,
                                               @PathVariable("classId") String classId,
                                               @AuthenticationPrincipal User user) {
    Optional<Scheme> scheme = schemeService.get(schemeId, user);
    Optional<Class> cls = scheme.isPresent()
                          ? tryFind(scheme.get().getClasses(), new ClassIdMatches(classId))
                          : Optional.<Class>absent();
    return cls.isPresent() ? cls.get().getTextAttributes()
                           : Lists.<TextAttribute>newArrayList();
  }

  @RequestMapping(method = GET, value = "/{schemeId}/classes/{classId}/referenceAttributes",
      produces = "application/json;charset=UTF-8")
  @ResponseBody
  public List<ReferenceAttribute> getReferenceAttributes(@PathVariable("schemeId") UUID schemeId,
                                                         @PathVariable("classId") String classId,
                                                         @AuthenticationPrincipal User user) {
    Optional<Scheme> scheme = schemeService.get(schemeId, user);
    Optional<Class> cls = scheme.isPresent()
                          ? tryFind(scheme.get().getClasses(), new ClassIdMatches(classId))
                          : Optional.<Class>absent();
    return cls.isPresent() ? cls.get().getReferenceAttributes()
                           : Lists.<ReferenceAttribute>newArrayList();
  }

  @RequestMapping(method = POST, consumes = "application/json;charset=UTF-8",
      produces = "application/json;charset=UTF-8")
  @ResponseBody
  public Scheme save(@RequestBody Scheme scheme,
                     @AuthenticationPrincipal User user) {
    UUID schemeId = schemeService.save(scheme, user);
    return schemeService.get(schemeId, user).get();
  }

  @RequestMapping(method = PUT, value = "/{schemeId}", consumes = "application/json;charset=UTF-8")
  @ResponseBody
  public Scheme save(@PathVariable("schemeId") UUID schemeId,
                     @RequestBody Scheme scheme,
                     @AuthenticationPrincipal User user) {
    scheme.setId(schemeId);
    schemeService.save(scheme, user);
    return schemeService.get(schemeId, user).get();
  }

  @RequestMapping(method = DELETE, value = "/{schemeId}")
  @ResponseStatus(NO_CONTENT)
  public void delete(@PathVariable("schemeId") UUID schemeId,
                     @AuthenticationPrincipal User user) {
    schemeService.delete(schemeId, user);
  }

  private class ClassIdMatches implements Predicate<Class> {

    private String id;

    public ClassIdMatches(String id) {
      this.id = id;
    }

    @Override
    public boolean apply(Class cls) {
      return Objects.equal(cls.getId(), id);
    }

  }

}
