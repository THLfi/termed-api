package fi.thl.termed.service.resource.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.resource.internal.ResourceReferences;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Query;

import static fi.thl.termed.util.specification.Query.Engine.LUCENE;

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
    List<Resource> populatedReferences = resourceService.get(new Query<>(
        new ResourceReferences(new ResourceId(resource), attributeId), LUCENE), user);

    Map<ResourceId, Resource> populatedReferencesIndex = new HashMap<>();
    populatedReferences.forEach(r -> populatedReferencesIndex.put(new ResourceId(r), r));

    // We could populate values one by one, but searching all references and then populating from
    // them is faster. We how ever can't just return populatedReferences as it may contain
    // extra values (no attribute domain checks are done) and it's order does not match actual
    // order given for resource references.
    return resource.getReferences().get(attributeId).stream()
        .map(populatedReferencesIndex::get).collect(Collectors.toList());
  }

}
