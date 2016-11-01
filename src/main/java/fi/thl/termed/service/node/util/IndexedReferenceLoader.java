package fi.thl.termed.service.node.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.internal.NodeReferences;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Query;

import static fi.thl.termed.util.specification.Query.Engine.LUCENE;

/**
 * Load node references from index.
 */
public class IndexedReferenceLoader implements Function<Node, List<Node>> {

  private Service<NodeId, Node> nodeService;
  private User user;
  private String attributeId;

  public IndexedReferenceLoader(Service<NodeId, Node> nodeService,
                                User user, String attributeId) {
    this.nodeService = nodeService;
    this.user = user;
    this.attributeId = attributeId;
  }

  @Override
  public List<Node> apply(Node node) {
    List<Node> populatedReferences = nodeService.get(new Query<>(
        new NodeReferences(new NodeId(node), attributeId), LUCENE), user);

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
