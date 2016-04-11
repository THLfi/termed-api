package fi.thl.termed.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.ResourceIndex;
import fi.thl.termed.repository.ResourceRepository;
import fi.thl.termed.repository.SchemeRepository;
import fi.thl.termed.repository.dao.ResourceDao;
import fi.thl.termed.repository.spesification.Specification;

import static fi.thl.termed.util.ObjectUtils.coalesce;
import static fi.thl.termed.util.UUIDs.nameUUIDFromString;
import static org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization;

@Service("resourceService")
@Transactional
public class ResourceServiceImpl
    implements ResourceService, ApplicationListener<ContextRefreshedEvent> {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private SchemeRepository schemeRepository;

  @Autowired
  private ResourceDao resourceDao;

  @Autowired
  private ResourceRepository resourceRepository;

  @Autowired
  private ResourceIndex resourceIndex;

  // check if reindexing is needed on context refresh (e.g. after app is loaded)
  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    if (resourceIndex.indexSize() == 0) {
      reindex(resourceDao.getKeys());
    }
  }

  private void reindex(List<ResourceId> resourceIds) {
    if (!resourceIds.isEmpty()) {
      resourceIndex.reindex(resourceIds, new Function<List<ResourceId>, List<Resource>>() {
        public List<Resource> apply(List<ResourceId> ids) {
          return resourceRepository.get(ids);
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
  public Resource save(ResourceId resourceId, Resource resource, User currentUser) {
    resource.setId(resourceId.getId());
    return save(resource, currentUser);
  }

  @Override
  public Resource save(Resource resource, User currentUser) {
    save(Collections.singletonList(resource), currentUser);
    return get(new ResourceId(resource), currentUser);
  }

  @Override
  public int save(List<Resource> resources, User user) {
    String lastModifiedBy = user.getUsername();
    Date lastModifiedDate = new Date();

    for (Resource resource : resources) {
      prepareResource(resource, lastModifiedBy, lastModifiedDate);
    }

    log.info("Saving {} resources", resources.size());

    resourceRepository.save(resources);

    log.info("Done.");

    reindexAfterCommit(resources);

    return resources.size();
  }

  private void prepareResource(Resource resource, String lastModifiedBy, Date lastModifiedDate) {
    Scheme scheme = resource.getScheme();

    if (scheme.getId() == null) {
      scheme.setId(coalesce(nameUUIDFromString(scheme.getCode()),
                            nameUUIDFromString(scheme.getUri())));
    }

    if (resource.getId() == null) {
      resource.setId(coalesce(nameUUIDFromString(resource.getCode()),
                              nameUUIDFromString(resource.getUri()),
                              UUID.randomUUID()));
    }

    // created info will be overwritten by repository if there is an older version of the resource
    resource.setCreatedBy(lastModifiedBy);
    resource.setCreatedDate(lastModifiedDate);
    resource.setLastModifiedBy(lastModifiedBy);
    resource.setLastModifiedDate(lastModifiedDate);

    for (Resource reference : resource.getReferences().values()) {
      prepareReference(resource, reference);
    }
  }

  private void prepareReference(Resource resource, Resource reference) {
    if (reference.getSchemeId() == null) {
      reference.setScheme(new Scheme(resource.getSchemeId()));
    }
    if (reference.getTypeId() == null) {
      reference.setType(new Class(resource.getTypeId()));
    }
    if (reference.getId() == null) {
      reference.setId(coalesce(nameUUIDFromString(reference.getCode()),
                               nameUUIDFromString(reference.getUri())));
    }
  }

  private void reindexAfterCommit(final List<Resource> resources) {
    registerSynchronization(new TransactionSynchronizationAdapter() {
      public void afterCommit() {
        reindex(collectIdsForReindexing(resources));
      }
    });
  }

  private List<ResourceId> collectIdsForReindexing(List<Resource> resources) {
    Set<ResourceId> indexIds = Sets.newHashSet();
    for (Resource resource : resources) {
      indexIds.add(new ResourceId(resource.getSchemeId(), resource.getTypeId(), resource.getId()));
      indexIds.addAll(resource.getReferenceIds().values());
    }
    return Lists.newArrayList(indexIds);
  }

  @Override
  public void delete(ResourceId resourceId, User currentUser) {
    resourceIndex.deleteFromIndex(resourceId);
    resourceRepository.delete(resourceId);
  }

}
