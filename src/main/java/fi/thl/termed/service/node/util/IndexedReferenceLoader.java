package fi.thl.termed.service.node.util;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.internal.NodeReferences;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import fi.thl.termed.util.service.Service;
import java.util.Map;
import java.util.Set;
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
  private Set<Select> selects;

  public IndexedReferenceLoader(Service<NodeId, Node> nodeService, User user) {
    this.nodeService = nodeService;
    this.user = user;
    this.selects = singleton(new SelectAll());
  }

  public IndexedReferenceLoader(Service<NodeId, Node> nodeService, User user,
      Set<Select> selects) {
    this.nodeService = nodeService;
    this.user = user;
    this.selects = selects;
  }

  @Override
  public ImmutableList<Node> apply(Node node, String attributeId) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));

    Query<NodeId, Node> query = new Query<>(selects,
        new NodeReferences(new NodeId(node), attributeId));

    try (Stream<Node> results = nodeService.values(query, user)) {
      Map<NodeId, Node> referenceValueMap = results.collect(toMap(Node::identifier, n -> n));

      Set<NodeId> referenceIds = ImmutableSet.copyOf(node.getReferences().get(attributeId));

      if (!referenceValueMap.keySet().equals(referenceIds)) {
        log.error("Index may be corrupted or outdated, requested: {}, found: {}",
            referenceIds, referenceValueMap.keySet());
      }

      return referenceIds.stream()
          .filter(referenceValueMap::containsKey)
          .map(referenceValueMap::get)
          .collect(toImmutableList());
    }
  }

}
