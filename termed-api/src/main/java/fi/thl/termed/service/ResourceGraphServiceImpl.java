package fi.thl.termed.service;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.ResourceKey;
import fi.thl.termed.domain.ResourceTree;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.GraphUtils;
import fi.thl.termed.util.ListUtils;
import fi.thl.termed.util.ResourceTreeToJsTree;
import fi.thl.termed.util.Tree;
import fi.thl.termed.util.UUIDs;

import static java.lang.String.format;

/**
 * Implements graph service by querying and transforming resources using resource service
 */
@Service
@Transactional(readOnly = true)
public class ResourceGraphServiceImpl implements ResourceGraphService {

  @Autowired
  @Qualifier("resourceService")
  private ResourceService resourceService;

  private Function<Resource, ResourceId> toResourceId = new Function<Resource, ResourceId>() {
    public ResourceId apply(Resource resource) {
      return new ResourceId(resource.getSchemeId(), resource.getTypeId(), resource.getId());
    }
  };

  private Function<Resource, UUID> toId = new Function<Resource, UUID>() {
    public UUID apply(Resource resource) {
      return resource.getId();
    }
  };

  @Override
  public List<List<Resource>> findPaths(ResourceKey key, String attributeId, User user) {
    Resource resource = resourceService.get(new ResourceId(key), user);
    return GraphUtils.collectPaths(resource,
                                   new IndexedReferrers(key.getSchemeId(), attributeId, user));
  }

  @Override
  public List<List<Resource>> findReferrerPaths(ResourceKey key, String attributeId, User user) {
    Resource resource = resourceService.get(new ResourceId(key), user);
    return GraphUtils.collectPaths(resource,
                                   new IndexedReferences(key.getSchemeId(), attributeId, user));
  }

  @Override
  public List<ResourceTree> getTrees(UUID schemeId, String attrId, User user) {
    String findRootsQuery = format("+scheme.id:%s -referrers.%s.id:[* TO *]", schemeId, attrId);
    List<Resource> roots = resourceService.get(new Query(findRootsQuery, Integer.MAX_VALUE), user);

    Function<Resource, ResourceTree> toTree =
        Functions.compose(
            new ToResourceTree(),
            new GraphUtils.ToTreeFunction<Resource>(
                new IndexedReferences(schemeId, attrId, user)));

    return Lists.transform(roots, toTree);
  }

  @Override
  public List<JsTree> getJsTrees(UUID schemeId, String refAttrId, String lang, User user) {
    String findRootsQuery = format("+scheme.id:%s -referrers.%s.id:[* TO *]", schemeId, refAttrId);
    List<Resource> roots = resourceService.get(new Query(findRootsQuery, Integer.MAX_VALUE), user);

    Function<Resource, JsTree> toTree =
        Functions.compose(
            new ResourceTreeToJsTree(Predicates.<UUID>alwaysTrue(),
                                     UUIDs.nilUuid(), "prefLabel", lang),
            new GraphUtils.ToTreeFunction<Resource>(
                new IndexedReferences(schemeId, refAttrId, user)));

    return Lists.transform(roots, toTree);
  }

  @Override
  public List<ResourceTree> getReferrerTrees(UUID schemeId, String attributeId, User user) {
    String findRootsQuery = format("+scheme.id:%s -%s.id:[* TO *]", schemeId, attributeId);
    List<Resource> roots = resourceService.get(new Query(findRootsQuery, Integer.MAX_VALUE), user);

    Function<Resource, ResourceTree> toTree =
        Functions.compose(
            new ToResourceTree(),
            new GraphUtils.ToTreeFunction<Resource>(
                new IndexedReferrers(schemeId, attributeId, user)));

    return Lists.transform(roots, toTree);
  }

  @Override
  public List<JsTree> getJsReferrerTrees(UUID schemeId, String attrId, String lang, User user) {
    String findRootsQuery = format("+scheme.id:%s -%s.id:[* TO *]", schemeId, attrId);
    List<Resource> roots = resourceService.get(new Query(findRootsQuery, Integer.MAX_VALUE), user);

    Function<Resource, JsTree> toTree =
        Functions.compose(
            new ResourceTreeToJsTree(Predicates.<UUID>alwaysTrue(),
                                     UUIDs.nilUuid(), "prefLabel", lang),
            new GraphUtils.ToTreeFunction<Resource>(
                new IndexedReferrers(schemeId, attrId, user)));

    return Lists.transform(roots, toTree);
  }

  @Override
  public ResourceTree getTree(ResourceKey key, String attributeId, User user) {
    Resource root = resourceService.get(new ResourceId(key), user);

    Function<Resource, ResourceTree> toTree =
        Functions.compose(
            new ToResourceTree(),
            new GraphUtils.ToTreeFunction<Resource>(
                new IndexedReferences(key.getSchemeId(), attributeId, user)));

    return toTree.apply(root);
  }

  @Override
  public ResourceTree getReferrerTree(ResourceKey key, String attributeId, User user) {
    Resource root = resourceService.get(new ResourceId(key), user);

    Function<Resource, ResourceTree> toTree =
        Functions.compose(
            new ToResourceTree(),
            new GraphUtils.ToTreeFunction<Resource>(
                new IndexedReferrers(key.getSchemeId(), attributeId, user)));

    return toTree.apply(root);
  }

