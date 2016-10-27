package fi.thl.termed.web.class_;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.class_.specification.ClassesBySchemeId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

/**
 * SchemeService published as a JSON/REST service.
 */
@RestController
@RequestMapping(value = "/api")
public class ClassReadController {

  @Autowired
  private Service<SchemeId, Scheme> schemeService;

  @Autowired
  private Service<ClassId, Class> classService;

  @GetJsonMapping("/classes")
  public List<Class> getClasses(
      @AuthenticationPrincipal User currentUser) {
    return classService.get(new Query<>(new MatchAll<>()), currentUser);
  }

  @GetJsonMapping("/schemes/{schemeId}/classes")
  public List<Class> getClasses(
      @PathVariable("schemeId") UUID schemeId,
      @AuthenticationPrincipal User currentUser) {
    schemeService.get(new SchemeId(schemeId), currentUser).orElseThrow(NotFoundException::new);
    return classService.get(new Query<>(new ClassesBySchemeId(schemeId)), currentUser);
  }

  @GetJsonMapping("/schemes/{schemeId}/classes/{classId}")
  public Class getClass(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("classId") String classId,
      @AuthenticationPrincipal User currentUser) {
    return classService.get(new ClassId(classId, schemeId), currentUser)
        .orElseThrow(NotFoundException::new);
  }

  @GetJsonMapping("/schemes/{schemeId}/classes/{classId}/textAttributes")
  public List<TextAttribute> getTextAttributes(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("classId") String classId,
      @AuthenticationPrincipal User currentUser) {

    return classService.get(new ClassId(classId, schemeId), currentUser)
        .orElseThrow(NotFoundException::new).getTextAttributes();
  }

  @GetJsonMapping("/schemes/{schemeId}/classes/{classId}/textAttributes/{attributeId}")
  public TextAttribute getTextAttribute(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("classId") String classId,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User currentUser) {

    return classService.get(new ClassId(classId, schemeId), currentUser)
        .orElseThrow(NotFoundException::new)
        .getTextAttributes().stream()
        .filter(attr -> Objects.equals(attr.getId(), attributeId))
        .findFirst().orElseThrow(NotFoundException::new);
  }

  @GetJsonMapping("/schemes/{schemeId}/classes/{classId}/referenceAttributes")
  public List<ReferenceAttribute> getReferenceAttributes(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("classId") String classId,
      @AuthenticationPrincipal User currentUser) {
    return classService.get(new ClassId(classId, schemeId), currentUser)
        .orElseThrow(NotFoundException::new).getReferenceAttributes();
  }

  @GetJsonMapping("/schemes/{schemeId}/classes/{classId}/referenceAttributes/{attributeId}")
  public ReferenceAttribute getReferenceAttribute(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("classId") String classId,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User currentUser) {

    return classService.get(new ClassId(classId, schemeId), currentUser)
        .orElseThrow(NotFoundException::new)
        .getReferenceAttributes().stream()
        .filter(attr -> Objects.equals(attr.getId(), attributeId))
        .findFirst().orElseThrow(NotFoundException::new);
  }

}
