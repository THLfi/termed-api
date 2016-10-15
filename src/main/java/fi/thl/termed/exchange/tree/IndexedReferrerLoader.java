package fi.thl.termed.exchange.tree;

import java.util.List;
import java.util.function.Function;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.SpecificationQuery;
import fi.thl.termed.spesification.resource.ResourceReferrers;

import static fi.thl.termed.util.specification.SpecificationQuery.Engine.LUCENE;

/**
 * Load resource referrers from index.
 */
public class IndexedReferrerLoader implements Function<Resource, List<Resource>> {

  private Service<ResourceId, Resource> resourceService;
  private User user;
  private ReferenceAttributeId attributeId;
  private ClassId rangeId;

  public IndexedReferrerLoader(Service<ResourceId, Resource> resourceService,
                               User user, ReferenceAttributeId attributeId, ClassId rangeId) {
    this.resourceService = resourceService;
    this.user = user;
    this.attributeId = attributeId;
    this.rangeId = rangeId;
  }

  @Override
  public List<Resource> apply(Resource resource) {
    return resourceService.get(
        new SpecificationQuery<ResourceId, Resource>(
            new ResourceReferrers(new ResourceId(resource), attributeId, rangeId), LUCENE), user);
  }

}
