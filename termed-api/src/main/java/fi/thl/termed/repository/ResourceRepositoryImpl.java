package fi.thl.termed.repository;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.repository.dao.ClassDao;
import fi.thl.termed.repository.dao.ClassPropertyValueDao;
import fi.thl.termed.repository.dao.ResourceDao;
import fi.thl.termed.repository.dao.ResourceReferenceAttributeValueDao;
import fi.thl.termed.repository.dao.ResourceTextAttributeValueDao;
import fi.thl.termed.repository.dao.SchemeDao;
import fi.thl.termed.repository.dao.SchemePropertyValueDao;
import fi.thl.termed.repository.spesification.ClassPropertyValueSpecificationBySubjectId;
import fi.thl.termed.repository.spesification.ResourceReferenceAttributeSpecificationByValueId;
import fi.thl.termed.repository.spesification.ResourceReferenceAttributeValueSpecificationByResourceId;
import fi.thl.termed.repository.spesification.ResourceTextAttributeValueSpecificationByResourceId;
import fi.thl.termed.repository.spesification.SchemePropertyValueSpecificationBySubjectId;
import fi.thl.termed.repository.spesification.Specification;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.repository.transform.ReferenceAttributeValueDtoToModel;
import fi.thl.termed.repository.transform.ReferenceAttributeValueModelToDto;
import fi.thl.termed.repository.transform.ReferenceAttributeValueModelToReferrerDto;
import fi.thl.termed.repository.transform.ResourceTextAttributeValueDtoToModel;
import fi.thl.termed.repository.transform.ResourceTextAttributeValueModelToDto;
import fi.thl.termed.util.MapUtils;
import fi.thl.termed.util.SimpleValueDifference;
import fi.thl.termed.util.StrictLangValue;

import static com.google.common.collect.Maps.difference;
import static fi.thl.termed.util.FunctionUtils.memoize;

@Repository
public class ResourceRepositoryImpl extends ResourceRepository {

  @SuppressWarnings("all")
  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private ResourceDao resourceDao;
  @Autowired
  private ResourceTextAttributeValueDao textAttributeValueDao;
  @Autowired
  private ResourceReferenceAttributeValueDao referenceAttributeValueDao;

  @Autowired
  private SchemeDao schemeDao;
  @Autowired
  private SchemePropertyValueDao schemePropertyValueDao;

  @Autowired
  private ClassDao classDao;
  @Autowired
  private ClassPropertyValueDao classPropertyValueDao;

