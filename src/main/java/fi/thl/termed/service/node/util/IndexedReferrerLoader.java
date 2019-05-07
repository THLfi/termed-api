package fi.thl.termed.service.node.util;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableList;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.internal.NodeReferrers;
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
 * Load node referrers from index.
 */
public class IndexedReferrerLoader implements BiFunction<Node, String, ImmutableList<Node>> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Service<NodeId, Node> nodeService;
  private User user;
  private List<Select> selects;

  public IndexedReferrerLoader(Service<NodeId, Node> nodeService, User user) {
    this.nodeService = nodeService;
    this.user = user;
    this.selects = ImmutableList.of(new SelectAll());
  }

  public IndexedReferrerLoader(Service<NodeId, Node> nodeService, User user, List<Select> selects) {
    this.nodeService = nodeService;
    this.user = user;
    this.selects = selects;
  }

  @Override
  public ImmutableList<Node> apply(Node node, String attributeId) {
    Query<NodeId, Node> query = new Query<>(selects,
        new NodeReferrers(node.identifier(), attributeId));

    try (Stream<Node> results = nodeService.values(query, user)) {
      Map<NodeId, Node> referrerValuesMap = results.collect(toMap(Node::identifier, n -> n));

      ImmutableList<Node> populatedReferrers = node.getReferrers().get(attributeId).stream()
          .filter(referrerId -> referrerValuesMap.containsKey(referrerId) ||
              reportMissingReferrerValue(node.identifier(), attributeId, referrerId))
          .map(referrerValuesMap::remove)
          .collect(toImmutableList());

      if (!referrerValuesMap.isEmpty()) {
        referrerValuesMap.keySet()
            .forEach(k -> logUnexpectedReferrerValue(node.identifier(), attributeId, k));
      }

      return populatedReferrers;
    }
  }

  private boolean reportMissingReferrerValue(NodeId nodeId, String attributeId, NodeId referrerId) {
    log.warn(
        "Index may be corrupted or outdated. Node {} is missing referrers.{}.{} from the index.",
        nodeId, attributeId, referrerId);
    return false;
  }

  private void logUnexpectedReferrerValue(NodeId nodeId, String attributeId, NodeId referrerId) {
    log.warn(
        "Index may be corrupted or outdated. Node {} has unexpected referrers.{}.{} in the index.",
        nodeId, attributeId, referrerId);
  }

}
