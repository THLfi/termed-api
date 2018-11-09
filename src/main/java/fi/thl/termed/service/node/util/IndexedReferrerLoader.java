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
import fi.thl.termed.service.node.internal.NodeReferrers;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import fi.thl.termed.util.service.Service;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
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
  private Set<Select> selects;

  public IndexedReferrerLoader(Service<NodeId, Node> nodeService, User user) {
    this.nodeService = nodeService;
    this.user = user;
    this.selects = singleton(new SelectAll());
  }

  public IndexedReferrerLoader(Service<NodeId, Node> nodeService, User user, Set<Select> selects) {
    this.nodeService = nodeService;
    this.user = user;
    this.selects = selects;
  }

  @Override
  public ImmutableList<Node> apply(Node node, String attributeId) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));

    Query<NodeId, Node> query = new Query<>(selects,
        new NodeReferrers(new NodeId(node), attributeId));

    try (Stream<Node> results = nodeService.values(query, user)) {
      Map<NodeId, Node> referrerValueMap = results.collect(toMap(Node::identifier, n -> n));

      Collection<NodeId> referrerIds = ImmutableSet.copyOf(node.getReferrers().get(attributeId));

      if (!referrerValueMap.keySet().equals(referrerIds)) {
        log.error("Index may be corrupted or outdated, requested: {}, found: {}",
            referrerIds, referrerValueMap.keySet());
      }

      return referrerIds.stream()
          .filter(referrerValueMap::containsKey)
          .map(referrerValueMap::get)
          .collect(toImmutableList());
    }
  }

}
