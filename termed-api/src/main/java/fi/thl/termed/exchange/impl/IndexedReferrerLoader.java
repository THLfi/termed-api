package fi.thl.termed.exchange.impl;

import com.google.common.base.Function;

import java.util.List;

import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;

import static java.lang.String.format;

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
    return resourceService.get(new Query(
        format("+scheme.id:%s +%s.id:%s", resource.getSchemeId(), attributeId, resource.getId()),
        Integer.MAX_VALUE), user);
  }

}