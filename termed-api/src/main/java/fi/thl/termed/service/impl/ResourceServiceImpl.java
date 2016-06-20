package fi.thl.termed.service.impl;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.User;
import fi.thl.termed.index.Index;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.sql.ResourceByCode;
import fi.thl.termed.spesification.sql.ResourceByUri;
import fi.thl.termed.spesification.sql.ResourceReferenceAttributeResourcesByValueId;
import fi.thl.termed.spesification.sql.ResourceReferenceAttributeValuesByResourceId;
import fi.thl.termed.spesification.sql.SchemeByCode;
import fi.thl.termed.spesification.sql.SchemeByUri;
import fi.thl.termed.util.UUIDs;

import static fi.thl.termed.util.ObjectUtils.coalesce;
import static org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization;

@Transactional
public class ResourceServiceImpl
    implements Service<ResourceId, Resource>, ApplicationListener<ContextRefreshedEvent> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Repository<ResourceId, Resource> resourceRepository;
  private Index<ResourceId, Resource> resourceIndex;

  private Dao<UUID, Scheme> schemeDao;
  private Dao<ResourceId, Resource> resourceDao;
  private Dao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao;

  public ResourceServiceImpl(
      Repository<ResourceId, Resource> resourceRepository,
      Index<ResourceId, Resource> resourceIndex,
      Dao<UUID, Scheme> schemeDao,
      Dao<ResourceId, Resource> resourceDao,
      Dao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao) {
    this.resourceRepository = resourceRepository;
    this.resourceIndex = resourceIndex;
    this.schemeDao = schemeDao;
    this.resourceDao = resourceDao;
    this.referenceAttributeValueDao = referenceAttributeValueDao;
  }

  // check if reindexing is needed on context refresh (e.g. after app is loaded)
  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    if (resourceIndex.indexSize() == 0) {
      reindex(resourceDao.getKeys());
    }
  }

  private void reindex(List<ResourceId> resourceIds) {
    if (!resourceIds.isEmpty()) {
      resourceIndex.reindex(resourceIds, new Function<ResourceId, Resource>() {
        public Resource apply(ResourceId id) {
          return resourceRepository.get(id);
        }
      });
    }
  }

  @Override
  public List<Resource> get(User currentUser) {
    return resourceIndex.query(new Query());
  }

  @Override
  public List<Resource> get(Specification<ResourceId, Resource> specification, User currentUser) {
    return resourceRepository.get(specification);
  }

  @Override
  public List<Resource> get(Query query, User currentUser) {
    return resourceIndex.query(query);
  }

  @Override
  public Resource get(ResourceId resourceId, User currentUser) {
    return resourceRepository.get(resourceId);
  }

  @Override
  public void save(Resource resource, User currentUser) {
    prepareResource(resource, currentUser, new Date());
    Set<ResourceId> affectedIds = indexSet(new ResourceId(resource), resource);

    resourceRepository.save(resource);

    for (ResourceId affectedId : affectedIds) {
      resourceIndex.reindex(affectedId, resourceRepository.get(affectedId));
    }
  }

  @Override
  public void save(List<Resource> resources, User user) {
    long startTime = System.nanoTime();

    log.info("Preparing {} resources", resources.size());

    Date now = new Date();
    Set<ResourceId> affectedIds = Sets.newHashSet();

    for (Resource resource : resources) {
      prepareResource(resource, user, now);
      affectedIds.addAll(indexSet(new ResourceId(resource), resource));
    }

    log.info("Saving", resources.size());

    resourceRepository.save(resources);

    log.info("Done in {} ms", (System.nanoTime() - startTime) / 1000000);

    reindexAfterCommit(Lists.newArrayList(affectedIds));
  }

  private Set<ResourceId> indexSet(ResourceId resourceId, Resource resource) {
    Set<ResourceId> indexSet = Sets.newHashSet();

    indexSet.add(resourceId);
    indexSet.addAll(resource.getReferenceIds().values());
    indexSet.addAll(relatedIds(resourceId));

    return indexSet;
  }

  private Resource prepareResource(Resource resource, User user, Date now) {
    Preconditions.checkNotNull(resource.getScheme());
    Preconditions.checkNotNull(resource.getType());

    resolveSchemeId(resource.getScheme());
    resolveId(resource);

    resource.ensureId();

    for (Resource reference : resource.getReferences().values()) {
      if (reference.getScheme() == null) {
        reference.setScheme(resource.getScheme());
      }
      if (reference.getTypeId() == null) {
        reference.setType(resource.getType());
      }

      resolveSchemeId(reference.getScheme());
      resolveId(reference);

      Preconditions.checkState(reference.getId() != null);
    }

    ResourceId resourceId = new ResourceId(resource);

    if (resourceRepository.exists(resourceId)) {
      Resource oldResource = resourceDao.get(resourceId);
      resource.setCreatedBy(oldResource.getCreatedBy());
      resource.setCreatedDate(oldResource.getCreatedDate());
    } else {
      resource.setCreatedBy(user.getUsername());
      resource.setCreatedDate(now);
    }

    resource.setLastModifiedBy(user.getUsername());
    resource.setLastModifiedDate(now);

    return resource;
  }

  private Set<ResourceId> relatedIds(ResourceId resourceId) {
    Set<ResourceId> relatedIds = Sets.newHashSet();

    // ids of referenced resources
    relatedIds.addAll(referenceAttributeValueDao.getValues(
        new ResourceReferenceAttributeValuesByResourceId(resourceId)));

    // ids of referring resources
    relatedIds.addAll(Lists.transform(referenceAttributeValueDao.getKeys(
        new ResourceReferenceAttributeResourcesByValueId(resourceId)),
                                      new ResourceAttributeValueIdToResourceId()));

    return relatedIds;
  }

  private void resolveId(Resource resource) {
    if (resource.getId() == null) {
      resource.setId(coalesce(resolveResourceIdForCode(resource.getCode()),
                              resolveResourceIdForUri(resource.getUri())));
    }
  }

  private UUID resolveResourceIdForCode(String code) {
    if (code != null) {
      List<ResourceId> ids = resourceDao.getKeys(new ResourceByCode(code));
      return !ids.isEmpty() ? ids.get(0).getId() : UUIDs.nameUUIDFromString(code);
    }
    return null;
  }

  private UUID resolveResourceIdForUri(String uri) {
    if (uri != null) {
      List<ResourceId> ids = resourceDao.getKeys(new ResourceByUri(uri));
      return !ids.isEmpty() ? ids.get(0).getId() : UUIDs.nameUUIDFromString(uri);
    }
    return null;
  }

  private void resolveSchemeId(Scheme scheme) {
    if (scheme.getId() == null) {
      scheme.setId(coalesce(resolveSchemeIdForCode(scheme.getCode()),
                            resolveSchemeIdForUri(scheme.getUri())));
    }
  }

  private UUID resolveSchemeIdForCode(String code) {
    if (code != null) {
      List<UUID> ids = schemeDao.getKeys(new SchemeByCode(code));
      return !ids.isEmpty() ? ids.get(0) : UUIDs.nameUUIDFromString(code);
    }
    return null;
  }

  private UUID resolveSchemeIdForUri(String uri) {
    if (uri != null) {
      List<UUID> ids = schemeDao.getKeys(new SchemeByUri(uri));
      return !ids.isEmpty() ? ids.get(0) : UUIDs.nameUUIDFromString(uri);
    }
    return null;
  }

  private void reindexAfterCommit(final List<ResourceId> resourceIds) {
    registerSynchronization(new TransactionSynchronizationAdapter() {
      public void afterCommit() {
        reindex(resourceIds);
      }
    });
  }

  @Override
  public void delete(ResourceId resourceId, User currentUser) {
    resourceIndex.deleteFromIndex(resourceId);
    reindexAfterCommit(Lists.newArrayList(relatedIds(resourceId)));
    resourceRepository.delete(resourceId);
  }

  private class ResourceAttributeValueIdToResourceId
      implements Function<ResourceAttributeValueId, ResourceId> {

    public ResourceId apply(ResourceAttributeValueId input) {
      return input.getResourceId();
    }
  }

}
