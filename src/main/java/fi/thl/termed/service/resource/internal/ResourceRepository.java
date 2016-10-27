package fi.thl.termed.service.resource.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Multimap;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.ReferenceAttributeValueIdDtoToModel;
import fi.thl.termed.domain.transform.ReferenceAttributeValueIdModelToDto;
import fi.thl.termed.domain.transform.ReferenceAttributeValueModelToReferrerDto;
import fi.thl.termed.domain.transform.ResourceTextAttributeValueDtoToModel;
import fi.thl.termed.domain.transform.ResourceTextAttributeValueModelToDto;
import fi.thl.termed.util.collect.MapUtils;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.specification.Query;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Maps.difference;

public class ResourceRepository extends AbstractRepository<ResourceId, Resource> {

  private Dao<ResourceId, Resource> resourceDao;
  private Dao<ResourceAttributeValueId, StrictLangValue> textAttributeValueDao;
  private Dao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao;

  public ResourceRepository(
      Dao<ResourceId, Resource> resourceDao,
      Dao<ResourceAttributeValueId, StrictLangValue> textAttributeValueDao,
      Dao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao) {

    this.resourceDao = resourceDao;
    this.textAttributeValueDao = textAttributeValueDao;
    this.referenceAttributeValueDao = referenceAttributeValueDao;
  }

  /**
   * With bulk insert, first save all resources, then dependant values.
   */
  @Override
  public void insert(Map<ResourceId, Resource> map, User user) {
    addCreatedInfo(map.values(), user);
    resourceDao.insert(map, user);
    for (Map.Entry<ResourceId, Resource> entry : map.entrySet()) {
      insertTextAttrValues(entry.getKey(), entry.getValue().getProperties(), user);
      insertRefAttrValues(entry.getKey(), entry.getValue().getReferences(), user);
    }
  }

  @Override
  public void insert(ResourceId id, Resource resource, User user) {
    addCreatedInfo(resource, new Date(), user);
    resourceDao.insert(id, resource, user);
    insertTextAttrValues(id, resource.getProperties(), user);
    insertRefAttrValues(id, resource.getReferences(), user);
  }

  private void addCreatedInfo(Iterable<Resource> values, User user) {
    Date now = new Date();
    for (Resource resource : values) {
      addCreatedInfo(resource, now, user);
    }
  }

  private void addCreatedInfo(Resource resource, Date now, User user) {
    resource.setCreatedDate(now);
    resource.setCreatedBy(user.getUsername());
    resource.setLastModifiedDate(now);
    resource.setLastModifiedBy(user.getUsername());
  }

  private void insertTextAttrValues(ResourceId id,
                                    Multimap<String, StrictLangValue> properties, User user) {
    textAttributeValueDao.insert(
        ResourceTextAttributeValueDtoToModel.create(id).apply(properties), user);
  }

  private void insertRefAttrValues(ResourceId id, Multimap<String, ResourceId> references,
                                   User user) {
    referenceAttributeValueDao.insert(
        ReferenceAttributeValueIdDtoToModel.create(id).apply(references), user);
  }

  @Override
  public void update(ResourceId id, Resource newResource, Resource oldResource,
                     User user) {
    addLastModifiedInfo(newResource, oldResource, user);
    resourceDao.update(id, newResource, user);
    updateTextAttrValues(id, newResource.getProperties(), oldResource.getProperties(), user);
    updateRefAttrValues(id, newResource.getReferences(), oldResource.getReferences(), user);
  }

  private void addLastModifiedInfo(Resource newResource, Resource oldResource, User user) {
    Date now = new Date();
    newResource.setCreatedDate(oldResource.getCreatedDate());
    newResource.setCreatedBy(oldResource.getCreatedBy());
    newResource.setLastModifiedDate(now);
    newResource.setLastModifiedBy(user.getUsername());
  }

