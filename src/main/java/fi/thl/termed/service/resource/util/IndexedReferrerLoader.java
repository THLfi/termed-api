package fi.thl.termed.service.resource.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.resource.internal.ResourceReferrers;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Query;

import static fi.thl.termed.util.specification.Query.Engine.LUCENE;

/**
 * Load resource referrers from index.
 */
public class IndexedReferrerLoader implements Function<Resource, List<Resource>> {

  private Service<ResourceId, Resource> resourceService;
  private User user;
  private String attributeId;

  public IndexedReferrerLoader(Service<ResourceId, Resource> resourceService,
                               User user, String attributeId) {
    this.resourceService = resourceService;
    this.user = user;
    this.attributeId = attributeId;
  }

  @Override
  public List<Resource> apply(Resource resource) {
    List<Resource> populatedReferrers = resourceService.get(new Query<>(
        new ResourceReferrers(new ResourceId(resource), attributeId), LUCENE), user);

    Map<ResourceId, Resource> populatedReferrersIndex = new HashMap<>();
    populatedReferrers.forEach(r -> populatedReferrersIndex.put(new ResourceId(r), r));

    return resource.getReferrers().get(attributeId).stream()
        .map(populatedReferrersIndex::get).collect(Collectors.toList());
  }

}
