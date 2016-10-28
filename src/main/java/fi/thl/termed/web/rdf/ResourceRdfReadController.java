package fi.thl.termed.web.rdf;

import com.google.common.collect.Lists;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.class_.specification.ClassesBySchemeId;
import fi.thl.termed.service.resource.specification.ResourcesByClassId;
import fi.thl.termed.service.resource.specification.ResourcesByTextAttributeValuePrefix;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.annotation.GetRdfMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

import static fi.thl.termed.util.specification.Query.Engine.LUCENE;

@RestController
@RequestMapping("/api/schemes/{schemeId}")
public class ResourceRdfReadController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Service<SchemeId, Scheme> schemeService;

  @Autowired
  private Service<ResourceId, Resource> resourceService;

  @Autowired
  private Service<ClassId, Class> classService;

  @GetRdfMapping("/resources")
  public Model get(@PathVariable("schemeId") UUID schemeId,
                   @RequestParam(value = "query", required = false, defaultValue = "") String query,
                   @AuthenticationPrincipal User user) {
    log.info("Exporting RDF-model {} (user: {})", schemeId, user.getUsername());
    schemeService.get(new SchemeId(schemeId), user).orElseThrow(NotFoundException::new);

    Specification<ResourceId, Resource> specification =
        query.isEmpty() ? resourcesBy(classIds(schemeId, user))
                        : resourcesBy(textAttributeIds(schemeId, user), tokenize(query));
    List<Resource> resources = resourceService.get(new Query<>(specification, LUCENE), user);
    List<Class> classes = classService.get(new Query<>(new ClassesBySchemeId(schemeId)), user);

    return new JenaRdfModel(new ResourcesToRdfModel(
        classes, resourceId -> resourceService.get(resourceId, user)).apply(resources)).getModel();
  }

  @GetRdfMapping("/classes/{typeId}/resources/{id}")
  public Model get(@PathVariable("schemeId") UUID schemeId,
                   @PathVariable("typeId") String typeId,
                   @PathVariable("id") UUID id,
                   @AuthenticationPrincipal User user) {
    schemeService.get(new SchemeId(schemeId), user).orElseThrow(NotFoundException::new);

    List<Resource> resource = resourceService.get(new ResourceId(id, typeId, schemeId), user)
        .map(Collections::singletonList).orElseThrow(NotFoundException::new);
    List<Class> classes = classService.get(new Query<>(new ClassesBySchemeId(schemeId)), user);

    return new JenaRdfModel(new ResourcesToRdfModel(
        classes, resourceId -> resourceService.get(resourceId, user)).apply(resource)).getModel();
  }

  private Specification<ResourceId, Resource> resourcesBy(List<ClassId> classIds) {
    List<Specification<ResourceId, Resource>> specifications = Lists.newArrayList();
    classIds.forEach(classId -> specifications.add(new ResourcesByClassId(classId)));
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

  private List<String> tokenize(String query) {
    return Arrays.asList(query.split("\\s"));
  }


}