  private void updateTextAttrValues(ResourceId resourceId,
                                    Multimap<String, StrictLangValue> newProperties,
                                    Multimap<String, StrictLangValue> oldProperties,
                                    User user) {

    Map<ResourceAttributeValueId, StrictLangValue> newMappedProperties =
        ResourceTextAttributeValueDtoToModel.create(resourceId).apply(newProperties);
    Map<ResourceAttributeValueId, StrictLangValue> oldMappedProperties =
        ResourceTextAttributeValueDtoToModel.create(resourceId).apply(oldProperties);

    MapDifference<ResourceAttributeValueId, StrictLangValue> diff =
        difference(newMappedProperties, oldMappedProperties);

    textAttributeValueDao.insert(diff.entriesOnlyOnLeft(), user);
    textAttributeValueDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    textAttributeValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateRefAttrValues(ResourceId resourceId,
                                   Multimap<String, ResourceId> oldRefs,
                                   Multimap<String, ResourceId> newRefs,
                                   User user) {

    Map<ResourceAttributeValueId, ResourceId> newMappedRefs =
        ReferenceAttributeValueIdDtoToModel.create(resourceId).apply(oldRefs);
    Map<ResourceAttributeValueId, ResourceId> oldMappedRefs =
        ReferenceAttributeValueIdDtoToModel.create(resourceId).apply(newRefs);

    MapDifference<ResourceAttributeValueId, ResourceId> diff =
        difference(newMappedRefs, oldMappedRefs);

    referenceAttributeValueDao.insert(diff.entriesOnlyOnLeft(), user);
    referenceAttributeValueDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    referenceAttributeValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(ResourceId resourceId, Resource resource, User user) {
    deleteRefAttrValues(resourceId, resource.getReferences(), user);
    deleteTextAttrValues(resourceId, resource.getProperties(), user);
    resourceDao.delete(resourceId, user);
  }

  private void deleteRefAttrValues(ResourceId id, Multimap<String, ResourceId> refs, User user) {
    Map<ResourceAttributeValueId, ResourceId> mappedRefs =
        ReferenceAttributeValueIdDtoToModel.create(id).apply(refs);
    referenceAttributeValueDao.delete(ImmutableList.copyOf(mappedRefs.keySet()), user);
  }

  private void deleteTextAttrValues(ResourceId id, Multimap<String, StrictLangValue> properties,
                                    User user) {
    Map<ResourceAttributeValueId, StrictLangValue> mappedProperties =
        ResourceTextAttributeValueDtoToModel.create(id).apply(properties);
    textAttributeValueDao.delete(ImmutableList.copyOf(mappedProperties.keySet()), user);
  }

  @Override
  public boolean exists(ResourceId resourceId, User user) {
    return resourceDao.exists(resourceId, user);
  }

  @Override
  public List<Resource> get(Query<ResourceId, Resource> specification, User user) {
    return resourceDao.getValues(specification.getSpecification(), user).stream()
        .map(resource -> populateValue(resource, user))
        .collect(Collectors.toList());
  }

  @Override
  public List<ResourceId> getKeys(Query<ResourceId, Resource> specification,
                                  User user) {
    return resourceDao.getKeys(specification.getSpecification(), user);
  }

  @Override
  public Optional<Resource> get(ResourceId id, User user) {
    return resourceDao.get(id, user).map(resource -> populateValue(resource, user));
  }

  private Resource populateValue(Resource resource, User user) {
    resource = new Resource(resource);

    resource.setProperties(
        ResourceTextAttributeValueModelToDto.create().apply(textAttributeValueDao.getMap(
            new ResourceTextAttributeValuesByResourceId(new ResourceId(resource)), user)));

    resource.setReferences(
        ReferenceAttributeValueIdModelToDto.create()
            .apply(referenceAttributeValueDao.getMap(
                new ResourceReferenceAttributeValuesByResourceId(new ResourceId(resource)), user)));

    resource.setReferrers(
        ReferenceAttributeValueModelToReferrerDto.create()
            .apply(referenceAttributeValueDao.getMap(
                new ResourceReferenceAttributeResourcesByValueId(new ResourceId(resource)), user)));

    return resource;
  }

}
