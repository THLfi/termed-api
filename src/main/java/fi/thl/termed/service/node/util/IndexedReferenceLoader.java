package fi.thl.termed.service.node.util;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toMap;

import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.internal.NodeReferences;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import fi.thl.termed.util.service.Service2;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load node references from index.
 */
public class IndexedReferenceLoader implements BiFunction<Node, String, List<Node>> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Service2<NodeId, Node> nodeService;
  private User user;
  private Set<Select> selects;

  public IndexedReferenceLoader(Service2<NodeId, Node> nodeService, User user) {
    this.nodeService = nodeService;
    this.user = user;
    this.selects = singleton(new SelectAll());
  }

  public IndexedReferenceLoader(Service2<NodeId, Node> nodeService, User user,
      Set<Select> selects) {
    this.nodeService = nodeService;
    this.user = user;
    this.selects = selects;
  }

  @Override
  public List<Node> apply(Node node, String attributeId) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));

    Query<NodeId, Node> query = new Query<>(selects,
        new NodeReferences(new NodeId(node), attributeId));

    try (Stream<Node> results = nodeService.values(query, user)) {
      Map<NodeId, Node> referenceValueMap = results.collect(toMap(Node::identifier, n -> n));

      Collection<NodeId> referenceIds = node.getReferences().get(attributeId);

      if (!referenceValueMap.keySet().equals(referenceIds)) {
        log.error("Index may be corrupted or outdated, requested: {}, found: {}",
            referenceIds, referenceValueMap.keySet());
      }

      return referenceIds.stream()
          .filter(referenceValueMap::containsKey)
          .map(referenceValueMap::get)
          .collect(Collectors.toList());
    }
  }

}
