package fi.thl.termed.web.resource;

import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.class_.specification.ClassesBySchemeId;
import fi.thl.termed.service.resource.specification.ResourcesByClassId;
import fi.thl.termed.service.resource.specification.ResourcesByTextAttributeValuePrefix;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.MatchNone;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

import static fi.thl.termed.util.specification.Query.Engine.LUCENE;
import static fi.thl.termed.util.specification.Query.Engine.SQL;

@RestController
@RequestMapping(value = "/api")
public class ResourceReadController {

  @Autowired
  private Service<ResourceId, Resource> resourceService;

  @Autowired
  private Service<ClassId, Class> classService;

  @GetJsonMapping("/resources")
  public List<Resource> get(
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User currentUser) {

    Specification<ResourceId, Resource> specification =
        query.isEmpty() ? resourcesBy(classIds(currentUser))
                        : resourcesBy(textAttributeIds(currentUser), tokenize(query));
    return resourceService.get(query(specification, orderBy, max, bypassIndex), currentUser);
  }

  @GetJsonMapping("/schemes/{schemeId}/resources")
  public List<Resource> get(
      @PathVariable("schemeId") UUID schemeId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User currentUser) {

    Specification<ResourceId, Resource> specification =
        query.isEmpty() ? resourcesBy(classIds(schemeId, currentUser))
                        : resourcesBy(textAttributeIds(schemeId, currentUser), tokenize(query));
    return resourceService.get(query(specification, orderBy, max, bypassIndex), currentUser);
  }

  @GetJsonMapping("/schemes/{schemeId}/classes/{typeId}/resources")
  public List<Resource> get(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User currentUser) {

    ClassId classId = new ClassId(typeId, schemeId);
    Specification<ResourceId, Resource> specification =
        query.isEmpty() ? resourcesBy(classId(classId, currentUser))
                        : resourcesBy(textAttributeIds(classId, currentUser), tokenize(query));
    return resourceService.get(query(specification, orderBy, max, bypassIndex), currentUser);
  }

  @GetJsonMapping("/schemes/{schemeId}/classes/{typeId}/resources/{id}")
  public Resource get(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @AuthenticationPrincipal User currentUser) {
    return resourceService.get(new ResourceId(id, typeId, schemeId), currentUser)
        .orElseThrow(NotFoundException::new);
  }

  private Query<ResourceId, Resource> query(
      Specification<ResourceId, Resource> specification, List<String> orderBy, int max,
      boolean bypassIndex) {
    return new Query<>(specification, orderBy, max, bypassIndex ? SQL : LUCENE);
  }

  private Specification<ResourceId, Resource> resourcesBy(ClassId classId) {
    return classId != null ? new ResourcesByClassId(classId)
                           : new MatchNone<>();
  }

  private Specification<ResourceId, Resource> resourcesBy(List<ClassId> classIds) {
    List<Specification<ResourceId, Resource>> specifications = Lists.newArrayList();
    for (ClassId classId : classIds) {
      specifications.add(new ResourcesByClassId(classId));
    }
    return new OrSpecification<>(specifications);
  }

  private Specification<ResourceId, Resource> resourcesBy(List<TextAttributeId> textAttributeIds,
                                                          List<String> prefixQueries) {
    List<Specification<ResourceId, Resource>> specifications = Lists.newArrayList();
    for (TextAttributeId attributeId : textAttributeIds) {
      for (String prefixQuery : prefixQueries) {
        specifications.add(new ResourcesByTextAttributeValuePrefix(attributeId, prefixQuery));
      }
    }
    return new OrSpecification<>(specifications);
  }


  private List<ClassId> classIds(UUID schemeId, User user) {
    return classService.getKeys(new Query<>(new ClassesBySchemeId(schemeId)), user);
  }

  private List<TextAttributeId> textAttributeIds(UUID schemeId, User user) {
    return classService.get(new Query<>(new ClassesBySchemeId(schemeId)), user).stream()
        .flatMap(cls -> cls.getTextAttributes().stream())
        .map(TextAttributeId::new)
        .collect(Collectors.toList());
  }

  private List<ClassId> classIds(User user) {
    return classService.getKeys(new Query<>(new MatchAll<>()), user);
  }

  private ClassId classId(ClassId classId, User user) {
    return classService.get(classId, user).map(ClassId::new).orElse(null);
  }

  private List<TextAttributeId> textAttributeIds(User user) {
    return classService.get(new Query<>(new MatchAll<>()), user).stream()
        .flatMap(cls -> cls.getTextAttributes().stream())
        .map(TextAttributeId::new)
        .collect(Collectors.toList());
  }

  private List<TextAttributeId> textAttributeIds(ClassId classId, User user) {
    return classService.get(classId, user).orElseThrow(NotFoundException::new)
        .getTextAttributes().stream()
        .map(TextAttributeId::new)
        .collect(Collectors.toList());
  }

  private List<String> tokenize(String query) {
    return Arrays.asList(query.split("\\s"));
  }


}
