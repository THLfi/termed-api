package fi.thl.termed.web.scheme;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

/**
 * SchemeService published as a JSON/REST service.
 */
@RestController
@RequestMapping(value = "/api/schemes")
public class SchemeReadController {

  @Autowired
  private Service<UUID, Scheme> schemeService;

  @GetJsonMapping
  public List<Scheme> getSchemes(@AuthenticationPrincipal User user) {
    return schemeService.get(new Query<>(new MatchAll<>()), user);
  }

  @GetJsonMapping(path = "/{schemeId}")
  public Scheme getScheme(@PathVariable("schemeId") UUID schemeId,
                          @AuthenticationPrincipal User user) {
    return schemeService.get(schemeId, user).orElseThrow(NotFoundException::new);
  }

  @GetJsonMapping("/{schemeId}/classes")
  public List<Class> getClasses(@PathVariable("schemeId") UUID schemeId,
                                @AuthenticationPrincipal User currentUser) {
    Scheme scheme = schemeService.get(schemeId, currentUser).orElseThrow(NotFoundException::new);
    return scheme.getClasses();
  }

  @GetJsonMapping("/{schemeId}/classes/{classId}")
  public Class getClass(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("classId") String classId,
      @AuthenticationPrincipal User currentUser) {

    return schemeService.get(schemeId, currentUser).orElseThrow(NotFoundException::new)
        .getClasses().stream()
        .filter(cls -> Objects.equals(cls.getId(), classId))
        .findFirst().orElseThrow(NotFoundException::new);
  }

  @GetJsonMapping("/{schemeId}/classes/{classId}/textAttributes")
  public List<TextAttribute> getTextAttributes(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("classId") String classId,
      @AuthenticationPrincipal User currentUser) {

    return schemeService.get(schemeId, currentUser).orElseThrow(NotFoundException::new)
        .getClasses().stream()
        .filter(cls -> Objects.equals(cls.getId(), classId))
        .findFirst().orElseThrow(NotFoundException::new).getTextAttributes();
  }

  @GetJsonMapping("/{schemeId}/classes/{classId}/textAttributes/{attributeId}")
  public TextAttribute getTextAttribute(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("classId") String classId,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User currentUser) {

    return schemeService.get(schemeId, currentUser).orElseThrow(NotFoundException::new)
        .getClasses().stream()
        .filter(cls -> Objects.equals(cls.getId(), classId))
        .findFirst().orElseThrow(NotFoundException::new)
        .getTextAttributes().stream()
        .filter(attr -> Objects.equals(attr.getId(), attributeId))
        .findFirst().orElseThrow(NotFoundException::new);
  }

  @GetJsonMapping("/{schemeId}/classes/{classId}/referenceAttributes")
  public List<ReferenceAttribute> getReferenceAttributes(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("classId") String classId,
      @AuthenticationPrincipal User currentUser) {
    return schemeService.get(schemeId, currentUser).orElseThrow(NotFoundException::new)
        .getClasses().stream()
        .filter(cls -> Objects.equals(cls.getId(), classId))
        .findFirst().orElseThrow(NotFoundException::new).getReferenceAttributes();
  }

  @GetJsonMapping("/{schemeId}/classes/{classId}/referenceAttributes/{attributeId}")
  public ReferenceAttribute getReferenceAttribute(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("classId") String classId,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User currentUser) {

    return schemeService.get(schemeId, currentUser).orElseThrow(NotFoundException::new)
        .getClasses().stream()
        .filter(cls -> Objects.equals(cls.getId(), classId))
        .findFirst().orElseThrow(NotFoundException::new)
        .getReferenceAttributes().stream()
        .filter(attr -> Objects.equals(attr.getId(), attributeId))
        .findFirst().orElseThrow(NotFoundException::new);
  }

}