  @Override
  public List<ResourceTree> getContextTrees(ResourceKey key, String attributeId, User user) {
    List<List<Resource>> paths = findPaths(key, attributeId, user);

    List<Resource> roots = GraphUtils.findRoots(paths);
    List<ResourceId> expandedResourceIds = Lists.transform(ListUtils.flatten(paths), toResourceId);

    Function<Resource, ResourceTree> toTree =
        Functions.compose(
            new ToResourceTree(Predicates.in(expandedResourceIds)),
            new GraphUtils.ToTreeFunction<Resource>(
                new IndexedReferences(key.getSchemeId(), attributeId, user)));

    return Lists.transform(roots, toTree);
  }


  @Override
  public List<JsTree> getContextJsTrees(ResourceKey key, String attrId, String lang, User user) {
    List<List<Resource>> paths = findPaths(key, attrId, user);

    List<Resource> roots = GraphUtils.findRoots(paths);
    Set<UUID> expandedResourceIds =
        Sets.newHashSet(Lists.transform(ListUtils.flatten(paths), toId));

    Function<Resource, JsTree> toTree =
        Functions.compose(
            new ResourceTreeToJsTree(Predicates.in(expandedResourceIds), key.getId(),
                                     "prefLabel", lang),
            new GraphUtils.ToTreeFunction<Resource>(
                new IndexedReferences(key.getSchemeId(), attrId, user)));

    return Lists.transform(roots, toTree);
  }

  @Override
  public List<ResourceTree> getContextReferrerTrees(ResourceKey key, String attrId, User user) {
    List<List<Resource>> paths = findReferrerPaths(key, attrId, user);

    List<Resource> roots = GraphUtils.findRoots(paths);
    List<ResourceId> expandedResourceIds = Lists.transform(ListUtils.flatten(paths), toResourceId);

    Function<Resource, ResourceTree> toTree =
        Functions.compose(
            new ToResourceTree(Predicates.in(expandedResourceIds)),
            new GraphUtils.ToTreeFunction<Resource>(
                new IndexedReferrers(key.getSchemeId(), attrId, user)));

    return Lists.transform(roots, toTree);
  }

  @Override
  public List<JsTree> getContextJsReferrerTrees(ResourceKey key, String attrId, String lang,
                                                User user) {
    List<List<Resource>> paths = findReferrerPaths(key, attrId, user);

    List<Resource> roots = GraphUtils.findRoots(paths);
    Set<UUID> expandedResourceIds =
        Sets.newHashSet(Lists.transform(ListUtils.flatten(paths), toId));

    Function<Resource, JsTree> toTree =
        Functions.compose(
            new ResourceTreeToJsTree(Predicates.in(expandedResourceIds), key.getId(),
                                     "prefLabel", lang),
            new GraphUtils.ToTreeFunction<Resource>(
                new IndexedReferrers(key.getSchemeId(), attrId, user)));

    return Lists.transform(roots, toTree);
  }

  private class IndexedReferences implements Function<Resource, List<Resource>> {

    private UUID schemeId;
    private String referenceAttributeId;
    private User user;

    private String queryTemplate = "+scheme.id:%s +referrers.%s.id:%s";

    public IndexedReferences(UUID schemeId, String referenceAttributeId, User user) {
      this.schemeId = schemeId;
      this.referenceAttributeId = referenceAttributeId;
      this.user = user;
    }

    @Override
    public List<Resource> apply(Resource resource) {
      String queryString = format(queryTemplate, schemeId, referenceAttributeId, resource.getId());
      Map<ResourceId, Resource> cachedReferenceValues =
          mapByResourceId(resourceService.get(new Query(queryString, Integer.MAX_VALUE), user));

      List<Resource> orderedReferenceValues = Lists.newArrayList();
      for (ResourceId resourceId : resource.getReferenceIds().get(referenceAttributeId)) {
        orderedReferenceValues.add(cachedReferenceValues.get(resourceId));
      }

      return orderedReferenceValues;
    }

    private Map<ResourceId, Resource> mapByResourceId(List<Resource> resources) {
      Map<ResourceId, Resource> index = Maps.newLinkedHashMap();
      for (Resource resource : resources) {
        index.put(new ResourceId(resource), resource);
      }
      return index;
    }
  }

  private class IndexedReferrers implements Function<Resource, List<Resource>> {

    private UUID schemeId;
    private String referenceAttributeId;
    private User user;

    private String queryTemplate = "+scheme.id:%s +%s.id:%s";

    public IndexedReferrers(UUID schemeId, String referenceAttributeId, User user) {
      this.schemeId = schemeId;
      this.referenceAttributeId = referenceAttributeId;
      this.user = user;
    }

    @Override
    public List<Resource> apply(Resource resource) {
      return resourceService.get(new Query(
          format(queryTemplate, schemeId, referenceAttributeId, resource.getId()),
          Integer.MAX_VALUE), user);
    }
  }

  private class ToResourceTree implements Function<Tree<Resource>, ResourceTree> {

    private Predicate<ResourceId> addChildrenPredicate;

    public ToResourceTree() {
      this(Predicates.<ResourceId>alwaysTrue());
    }

    public ToResourceTree(Predicate<ResourceId> addChildrenPredicate) {
      this.addChildrenPredicate = addChildrenPredicate;
    }

    @Override
    public ResourceTree apply(Tree<Resource> tree) {
      ResourceTree dto = new ResourceTree(tree.getData());
      dto.setPath(Lists.transform(Lists.newArrayList(tree.getPath()), toId));
      if (addChildrenPredicate.apply(toResourceId.apply(tree.getData()))) {
        dto.setChildren(Lists.transform(tree.getChildren(), this));
      }
      return dto;
    }

  }

}
