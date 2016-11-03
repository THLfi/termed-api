package fi.thl.termed.web.node.json;

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

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

@RestController
@RequestMapping("/api/graphs/{graphId}/types/{typeId}/nodes/{id}")
public class NodeReferencesReadController {

  @Autowired
  private Service<NodeId, Node> nodeService;

  @GetJsonMapping("/references/{attributeId}")
  public List<Node> getReferences(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User user) {

    Node root = nodeService.get(new NodeId(id, typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Function<Node, List<Node>> loadReferences =
        new IndexedReferenceLoader(nodeService, user, attributeId);

    return loadReferences.apply(root);
  }

  @GetJsonMapping("/references/{attributeId}/recursive")
  public Set<Node> getRecursiveReferences(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User user) {

    Node root = nodeService.get(new NodeId(id, typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Function<Node, List<Node>> loadReferences =
        new IndexedReferenceLoader(nodeService, user, attributeId);

    Set<Node> results = new LinkedHashSet<>();
    for (Node neighbour : loadReferences.apply(root)) {
      collectNeighbours(results, neighbour, loadReferences);
    }
    return results;
  }


  @GetJsonMapping("/referrers/{attributeId}")
  public List<Node> getReferrers(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User user) {

    Node root = nodeService.get(new NodeId(id, typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Function<Node, List<Node>> loadReferrers =
        new IndexedReferrerLoader(nodeService, user, attributeId);

    return loadReferrers.apply(root);
  }

  @GetJsonMapping("/referrers/{attributeId}/recursive")
  public Set<Node> getRecursiveReferrers(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User user) {

    Node root = nodeService.get(new NodeId(id, typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Function<Node, List<Node>> loadReferrers =
        new IndexedReferrerLoader(nodeService, user, attributeId);

    Set<Node> results = new LinkedHashSet<>();
    for (Node neighbour : loadReferrers.apply(root)) {
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
