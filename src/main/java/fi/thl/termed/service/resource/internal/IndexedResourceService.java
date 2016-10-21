package fi.thl.termed.service.resource.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.ApplicationReadyEvent;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Query;

public class IndexedResourceService extends ForwardingService<ResourceId, Resource> {

  private Index<ResourceId, Resource> index;

  private PermissionEvaluator<ClassId> classEvaluator;
  private PermissionEvaluator<TextAttributeId> textAttrEvaluator;
  private PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator;

  private User indexer = new User("indexer", "", AppRole.ADMIN);

  public IndexedResourceService(
      Service<ResourceId, Resource> delegate,
      Index<ResourceId, Resource> index,
      PermissionEvaluator<ClassId> classEvaluator,
      PermissionEvaluator<TextAttributeId> textAttrEvaluator,
      PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator) {
    super(delegate);
    this.index = index;
    this.classEvaluator = classEvaluator;
    this.textAttrEvaluator = textAttrEvaluator;
    this.refAttrEvaluator = refAttrEvaluator;
  }

  @Subscribe
  public void initIndexOn(ApplicationReadyEvent e) {
    if (index.indexSize() == 0) {
      index.reindex(super.getKeys(new Query<>(new MatchAll<>()), indexer),
                    k -> super.get(k, indexer).get());
    }
  }

  @Override
  public List<ResourceId> save(List<Resource> resources, User user) {
    Set<ResourceId> reindexSet = Sets.newHashSet();

    for (Resource resource : resources) {
      reindexSet.add(new ResourceId(resource));
      reindexSet.addAll(resourceRelatedIds(new ResourceId(resource)));
    }

    List<ResourceId> ids = super.save(resources, user);

    for (Resource resource : resources) {
      reindexSet.addAll(resourceRelatedIds(new ResourceId(resource)));
    }
    asyncReindex(reindexSet);
    return ids;
  }

  @Override
  public ResourceId save(Resource resource, User user) {
    Set<ResourceId> reindexSet = Sets.newHashSet();

    reindexSet.add(new ResourceId(resource));
    reindexSet.addAll(resourceRelatedIds(new ResourceId(resource)));

    ResourceId id = super.save(resource, user);

    reindexSet.addAll(resourceRelatedIds(new ResourceId(resource)));
    reindex(reindexSet);
    return id;
  }

  @Override
  public void delete(ResourceId resourceId, User user) {
    Set<ResourceId> reindexSet = resourceRelatedIds(resourceId);
    super.delete(resourceId, user);
    index.deleteFromIndex(resourceId);
    asyncReindex(reindexSet);
  }

  @Override
  public List<Resource> get(Query<ResourceId, Resource> specification, User user) {
    return specification.getEngine() == Query.Engine.LUCENE
           ? filter(index.query(specification), user, Permission.READ)
           : super.get(specification, user);
  }

  private List<Resource> filter(List<Resource> resources, User user, Permission permission) {
    return resources.stream()
        .filter(new ResourcePermissionPredicate(classEvaluator, user, permission))
        .map(new ResourceAttributePermissionFilter(
            classEvaluator, textAttrEvaluator, refAttrEvaluator, user, permission))
        .collect(Collectors.toList());
  }

  private Set<ResourceId> resourceRelatedIds(ResourceId resourceId) {
    Set<ResourceId> refValues = new HashSet<>();
    Optional<Resource> resourceOptional = super.get(resourceId, indexer);

    if (resourceOptional.isPresent()) {
      refValues.addAll(resourceOptional.get().getReferenceIds().values());
      refValues.addAll(resourceOptional.get().getReferrerIds().values());
    }

    return refValues;
  }

  private void reindex(Set<ResourceId> ids) {
    for (ResourceId id : ids) {
      index.reindex(id, super.get(id, indexer).get());
    }
  }

  private void asyncReindex(Set<ResourceId> ids) {
    if (!ids.isEmpty()) {
      index.reindex(ImmutableList.copyOf(ids), id -> {
        return IndexedResourceService.super.get(id, indexer).get();
      });
    }
  }

}
