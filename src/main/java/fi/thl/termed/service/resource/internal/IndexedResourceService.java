package fi.thl.termed.service.resource.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.ApplicationReadyEvent;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.MatchAllQuery;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Query.Engine;

public class IndexedResourceService extends ForwardingService<ResourceId, Resource> {

  private Index<ResourceId, Resource> index;
  private User indexer = new User("indexer", "", AppRole.ADMIN);

  public IndexedResourceService(
      Service<ResourceId, Resource> delegate, Index<ResourceId, Resource> index) {
    super(delegate);
    this.index = index;
  }

  @Subscribe
  public void initIndexOn(ApplicationReadyEvent e) {
    if (index.isEmpty()) {
      // reindex all
      index.index(super.getKeys(new MatchAllQuery<>(), indexer), key -> super.get(key, indexer));
    }
  }

  @Override
  public List<ResourceId> save(List<Resource> resources, User user) {
    Set<ResourceId> reindexSet = Sets.newHashSet();

    for (Resource resource : resources) {
      reindexSet.add(new ResourceId(resource));
      reindexSet.addAll(resourceRelatedIds(new ResourceId(resource)));
    }

    List<ResourceId> ids = super.save(resources, user);

    for (Resource resource : resources) {
      reindexSet.addAll(resourceRelatedIds(new ResourceId(resource)));
    }
    asyncReindex(reindexSet);
    return ids;
  }

  @Override
  public ResourceId save(Resource resource, User user) {
    Set<ResourceId> reindexSet = Sets.newHashSet();

    reindexSet.add(new ResourceId(resource));
    reindexSet.addAll(resourceRelatedIds(new ResourceId(resource)));

    ResourceId id = super.save(resource, user);

    reindexSet.addAll(resourceRelatedIds(new ResourceId(resource)));
    reindex(reindexSet);
    return id;
  }

  @Override
  public void delete(ResourceId resourceId, User user) {
    Set<ResourceId> reindexSet = Sets.newHashSet();

    reindexSet.add(resourceId);
    reindexSet.addAll(resourceRelatedIds(resourceId));

    super.delete(resourceId, user);

    asyncReindex(reindexSet);
  }

  @Override
  public List<Resource> get(Query<ResourceId, Resource> specification, User user) {
    return specification.getEngine() == Engine.LUCENE ? index.get(specification)
                                                      : super.get(specification, user);
  }

  @Override
  public List<ResourceId> getKeys(Query<ResourceId, Resource> specification, User user) {
    return specification.getEngine() == Engine.LUCENE ? index.getKeys(specification)
                                                      : super.getKeys(specification, user);
  }

  private Set<ResourceId> resourceRelatedIds(ResourceId resourceId) {
    Set<ResourceId> refValues = new HashSet<>();
    Optional<Resource> resourceOptional = super.get(resourceId, indexer);

    if (resourceOptional.isPresent()) {
      refValues.addAll(resourceOptional.get().getReferences().values());
      refValues.addAll(resourceOptional.get().getReferrers().values());
    }

    return refValues;
  }

  private void reindex(Set<ResourceId> ids) {
    for (ResourceId id : ids) {
      Optional<Resource> resource = super.get(id, indexer);
      if (resource.isPresent()) {
        index.index(id, resource.get());
      } else {
        index.delete(id);
      }
    }
  }

  private void asyncReindex(Set<ResourceId> ids) {
    if (!ids.isEmpty()) {
      index.index(ImmutableList.copyOf(ids), id -> {
        return IndexedResourceService.super.get(id, indexer);
      });
    }
  }

}
