package fi.thl.termed.service.impl;

import com.google.common.base.Function;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.aspect.Profile;
import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.User;
import fi.thl.termed.index.Index;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.sql.ResourcesBySchemeId;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Sets.difference;
import static org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization;

@Transactional
public class SchemeServiceImpl implements Service<UUID, Scheme> {

  private Repository<UUID, Scheme> schemeRepository;
  private Repository<ResourceId, Resource> resourceRepository;
  private Index<ResourceId, Resource> resourceIndex;
  private Dao<ResourceId, Resource> resourceDao;
  private Function<Scheme, Scheme> schemeIdResolver;
  private Function<Scheme, Scheme> schemeIdValidator;

  public SchemeServiceImpl(Repository<UUID, Scheme> schemeRepository,
                           Repository<ResourceId, Resource> resourceRepository,
                           Index<ResourceId, Resource> resourceIndex,
                           Dao<UUID, Scheme> schemeDao,
                           Dao<ResourceId, Resource> resourceDao) {
    this.schemeRepository = schemeRepository;
    this.resourceRepository = resourceRepository;
    this.resourceIndex = resourceIndex;
    this.resourceDao = resourceDao;
    this.schemeIdResolver = new SchemeIdResolver(schemeDao);
    this.schemeIdValidator = new SchemeIdValidator();
  }

  @Override
  public List<Scheme> get(User currentUser) {
    return schemeRepository.get();
  }

  @Override
  public List<Scheme> get(Specification<UUID, Scheme> specification, User currentUser) {
    return schemeRepository.get(specification);
  }

  @Override
  public List<Scheme> get(Query query, User currentUser) {
    // search not supported, return all schemes
    return schemeRepository.get();
  }

  @Override
  public Scheme get(UUID schemeId, User currentUser) {
    return schemeRepository.get(schemeId);
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public void save(List<Scheme> schemes, User currentUser) {
    for (Scheme scheme : schemes) {
      save(scheme, currentUser);
    }
  }

  @Profile
  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public void save(Scheme scheme, User currentUser) {
    scheme = schemeIdValidator.apply(schemeIdResolver.apply(scheme));
    List<ResourceId> oldResourceIds = getResourceIds(scheme.getId());
    schemeRepository.save(scheme);
    reindexSchemeResourcesAfterCommit(oldResourceIds, getResourceIds(scheme.getId()));
  }

  private List<ResourceId> getResourceIds(UUID schemeId) {
    return resourceDao.getKeys(new ResourcesBySchemeId(schemeId));
  }

  private void reindexSchemeResourcesAfterCommit(final List<ResourceId> oldResourceIds,
                                                 final List<ResourceId> newResourceIds) {
    registerSynchronization(new TransactionSynchronizationAdapter() {
      @Override
      public void afterCommit() {
        cleanUp();
        reindex();
      }

      private void cleanUp() {
        for (ResourceId removedId : difference(copyOf(oldResourceIds), copyOf(newResourceIds))) {
          resourceIndex.deleteFromIndex(removedId);
        }
      }

      private void reindex() {
        if (!newResourceIds.isEmpty()) {
          resourceIndex.reindex(newResourceIds, new Function<ResourceId, Resource>() {
            public Resource apply(ResourceId id) {
              return resourceRepository.get(id);
            }
          });
        }
      }
    });
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(UUID schemeId, User currentUser) {
    List<ResourceId> ids = resourceDao.getKeys(new ResourcesBySchemeId(schemeId));

    schemeRepository.delete(schemeId);

    for (ResourceId id : ids) {
      resourceIndex.deleteFromIndex(id);
    }
  }

}
