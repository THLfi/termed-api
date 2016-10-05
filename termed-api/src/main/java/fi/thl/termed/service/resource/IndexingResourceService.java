package fi.thl.termed.service.resource;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import fi.thl.termed.dao.SystemDao;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.index.Index;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.service.Service;
import fi.thl.termed.service.common.ForwardingService;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.SpecificationQuery.Engine;
import fi.thl.termed.spesification.sql.ResourceReferenceAttributeResourcesByValueId;
import fi.thl.termed.spesification.sql.ResourceReferenceAttributeValuesByResourceId;

import static com.google.common.collect.Lists.transform;
import static fi.thl.termed.util.ListUtils.filter;

/**
 * Manages querying and updating full text index of resources
 */
public class IndexingResourceService extends ForwardingService<ResourceId, Resource> {

  private Repository<ResourceId, Resource> resourceRepository;

  private Index<ResourceId, Resource> resourceIndex;

  private PermissionEvaluator<ClassId> classEvaluator;
  private PermissionEvaluator<TextAttributeId> textAttrEvaluator;
  private PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator;

  private SystemDao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao;

  public IndexingResourceService(
      Service<ResourceId, Resource> delegate,
      Repository<ResourceId, Resource> resourceRepository,
      Index<ResourceId, Resource> resourceIndex,
      PermissionEvaluator<ClassId> classEvaluator,
      PermissionEvaluator<TextAttributeId> textAttrEvaluator,
      PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator,
      SystemDao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao) {
    super(delegate);
    this.resourceRepository = resourceRepository;
    this.resourceIndex = resourceIndex;
    this.classEvaluator = classEvaluator;
    this.textAttrEvaluator = textAttrEvaluator;
    this.refAttrEvaluator = refAttrEvaluator;
    this.referenceAttributeValueDao = referenceAttributeValueDao;
  }

  @Override
  public List<Resource> get(SpecificationQuery<ResourceId, Resource> specification,
                            User currentUser) {
    return specification.getEngine() == Engine.LUCENE
           ? filterByPermissions(resourceIndex.query(specification), currentUser)
           : resourceRepository.get(specification, currentUser);
  }

  // resources retrieved from index contain all data regardless of the user searching, thus filter
  private List<Resource> filterByPermissions(List<Resource> resources, User user) {
    Predicate<Resource> resourcePermissionPredicate =
        new ResourcePermissionPredicate(classEvaluator, user, Permission.READ);
    Function<Resource, Resource> resourceAttributeFilter =
        new ResourceAttributePermissionFilter(
            classEvaluator, textAttrEvaluator, refAttrEvaluator, user, Permission.READ);

    return transform(filter(resources, resourcePermissionPredicate), resourceAttributeFilter);
  }

  @Override
  public void save(List<Resource> resources, User currentUser) {
    Set<ResourceId> affectedIds = Sets.newHashSet();

    for (Resource resource : resources) {
      affectedIds.add(new ResourceId(resource));
      affectedIds.addAll(resourceRelatedIds(new ResourceId(resource)));
    }

    super.save(resources, currentUser);

    for (Resource resource : resources) {
      affectedIds.addAll(resourceRelatedIds(new ResourceId(resource)));
    }

    asyncReindex(affectedIds);
  }

  @Override
  public void save(Resource resource, User currentUser) {
    Set<ResourceId> affectedIds = Sets.newHashSet();
    affectedIds.add(new ResourceId(resource));
    affectedIds.addAll(resourceRelatedIds(new ResourceId(resource)));

    super.save(resource, currentUser);

    affectedIds.addAll(resourceRelatedIds(new ResourceId(resource)));

    reindex(affectedIds);
  }

  @Override
  public void delete(ResourceId resourceId, User currentUser) {
    Set<ResourceId> affectedIds = resourceRelatedIds(resourceId);

    super.delete(resourceId, currentUser);

    resourceIndex.deleteFromIndex(resourceId);

    asyncReindex(affectedIds);
  }

  private Set<ResourceId> resourceRelatedIds(ResourceId resourceId) {
    Set<ResourceId> relatedIds = Sets.newHashSet();
    relatedIds.addAll(referencedResourceIds(resourceId));
    relatedIds.addAll(referringResourceIds(resourceId));
    return relatedIds;
  }

  private List<ResourceId> referencedResourceIds(ResourceId resourceId) {
    return referenceAttributeValueDao.getValues(
        new ResourceReferenceAttributeValuesByResourceId(resourceId));
  }

  private List<ResourceId> referringResourceIds(ResourceId resourceId) {
    List<ResourceAttributeValueId> keys = referenceAttributeValueDao.getKeys(
        new ResourceReferenceAttributeResourcesByValueId(resourceId));
    return transform(keys, new GetResourceId());
  }

  private void reindex(Set<ResourceId> affectedIds) {
    for (ResourceId affectedId : affectedIds) {
      resourceIndex.reindex(
          affectedId,
          resourceRepository.get(affectedId, new User("indexer", "", AppRole.ADMIN)).get());
    }
  }

  private void asyncReindex(Set<ResourceId> ids) {
    if (!ids.isEmpty()) {
      resourceIndex.reindex(ImmutableList.copyOf(ids), new Function<ResourceId, Resource>() {
        public Resource apply(ResourceId id) {
          return resourceRepository.get(id, new User("indexer", "", AppRole.ADMIN)).get();
        }
      });
    }
  }

  /**
   * Helper class to get reference attribute value id.
   */
  private class GetResourceId implements Function<ResourceAttributeValueId, ResourceId> {

    public ResourceId apply(ResourceAttributeValueId attrValueId) {
      return attrValueId.getResourceId();
    }

  }

}
