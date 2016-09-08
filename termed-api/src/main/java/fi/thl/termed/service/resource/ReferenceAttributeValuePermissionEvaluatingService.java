package fi.thl.termed.service.resource;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.permission.util.PermissionPredicate;
import fi.thl.termed.repository.transform.ReferenceAttributeValueDtoToModel;
import fi.thl.termed.repository.transform.ReferenceAttributeValueIdDtoToModel;
import fi.thl.termed.repository.transform.ReferenceAttributeValueIdModelToDto;
import fi.thl.termed.repository.transform.ReferenceAttributeValueModelToDto;
import fi.thl.termed.service.Service;
import fi.thl.termed.service.common.ForwardingService;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.sql.ResourceReferenceAttributeValuesByResourceId;

public class ReferenceAttributeValuePermissionEvaluatingService
    extends ForwardingService<ResourceId, Resource> {

  private Dao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao;
  private PermissionEvaluator<ResourceAttributeValueId> refAttributeValueIdEvaluator;

  public ReferenceAttributeValuePermissionEvaluatingService(
      Service<ResourceId, Resource> delegate,
      Dao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao,
      PermissionEvaluator<ResourceAttributeValueId> referenceAttributeValueEvaluator) {
    super(delegate);
    this.referenceAttributeValueDao = referenceAttributeValueDao;
    this.refAttributeValueIdEvaluator = referenceAttributeValueEvaluator;
  }

  @Override
  public void save(List<Resource> resources, User currentUser) {
    for (Resource resource : resources) {
      ResourceId resourceId = new ResourceId(resource);
      evaluateForSave(resourceId, resource.getReferences(), getReferences(resourceId), currentUser);
    }
    super.save(resources, currentUser);
  }

  @Override
  public void save(Resource resource, User currentUser) {
    ResourceId resourceId = new ResourceId(resource);
    evaluateForSave(resourceId, resource.getReferences(), getReferences(resourceId), currentUser);
    super.save(resource, currentUser);
  }

  @Override
  public void delete(ResourceId resourceId, User currentUser) {
    evaluateForDelete(resourceId, getReferences(resourceId), currentUser);
    super.delete(resourceId, currentUser);
  }

  @Override
  public List<Resource> get(SpecificationQuery<ResourceId, Resource> specification,
                            User currentUser) {
    return Lists.transform(super.get(specification, currentUser), new FilterForGet(currentUser));
  }

  @Override
  public Resource get(ResourceId id, User currentUser) {
    return new FilterForGet(currentUser).apply(super.get(id, currentUser));
  }

  private Multimap<String, Resource> getReferences(ResourceId id) {
    return valuesAsResources(ReferenceAttributeValueIdModelToDto.create().apply(
        referenceAttributeValueDao.getMap(new ResourceReferenceAttributeValuesByResourceId(id))));
  }

  private Multimap<String, Resource> valuesAsResources(Multimap<String, ResourceId> idMap) {
    return Multimaps.transformValues(idMap, new AsResource());
  }

  private void evaluateForSave(ResourceId resourceId,
                               Multimap<String, Resource> newReferences,
                               Multimap<String, Resource> oldReferences,
                               User user) {

    MapDifference<ResourceAttributeValueId, ResourceId> diff = Maps.difference(
        ReferenceAttributeValueIdDtoToModel.create(resourceId).apply(newReferences),
        ReferenceAttributeValueIdDtoToModel.create(resourceId).apply(oldReferences));

    for (ResourceAttributeValueId attributeValueId : diff.entriesOnlyOnLeft().keySet()) {
      if (!refAttributeValueIdEvaluator.hasPermission(user, attributeValueId, Permission.INSERT)) {
        throw new AccessDeniedException("Access is denied");
      }
    }
    for (ResourceAttributeValueId attributeValueId : diff.entriesInCommon().keySet()) {
      if (!refAttributeValueIdEvaluator.hasPermission(user, attributeValueId, Permission.UPDATE)) {
        throw new AccessDeniedException("Access is denied");
      }
    }
    for (ResourceAttributeValueId attributeValueId : diff.entriesOnlyOnRight().keySet()) {
      if (!refAttributeValueIdEvaluator.hasPermission(user, attributeValueId, Permission.DELETE)) {
        throw new AccessDeniedException("Access is denied");
      }
    }
  }

  private void evaluateForDelete(ResourceId resourceId,
                                 Multimap<String, Resource> references,
                                 User user) {

    Map<ResourceAttributeValueId, ResourceId> delete =
        ReferenceAttributeValueIdDtoToModel.create(resourceId).apply(references);

    for (ResourceAttributeValueId attributeValueId : delete.keySet()) {
      if (!refAttributeValueIdEvaluator.hasPermission(user, attributeValueId, Permission.DELETE)) {
        throw new AccessDeniedException("Access is denied");
      }
    }
  }

  private class FilterForGet implements Function<Resource, Resource> {

    private User user;

    public FilterForGet(User user) {
      this.user = user;
    }

    @Override
    public Resource apply(Resource r) {
      Map<ResourceAttributeValueId, Resource> original =
          ReferenceAttributeValueDtoToModel.create(new ResourceId(r)).apply(r.getReferences());

      Map<ResourceAttributeValueId, Resource> filtered =
          Maps.filterKeys(original, new PermissionPredicate<ResourceAttributeValueId>(
              refAttributeValueIdEvaluator, user, Permission.READ));

      r.setReferences(ReferenceAttributeValueModelToDto.create().apply(filtered));
      return r;
    }
  }

  private class AsResource implements Function<ResourceId, Resource> {

    public Resource apply(ResourceId resourceId) {
      return new Resource(resourceId);
    }
  }

}
