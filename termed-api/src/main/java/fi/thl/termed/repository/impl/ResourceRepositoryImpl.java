package fi.thl.termed.repository.impl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.repository.transform.ReferenceAttributeValueDtoToModel;
import fi.thl.termed.repository.transform.ReferenceAttributeValueModelToDto;
import fi.thl.termed.repository.transform.ReferenceAttributeValueModelToReferrerDto;
import fi.thl.termed.repository.transform.ResourceTextAttributeValueDtoToModel;
import fi.thl.termed.repository.transform.ResourceTextAttributeValueModelToDto;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.sql.ClassPropertiesByClassId;
import fi.thl.termed.spesification.sql.ResourceReferenceAttributeResourcesByValueId;
import fi.thl.termed.spesification.sql.ResourceReferenceAttributeValuesByResourceId;
import fi.thl.termed.spesification.sql.ResourceTextAttributeValuesByResourceId;
import fi.thl.termed.spesification.sql.SchemePropertiesBySchemeId;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MapUtils;
import fi.thl.termed.util.SimpleValueDifference;
import fi.thl.termed.util.StrictLangValue;

import static com.google.common.collect.Maps.difference;

public class ResourceRepositoryImpl extends AbstractRepository<ResourceId, Resource> {

  @SuppressWarnings("all")
  private Logger log = LoggerFactory.getLogger(getClass());

  private Dao<ResourceId, Resource> resourceDao;
  private Dao<ResourceAttributeValueId, StrictLangValue> textAttributeValueDao;
  private Dao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao;

  private Dao<UUID, Scheme> schemeDao;
  private Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao;
  private Dao<ClassId, Class> classDao;
  private Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao;

  private Function<ResourceId, Resource> baseResourceLoader;
  private Function<ResourceId, Multimap<String, Resource>> referenceLoader;
  private Function<ResourceId, Multimap<String, Resource>> referrerLoader;
  private Function<ResourceId, Resource> resourceLoader;

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

