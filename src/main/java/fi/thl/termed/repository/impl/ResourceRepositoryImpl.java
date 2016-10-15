package fi.thl.termed.repository.impl;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Multimap;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.repository.transform.ReferenceAttributeValueIdDtoToModel;
import fi.thl.termed.repository.transform.ReferenceAttributeValueIdModelToDto;
import fi.thl.termed.repository.transform.ReferenceAttributeValueModelToReferrerDto;
import fi.thl.termed.repository.transform.ResourceTextAttributeValueDtoToModel;
import fi.thl.termed.repository.transform.ResourceTextAttributeValueModelToDto;
import fi.thl.termed.spesification.sql.ClassPropertiesByClassId;
import fi.thl.termed.spesification.sql.ResourceReferenceAttributeResourcesByValueId;
import fi.thl.termed.spesification.sql.ResourceReferenceAttributeValuesByResourceId;
import fi.thl.termed.spesification.sql.ResourceTextAttributeValuesByResourceId;
import fi.thl.termed.spesification.sql.SchemePropertiesBySchemeId;
import fi.thl.termed.util.collect.MapUtils;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.specification.SpecificationQuery;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Maps.difference;
import static com.google.common.collect.Multimaps.transformValues;

public class ResourceRepositoryImpl extends AbstractRepository<ResourceId, Resource> {

  private Dao<ResourceId, Resource> resourceDao;
  private Dao<ResourceAttributeValueId, StrictLangValue> textAttributeValueDao;
  private Dao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao;

  private Dao<UUID, Scheme> schemeDao;
  private Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao;
  private Dao<ClassId, Class> classDao;
  private Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao;

  public ResourceRepositoryImpl(
      Dao<ResourceId, Resource> resourceDao,
      Dao<ResourceAttributeValueId, StrictLangValue> textAttributeValueDao,
      Dao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao,
      Dao<UUID, Scheme> schemeDao,
      Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao,
      Dao<ClassId, Class> classDao,
      Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao) {

    this.resourceDao = resourceDao;
    this.textAttributeValueDao = textAttributeValueDao;
    this.referenceAttributeValueDao = referenceAttributeValueDao;

    this.schemeDao = schemeDao;
    this.schemePropertyValueDao = schemePropertyValueDao;
    this.classDao = classDao;
    this.classPropertyValueDao = classPropertyValueDao;
  }

  @Override
  protected ResourceId extractKey(Resource resource) {
    return new ResourceId(resource);
  }

  /**
   * With bulk insert, first save all resources, then dependant values.
   */
  @Override
  protected void insert(Map<ResourceId, Resource> map, User user) {
    addCreatedInfo(map.values(), user);
    resourceDao.insert(map, user);
    for (Map.Entry<ResourceId, Resource> entry : map.entrySet()) {
      insertTextAttrValues(entry.getKey(), entry.getValue().getProperties(), user);
      insertRefAttrValues(entry.getKey(), entry.getValue().getReferences(), user);
    }
  }

  @Override
  protected void insert(ResourceId id, Resource resource, User user) {
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

  private void insertRefAttrValues(ResourceId id, Multimap<String, Resource> references,
                                   User user) {
    referenceAttributeValueDao.insert(
        ReferenceAttributeValueIdDtoToModel.create(id).apply(references), user);
  }

  @Override
  protected void update(ResourceId id, Resource newResource, Resource oldResource,
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
                                   Multimap<String, Resource> oldRefs,
                                   Multimap<String, Resource> newRefs,
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
  public void delete(ResourceId resourceId, User user) {
    delete(resourceId, get(resourceId, user).get(), user);
  }

  @Override
  protected void delete(ResourceId resourceId, Resource resource, User user) {
    deleteRefAttrValues(resourceId, resource.getReferences(), user);
    deleteTextAttrValues(resourceId, resource.getProperties(), user);
    resourceDao.delete(resourceId, user);
  }

  private void deleteRefAttrValues(ResourceId id, Multimap<String, Resource> refs, User user) {
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
  public List<Resource> get(SpecificationQuery<ResourceId, Resource> specification, User user) {
    return Lists.transform(resourceDao.getKeys(specification.getSpecification(), user),
                           new ResourceLoader(user));
  }

  @Override
  public Optional<Resource> get(ResourceId id, User user) {
    return exists(id, user) ? Optional.of(new ResourceLoader(user).apply(id))
                            : Optional.<Resource>empty();
  }

  private class ResourceLoader implements Function<ResourceId, Resource> {

    private Function<ResourceId, Resource> baseResourceLoader;
    private Function<ResourceId, Multimap<String, ResourceId>> referenceLoader;
    private Function<ResourceId, Multimap<String, ResourceId>> referrerLoader;

    public ResourceLoader(User user) {
      this.baseResourceLoader = new BaseResourceLoader(user);
      this.referenceLoader = new ReferenceLoader(user);
      this.referrerLoader = new ReferrerLoader(user);
    }

    @Override
    public Resource apply(ResourceId id) {
      Resource resource = baseResourceLoader.apply(id);
      resource.setReferences(transformValues(referenceLoader.apply(id), baseResourceLoader));
      resource.setReferrers(transformValues(referrerLoader.apply(id), baseResourceLoader));
      return resource;
    }

  }

  private class BaseResourceLoader implements Function<ResourceId, Resource> {

    private User user;

    public BaseResourceLoader(User user) {
      this.user = user;
    }

    @Override
    public Resource apply(ResourceId resourceId) {
      Resource resource = new Resource(resourceDao.get(resourceId, user).get());
      resource.setProperties(
          ResourceTextAttributeValueModelToDto.create().apply(textAttributeValueDao.getMap(
              new ResourceTextAttributeValuesByResourceId(resourceId), user)));

      Scheme scheme = new Scheme(schemeDao.get(resourceId.getSchemeId(), user).get());
      scheme.setProperties(
          PropertyValueModelToDto.<UUID>create().apply(schemePropertyValueDao.getMap(
              new SchemePropertiesBySchemeId(scheme.getId()), user)));
      resource.setScheme(scheme);

      Class type = new Class(classDao.get(new ClassId(resourceId), user).get());
      type.setProperties(
          PropertyValueModelToDto.<ClassId>create().apply(classPropertyValueDao.getMap(
              new ClassPropertiesByClassId(new ClassId(resourceId)), user)));
      resource.setType(type);

      return resource;
    }
  }

  private class ReferenceLoader implements Function<ResourceId, Multimap<String, ResourceId>> {

    private User user;

    public ReferenceLoader(User user) {
      this.user = user;
    }

    @Override
    public Multimap<String, ResourceId> apply(ResourceId resourceId) {
      return ReferenceAttributeValueIdModelToDto.create()
          .apply(referenceAttributeValueDao.getMap(
              new ResourceReferenceAttributeValuesByResourceId(resourceId), user));
    }
  }

  private class ReferrerLoader implements Function<ResourceId, Multimap<String, ResourceId>> {

    private User user;

    public ReferrerLoader(User user) {
      this.user = user;
    }

    @Override
    public Multimap<String, ResourceId> apply(ResourceId resourceId) {
      return ReferenceAttributeValueModelToReferrerDto.create()
          .apply(referenceAttributeValueDao.getMap(
              new ResourceReferenceAttributeResourcesByValueId(resourceId), user));
    }
  }

}
