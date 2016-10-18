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

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.spesification.resource.ResourcesByClassId;
import fi.thl.termed.spesification.resource.ResourcesByTextAttributeValuePrefix;
import fi.thl.termed.spesification.sql.ClassesBySchemeId;
import fi.thl.termed.spesification.sql.TextAttributesByClassId;
import fi.thl.termed.spesification.sql.TextAttributesBySchemeId;
import fi.thl.termed.util.dao.Dao;
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
  private Dao<ClassId, Class> classDao;

  @Autowired
  private Dao<TextAttributeId, TextAttribute> textAttributeDao;

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

    ClassId classId = new ClassId(schemeId, typeId);
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
    return resourceService.get(new ResourceId(schemeId, typeId, id), currentUser)
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

  private List<ClassId> classIds(User user) {
    return classDao.getKeys(new MatchAll<>(), user);
  }

  private List<ClassId> classIds(UUID schemeId, User user) {
    return classDao.getKeys(new ClassesBySchemeId(schemeId), user);
  }

  private ClassId classId(ClassId classId, User user) {
    return classDao.exists(classId, user) ? classId : null;
  }

  private List<TextAttributeId> textAttributeIds(User user) {
    return textAttributeDao.getKeys(new MatchAll<>(), user);
  }

  private List<TextAttributeId> textAttributeIds(UUID schemeId, User user) {
    return textAttributeDao.getKeys(new TextAttributesBySchemeId(schemeId), user);
  }

  private List<TextAttributeId> textAttributeIds(ClassId classId, User user) {
    return textAttributeDao.getKeys(new TextAttributesByClassId(classId), user);
  }

  private List<String> tokenize(String query) {
    return Arrays.asList(query.split("\\s"));
  }


}