    this.baseResourceLoader = new BaseResourceLoader();
    this.referenceLoader = new ReferenceLoader();
    this.referrerLoader = new ReferrerLoader();
    this.resourceLoader = new ResourceLoader();
  }

  @Override
  public void save(Resource newResource) {
    ResourceId resourceId = new ResourceId(newResource);
    if (!exists(resourceId)) {
      insert(resourceId, newResource);
    } else {
      update(resourceId, newResource, get(resourceId));
    }
  }

  @Override
  public void save(Iterable<Resource> resources) {
    Map<ResourceId, Resource> inserts = Maps.newLinkedHashMap();
    Map<ResourceId, MapDifference.ValueDifference<Resource>> updates = Maps.newLinkedHashMap();

    for (Resource newResource : resources) {
      ResourceId resourceId = new ResourceId(newResource);

      if (!exists(resourceId)) {
        inserts.put(resourceId, newResource);
      } else {
        updates.put(resourceId, new SimpleValueDifference<Resource>(newResource, get(resourceId)));
      }
    }

    insert(inserts);
    update(updates);
  }

  /**
   * With bulk insert, first save all resources, then dependant values.
   */
  @Override
  protected void insert(Map<ResourceId, Resource> map) {
    resourceDao.insert(map);

    for (Map.Entry<ResourceId, Resource> entry : map.entrySet()) {
      textAttributeValueDao.insert(
          ResourceTextAttributeValueDtoToModel.create(entry.getKey())
              .apply(entry.getValue().getProperties()));
      referenceAttributeValueDao.insert(
          ReferenceAttributeValueDtoToModel.create(entry.getKey())
              .apply(entry.getValue().getReferences()));
    }
  }

  @Override
  protected void insert(ResourceId resourceId, Resource resource) {
    resourceDao.insert(resourceId, resource);
    textAttributeValueDao.insert(
        ResourceTextAttributeValueDtoToModel.create(resourceId).apply(resource.getProperties()));
    referenceAttributeValueDao.insert(
        ReferenceAttributeValueDtoToModel.create(resourceId).apply(resource.getReferences()));
  }

  @Override
  protected void update(ResourceId resourceId, Resource newResource, Resource oldResource) {
    resourceDao.update(resourceId, newResource);
    updateTextAttrValues(resourceId, newResource.getProperties(), oldResource.getProperties());
    updateRefAttrValues(resourceId, newResource.getReferences(), oldResource.getReferences());
  }

  private void updateTextAttrValues(ResourceId resourceId,
                                    Multimap<String, StrictLangValue> newProperties,
                                    Multimap<String, StrictLangValue> oldProperties) {

    Map<ResourceAttributeValueId, StrictLangValue> newMappedProperties =
        ResourceTextAttributeValueDtoToModel.create(resourceId).apply(newProperties);
    Map<ResourceAttributeValueId, StrictLangValue> oldMappedProperties =
        ResourceTextAttributeValueDtoToModel.create(resourceId).apply(oldProperties);

    MapDifference<ResourceAttributeValueId, StrictLangValue> diff =
        difference(newMappedProperties, oldMappedProperties);

    textAttributeValueDao.insert(diff.entriesOnlyOnLeft());
    textAttributeValueDao.update(MapUtils.leftValues(diff.entriesDiffering()));
    textAttributeValueDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  private void updateRefAttrValues(ResourceId resourceId,
                                   Multimap<String, Resource> oldRefs,
                                   Multimap<String, Resource> newRefs) {

    Map<ResourceAttributeValueId, ResourceId> newMappedRefs =
        ReferenceAttributeValueDtoToModel.create(resourceId).apply(oldRefs);
    Map<ResourceAttributeValueId, ResourceId> oldMappedRefs =
        ReferenceAttributeValueDtoToModel.create(resourceId).apply(newRefs);

    MapDifference<ResourceAttributeValueId, ResourceId> diff =
        difference(newMappedRefs, oldMappedRefs);

    referenceAttributeValueDao.insert(diff.entriesOnlyOnLeft());
    referenceAttributeValueDao.update(MapUtils.leftValues(diff.entriesDiffering()));
    referenceAttributeValueDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  @Override
  protected void delete(ResourceId resourceId, Resource resource) {
    delete(resourceId);
  }

  @Override
  public void delete(ResourceId resourceId) {
    resourceDao.delete(resourceId);
  }

  @Override
  public boolean exists(ResourceId resourceId) {
    return resourceDao.exists(resourceId);
  }

  @Override
  public List<Resource> get() {
    return Lists.transform(resourceDao.getKeys(), resourceLoader);
  }

  @Override
  public List<Resource> get(Specification<ResourceId, Resource> specification) {
    return Lists.transform(resourceDao.getKeys(specification), resourceLoader);
  }

  @Override
  public List<Resource> get(Iterable<ResourceId> ids) {
    return Lists.transform(Lists.newArrayList(ids), resourceLoader);
  }

  @Override
  public Resource get(ResourceId id) {
    return resourceLoader.apply(id);
  }

  private class ResourceLoader implements Function<ResourceId, Resource> {

    @Override
    public Resource apply(ResourceId resourceId) {
      Resource resource = baseResourceLoader.apply(resourceId);
      resource.setReferences(referenceLoader.apply(resourceId));
      resource.setReferrers(referrerLoader.apply(resourceId));
      return resource;
    }
  }

  private class BaseResourceLoader implements Function<ResourceId, Resource> {

    @Override
    public Resource apply(ResourceId resourceId) {
      Resource resource = new Resource(resourceDao.get(resourceId));
      resource.setProperties(
          ResourceTextAttributeValueModelToDto.create().apply(textAttributeValueDao.getMap(
              new ResourceTextAttributeValuesByResourceId(resourceId))));

      Scheme scheme = new Scheme(schemeDao.get(resourceId.getSchemeId()));
      scheme.setProperties(
          PropertyValueModelToDto.<UUID>create().apply(schemePropertyValueDao.getMap(
              new SchemePropertiesBySchemeId(scheme.getId()))));
      resource.setScheme(scheme);

      Class type = new Class(classDao.get(new ClassId(resourceId)));
      type.setProperties(
          PropertyValueModelToDto.<ClassId>create().apply(classPropertyValueDao.getMap(
              new ClassPropertiesByClassId(new ClassId(resourceId)))));
      resource.setType(type);

      return resource;
    }
  }

  private class ReferenceLoader implements Function<ResourceId, Multimap<String, Resource>> {

    @Override
    public Multimap<String, Resource> apply(ResourceId resourceId) {
      Multimap<String, ResourceId> references = ReferenceAttributeValueModelToDto.create()
          .apply(referenceAttributeValueDao.getMap(
              new ResourceReferenceAttributeValuesByResourceId(resourceId)));
      return Multimaps.transformValues(references, baseResourceLoader);
    }
  }

  private class ReferrerLoader implements Function<ResourceId, Multimap<String, Resource>> {

    @Override
    public Multimap<String, Resource> apply(ResourceId resourceId) {
      Multimap<String, ResourceId> references = ReferenceAttributeValueModelToReferrerDto.create()
          .apply(referenceAttributeValueDao.getMap(
              new ResourceReferenceAttributeResourcesByValueId(resourceId)));
      return Multimaps.transformValues(references, baseResourceLoader);
    }
  }

}
