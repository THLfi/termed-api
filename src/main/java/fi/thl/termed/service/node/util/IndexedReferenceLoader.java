package fi.thl.termed.service.node.util;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableList;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.internal.NodeReferences;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import fi.thl.termed.util.service.Service;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load node references from index.
 */
public class IndexedReferenceLoader implements BiFunction<Node, String, ImmutableList<Node>> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Service<NodeId, Node> nodeService;
  private User user;
  private List<Select> selects;

  public IndexedReferenceLoader(Service<NodeId, Node> nodeService, User user) {
    this.nodeService = nodeService;
    this.user = user;
    this.selects = ImmutableList.of(new SelectAll());
  }

  public IndexedReferenceLoader(Service<NodeId, Node> nodeService, User user,
      List<Select> selects) {
    this.nodeService = nodeService;
    this.user = user;
    this.selects = selects;
  }

  @Override
  public ImmutableList<Node> apply(Node node, String attributeId) {
    Query<NodeId, Node> query = new Query<>(selects,
        new NodeReferences(node.identifier(), attributeId));

    try (Stream<Node> results = nodeService.values(query, user)) {
      Map<NodeId, Node> referenceValuesMap = results.collect(toMap(Node::identifier, n -> n));

      ImmutableList<Node> populatedReferences = node.getReferences().get(attributeId).stream()
          .filter(referenceId -> referenceValuesMap.containsKey(referenceId) ||
              logMissingReferenceValue(node.identifier(), attributeId, referenceId))
          .map(referenceValuesMap::remove)
          .collect(toImmutableList());

      if (!referenceValuesMap.isEmpty()) {
        referenceValuesMap.keySet()
            .forEach(k -> logUnexpectedReferenceValue(node.identifier(), attributeId, k));
      }

      return populatedReferences;
    }
  }

  private boolean logMissingReferenceValue(NodeId nodeId, String attributeId, NodeId referrerId) {
    log.warn(
        "Index may be corrupted or outdated. Node {} is missing references.{}.{} from the index.",
        nodeId, attributeId, referrerId);
    return false;
  }

  private void logUnexpectedReferenceValue(NodeId nodeId, String attributeId, NodeId referenceId) {
    log.warn(
        "Index may be corrupted or outdated. Node {} has unexpected references.{}.{} in the index.",
        nodeId, attributeId, referenceId);
  }

}
