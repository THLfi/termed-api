package fi.thl.termed.service.resource;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.util.KeyPermissionEvaluatingPredicate;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.repository.transform.ResourceTextAttributeValueDtoToModel;
import fi.thl.termed.repository.transform.ResourceTextAttributeValueModelToDto;
import fi.thl.termed.service.Service;
import fi.thl.termed.service.common.ForwardingService;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.sql.ResourceTextAttributeValuesByResourceId;
import fi.thl.termed.util.StrictLangValue;

public class TextAttributeValuePermissionEvaluatingService
    extends ForwardingService<ResourceId, Resource> {

  private Dao<ResourceAttributeValueId, StrictLangValue> textAttributeValueDao;
  private PermissionEvaluator<ResourceAttributeValueId, StrictLangValue>
      textAttributeValueEvaluator;

  public TextAttributeValuePermissionEvaluatingService(
      Service<ResourceId, Resource> delegate,
      Dao<ResourceAttributeValueId, StrictLangValue> textAttributeValueDao,
      PermissionEvaluator<ResourceAttributeValueId, StrictLangValue> textAttributeValueEvaluator) {
    super(delegate);
    this.textAttributeValueDao = textAttributeValueDao;
    this.textAttributeValueEvaluator = textAttributeValueEvaluator;
  }

  @Override
  public void save(List<Resource> resources, User currentUser) {
    for (Resource resource : resources) {
      ResourceId resourceId = new ResourceId(resource);
      evaluateForSave(resourceId, resource.getProperties(), getProperties(resourceId), currentUser);
    }
    super.save(resources, currentUser);
  }

  @Override
  public void save(Resource resource, User currentUser) {
    ResourceId resourceId = new ResourceId(resource);
    evaluateForSave(resourceId, resource.getProperties(), getProperties(resourceId), currentUser);
    super.save(resource, currentUser);
  }

  @Override
  public void delete(ResourceId resourceId, User currentUser) {
    evaluateForDelete(resourceId, getProperties(resourceId), currentUser);
    super.delete(resourceId, currentUser);
  }

  @Override
  public List<Resource> get(User currentUser) {
    return Lists.transform(super.get(currentUser), new FilterForGet(currentUser));
  }

  @Override
  public List<Resource> get(Specification<ResourceId, Resource> specification, User currentUser) {
    return Lists.transform(super.get(specification, currentUser), new FilterForGet(currentUser));
  }

  @Override
  public List<Resource> get(Query query, User currentUser) {
    return Lists.transform(super.get(query, currentUser), new FilterForGet(currentUser));
  }

  @Override
  public Resource get(ResourceId id, User currentUser) {
    return new FilterForGet(currentUser).apply(super.get(id, currentUser));
  }

  private Multimap<String, StrictLangValue> getProperties(ResourceId resourceId) {
    return ResourceTextAttributeValueModelToDto.create().apply(
        textAttributeValueDao.getMap(new ResourceTextAttributeValuesByResourceId(resourceId)));
  }

  private void evaluateForSave(ResourceId resourceId,
                               Multimap<String, StrictLangValue> newProperties,
                               Multimap<String, StrictLangValue> oldProperties,
                               User user) {

    MapDifference<ResourceAttributeValueId, StrictLangValue> diff = Maps.difference(
        ResourceTextAttributeValueDtoToModel.create(resourceId).apply(newProperties),
        ResourceTextAttributeValueDtoToModel.create(resourceId).apply(oldProperties));

    for (ResourceAttributeValueId attributeValueId : diff.entriesOnlyOnLeft().keySet()) {
      if (!textAttributeValueEvaluator.hasPermission(user, attributeValueId, Permission.INSERT)) {
        throw new AccessDeniedException("Access is denied");
      }
    }
    for (ResourceAttributeValueId attributeValueId : diff.entriesInCommon().keySet()) {
      if (!textAttributeValueEvaluator.hasPermission(user, attributeValueId, Permission.UPDATE)) {
        throw new AccessDeniedException("Access is denied");
      }
    }
    for (ResourceAttributeValueId attributeValueId : diff.entriesOnlyOnRight().keySet()) {
      if (!textAttributeValueEvaluator.hasPermission(user, attributeValueId, Permission.DELETE)) {
        throw new AccessDeniedException("Access is denied");
      }
    }
  }

  private void evaluateForDelete(ResourceId resourceId,
                                 Multimap<String, StrictLangValue> properties,
                                 User user) {

    Map<ResourceAttributeValueId, StrictLangValue> delete =
        ResourceTextAttributeValueDtoToModel.create(resourceId).apply(properties);

    for (ResourceAttributeValueId attributeValueId : delete.keySet()) {
      if (!textAttributeValueEvaluator.hasPermission(user, attributeValueId, Permission.DELETE)) {
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
      Map<ResourceAttributeValueId, StrictLangValue> original =
          ResourceTextAttributeValueDtoToModel.create(new ResourceId(r)).apply(r.getProperties());

      Map<ResourceAttributeValueId, StrictLangValue> filtered =
          Maps.filterKeys(original, new KeyPermissionEvaluatingPredicate<ResourceAttributeValueId>(
              textAttributeValueEvaluator, user, Permission.READ));

      r.setProperties(ResourceTextAttributeValueModelToDto.create().apply(filtered));
      return r;
    }
  }

}