  @Override
  public void save(Resource newResource) {
    ResourceId resourceId = new ResourceId(newResource);
    if (!exists(resourceId)) {
      insert(resourceId, newResource);
    } else {
      Resource oldResource = get(resourceId);

      newResource.setCreatedBy(oldResource.getCreatedBy());
      newResource.setCreatedDate(oldResource.getCreatedDate());

      if (!newResource.equals(oldResource)) {
        update(resourceId, newResource, oldResource);
      }
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
        Resource oldResource = get(resourceId);

        newResource.setCreatedBy(oldResource.getCreatedBy());
        newResource.setCreatedDate(oldResource.getCreatedDate());

        if (!newResource.equals(oldResource)) {
          updates.put(resourceId, new SimpleValueDifference<Resource>(newResource, oldResource));
        }
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
        difference(newMappedProperties,
                   oldMappedProperties);

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

    MapDifference<ResourceAttributeValueId, ResourceId> diff = difference(newMappedRefs,
                                                                          oldMappedRefs);

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
    return loadResources(resourceDao.getKeys());
  }

  @Override
  public List<Resource> get(Specification<ResourceId, Resource> specification) {
    return loadResources(resourceDao.getKeys(specification));
  }

  @Override
  public List<Resource> get(Iterable<ResourceId> ids) {
    return loadResources(Lists.newArrayList(ids));
  }

  @Override
  public Resource get(ResourceId id) {
    return Iterables.getFirst(loadResources(Collections.singletonList(id)), null);
  }

  private List<Resource> loadResources(List<ResourceId> resourceIds) {
    final Function<ResourceId, Resource> resourceLoader = memoize(new ResourceLoader(), 10000);
    final Function<ResourceId, Scheme> schemeLoader = memoize(new SchemeLoader());
    final Function<ResourceId, Class> typeLoader = memoize(new TypeLoader());

    final Function<ResourceId, Resource> referencedResourceLoader =
        memoize(new Function<ResourceId, Resource>() {
          public Resource apply(ResourceId input) {
            return buildReferencedResource(resourceLoader.apply(input),
                                           schemeLoader.apply(input),
                                           typeLoader.apply(input));
          }
        }, 1000);

    final Function<ResourceId, Multimap<String, Resource>> referenceLoader =
        new ReferenceLoader(referencedResourceLoader);
    final Function<ResourceId, Multimap<String, Resource>> referrerLoader =
        new ReferrerLoader(referencedResourceLoader);

    Function<ResourceId, Resource> fullResourceLoader = new Function<ResourceId, Resource>() {
      public Resource apply(ResourceId resourceId) {
        Resource resource = resourceLoader.apply(resourceId);
        resource.setScheme(schemeLoader.apply(resourceId));
        resource.setType(typeLoader.apply(resourceId));
        resource.setReferences(referenceLoader.apply(resourceId));
        resource.setReferrers(referrerLoader.apply(resourceId));
        return resource;
      }
    };

    return Lists.transform(resourceIds, fullResourceLoader);
  }

  private Resource buildReferencedResource(Resource resource, Scheme scheme, Class type) {
    Resource ref = new Resource(resource.getId(), resource.getCode(), resource.getUri());
    ref.setProperties(resource.getProperties());

    Scheme refScheme = new Scheme(scheme.getId());
    refScheme.setProperties(scheme.getProperties());
    ref.setScheme(scheme);

    Class refType = new Class(type.getId());
    refType.setProperties(type.getProperties());
    ref.setType(refType);

    return ref;
  }

  private class ResourceLoader implements Function<ResourceId, Resource> {

    @Override
    public Resource apply(ResourceId resourceId) {
      Resource resource = resourceDao.get(resourceId);
      resource.setProperties(
          ResourceTextAttributeValueModelToDto.create().apply(textAttributeValueDao.getMap(
              new ResourceTextAttributeValueSpecificationByResourceId(resourceId))));
      return resource;
    }
  }

  private class SchemeLoader implements Function<ResourceId, Scheme> {

    @Override
    public Scheme apply(ResourceId resourceId) {
      Scheme scheme = schemeDao.get(resourceId.getSchemeId());
      scheme.setProperties(
          PropertyValueModelToDto.<UUID>create().apply(schemePropertyValueDao.getMap(
              new SchemePropertyValueSpecificationBySubjectId(scheme.getId()))));
      return scheme;
    }
  }

  private class TypeLoader implements Function<ResourceId, Class> {

    @Override
    public Class apply(ResourceId resourceId) {
      ClassId typeId = new ClassId(resourceId.getSchemeId(), resourceId.getTypeId());
      Class type = classDao.get(typeId);
      type.setProperties(
          PropertyValueModelToDto.<ClassId>create().apply(classPropertyValueDao.getMap(
              new ClassPropertyValueSpecificationBySubjectId(typeId))));
      return type;
    }
  }

  private class ReferenceLoader implements Function<ResourceId, Multimap<String, Resource>> {

    private Function<ResourceId, Resource> resourceLoader;

    public ReferenceLoader(Function<ResourceId, Resource> resourceLoader) {
      this.resourceLoader = resourceLoader;
    }

    @Override
    public Multimap<String, Resource> apply(ResourceId resourceId) {
      Multimap<String, ResourceId> references = ReferenceAttributeValueModelToDto.create()
          .apply(referenceAttributeValueDao.getMap(
              new ResourceReferenceAttributeValueSpecificationByResourceId(resourceId)));
      return Multimaps.transformValues(references, resourceLoader);
    }
  }

  private class ReferrerLoader implements Function<ResourceId, Multimap<String, Resource>> {

    private Function<ResourceId, Resource> resourceLoader;

    public ReferrerLoader(Function<ResourceId, Resource> resourceLoader) {
      this.resourceLoader = resourceLoader;
    }

    @Override
    public Multimap<String, Resource> apply(ResourceId resourceId) {
      Multimap<String, ResourceId> references = ReferenceAttributeValueModelToReferrerDto.create()
          .apply(referenceAttributeValueDao.getMap(
              new ResourceReferenceAttributeSpecificationByValueId(resourceId)));
      return Multimaps.transformValues(references, resourceLoader);
    }
  }

}
