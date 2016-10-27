package fi.thl.termed.web.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.resource.util.IndexedReferenceLoader;
import fi.thl.termed.service.resource.util.IndexedReferrerLoader;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

@RestController
@RequestMapping("/api/schemes/{schemeId}/classes/{typeId}/resources/{id}")
public class ResourceReferencesReadController {

  @Autowired
  private Service<ResourceId, Resource> resourceService;

  @GetJsonMapping("/references/{attributeId}")
  public List<Resource> getReferences(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User user) {

    Resource root = resourceService.get(new ResourceId(id, typeId, schemeId), user)
        .orElseThrow(NotFoundException::new);

    Function<Resource, List<Resource>> loadReferences =
        new IndexedReferenceLoader(resourceService, user, attributeId);

    return loadReferences.apply(root);
  }

  @GetJsonMapping("/references/{attributeId}/recursive")
  public Set<Resource> getRecursiveReferences(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User user) {

    Resource root = resourceService.get(new ResourceId(id, typeId, schemeId), user)
        .orElseThrow(NotFoundException::new);

    Function<Resource, List<Resource>> loadReferences =
        new IndexedReferenceLoader(resourceService, user, attributeId);

    Set<Resource> results = new LinkedHashSet<>();
    for (Resource neighbour : loadReferences.apply(root)) {
      collectNeighbours(results, neighbour, loadReferences);
    }
    return results;
  }


  @GetJsonMapping("/referrers/{attributeId}")
  public List<Resource> getReferrers(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User user) {

    Resource root = resourceService.get(new ResourceId(id, typeId, schemeId), user)
        .orElseThrow(NotFoundException::new);

    Function<Resource, List<Resource>> loadReferrers =
        new IndexedReferrerLoader(resourceService, user, attributeId);

    return loadReferrers.apply(root);
  }

  @GetJsonMapping("/referrers/{attributeId}/recursive")
  public Set<Resource> getRecursiveReferrers(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User user) {

    Resource root = resourceService.get(new ResourceId(id, typeId, schemeId), user)
        .orElseThrow(NotFoundException::new);

    Function<Resource, List<Resource>> loadReferrers =
        new IndexedReferrerLoader(resourceService, user, attributeId);

    Set<Resource> results = new LinkedHashSet<>();
    for (Resource neighbour : loadReferrers.apply(root)) {
      collectNeighbours(results, neighbour, loadReferrers);
    }
    return results;
  }

  private <T> void collectNeighbours(Set<T> results, T node, Function<T, List<T>> getNeighbours) {
    if (!results.contains(node)) {
      results.add(node);
      for (T neighbour : getNeighbours.apply(node)) {
        collectNeighbours(results, neighbour, getNeighbours);
      }
    }
  }

}
