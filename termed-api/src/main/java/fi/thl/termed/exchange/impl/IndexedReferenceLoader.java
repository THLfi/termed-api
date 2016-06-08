package fi.thl.termed.exchange.impl;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;

import static com.google.common.base.Functions.forMap;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.lang.String.format;

/**
 * Load resource references from index.
 */
public class IndexedReferenceLoader implements Function<Resource, List<Resource>> {

  private Service<ResourceId, Resource> resourceService;
  private User user;
  private String attributeId;

  public IndexedReferenceLoader(Service<ResourceId, Resource> resourceService,
                                User user, String attributeId) {
    this.resourceService = resourceService;
    this.user = user;
    this.attributeId = attributeId;
  }

  @Override
  public List<Resource> apply(Resource resource) {
    List<Resource> references = resourceService.get(new Query(
        format("+scheme.id:%s +referrers.%s.id:%s",
               resource.getSchemeId(), attributeId, resource.getId()),
        Integer.MAX_VALUE), user);

    return order(references, resource.getReferenceIds().get(attributeId));
  }

  // preserve reference order
  private List<Resource> order(List<Resource> references, Iterable<ResourceId> orderedIds) {
    Map<ResourceId, Resource> referenceIndex = Maps.uniqueIndex(references, new ToResourceId());
    return transform(newArrayList(orderedIds), forMap(referenceIndex));
  }

}