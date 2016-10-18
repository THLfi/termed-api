package fi.thl.termed.web.jstree;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.GraphUtils;
import fi.thl.termed.util.Tree;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

@RestController
@RequestMapping(value = "/api/schemes/{schemeId}/classes/{typeId}/resources/{resourceId}/trees",
    params = {"jstree=true", "context=true"})
public class ResourceContextJsTreeController {

  @Autowired
  private Service<ResourceId, Resource> resourceService;

  @Autowired
  private Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao;

  @GetJsonMapping
  public List<JsTree> getContextJsTrees(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @PathVariable("resourceId") UUID resourceId,
      @RequestParam(value = "attributeId", defaultValue = "broader") String attributeId,
      @RequestParam(value = "lang", defaultValue = "fi") String lang,
      @RequestParam(value = "referrers", defaultValue = "true") boolean referrers,
      @AuthenticationPrincipal User user) {

    Resource resource = resourceService.get(new ResourceId(schemeId, typeId, resourceId), user)
        .orElseThrow(NotFoundException::new);

    ClassId domainId = new ClassId(resource);
    ReferenceAttributeId referenceAttributeId = new ReferenceAttributeId(domainId, attributeId);
    ReferenceAttribute referenceAttribute =
        referenceAttributeDao.get(referenceAttributeId, user).orElseThrow(NotFoundException::new);

    if (referenceAttribute == null) {
      return Lists.newArrayList();
    }

    ClassId rangeId = new ClassId(referenceAttribute.getRange());

    Function<Resource, List<Resource>> referenceLoadingFunction =
        new IndexedReferenceLoader(resourceService, user, referenceAttributeId, rangeId);
    Function<Resource, List<Resource>> referrerLoadingFunction =
        new IndexedReferrerLoader(resourceService, user, referenceAttributeId, rangeId);

    Set<Resource> roots = Sets.newLinkedHashSet();
    Set<ResourceId> pathIds = Sets.newHashSet();
    Set<ResourceId> selectedIds = Sets.newHashSet();

    List<List<Resource>> paths = GraphUtils.collectPaths(
        resource, referrers ? referenceLoadingFunction : referrerLoadingFunction);
    roots.addAll(GraphUtils.findRoots(paths));
    pathIds.addAll(Lists.transform(ListUtils.flatten(paths), ResourceId::new));
    selectedIds.add(new ResourceId(resource));

    // function to convert and load resource into tree via attributeId
    Function<Resource, Tree<Resource>> toTree = new GraphUtils.ToTreeFunction<>(
        referrers ? referrerLoadingFunction : referenceLoadingFunction);

    Function<Tree<Resource>, JsTree> toJsTree =
        new ResourceTreeToJsTree(Predicates.in(pathIds),
                                 Predicates.in(selectedIds),
                                 "prefLabel", lang);

    return new ArrayList<>(roots).stream()
        .map(toTree)
        .map(toJsTree).collect(Collectors.toList());
  }

}
