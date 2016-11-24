package fi.thl.termed.service.node.util;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.internal.NodeReferences;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.service.Service;

/**
 * Load node references from index.
 */
public class IndexedReferenceLoader implements BiFunction<Node, String, List<Node>> {

  private Service<NodeId, Node> nodeService;
  private User user;

  public IndexedReferenceLoader(Service<NodeId, Node> nodeService, User user) {
    this.nodeService = nodeService;
    this.user = user;
  }

  @Override
  public List<Node> apply(Node node, String attributeId) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));

    List<Node> populatedReferences = nodeService.get(
        new NodeReferences(new NodeId(node), attributeId), user);

    Map<NodeId, Node> populatedReferencesIndex = new HashMap<>();
    populatedReferences.forEach(r -> populatedReferencesIndex.put(new NodeId(r), r));

    // We could populate values one by one, but searching all references and then populating from
    // them is faster. We how ever can't just return populatedReferences as it may contain
    // extra values (no attribute domain checks are done) and it's order does not match actual
    // order given for node references.
    return node.getReferences().get(attributeId).stream()
        .map(populatedReferencesIndex::get).collect(Collectors.toList());
  }

}
