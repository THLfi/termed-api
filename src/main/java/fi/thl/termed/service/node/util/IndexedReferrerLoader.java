package fi.thl.termed.service.node.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.internal.NodeReferrers;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.collect.Arg;
import fi.thl.termed.util.service.Service;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load node referrers from index.
 */
public class IndexedReferrerLoader implements BiFunction<Node, String, List<Node>> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Service<NodeId, Node> nodeService;
  private User user;
  private Arg[] args;

  public IndexedReferrerLoader(Service<NodeId, Node> nodeService, User user) {
    this(nodeService, user, new Arg[0]);
  }

  public IndexedReferrerLoader(Service<NodeId, Node> nodeService, User user, Arg... args) {
    this.nodeService = nodeService;
    this.user = user;
    this.args = args;
  }

  @Override
  public List<Node> apply(Node node, String attributeId) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));

    Map<NodeId, Node> referrerValueMap = Maps.uniqueIndex(
        nodeService.get(new NodeReferrers(new NodeId(node), attributeId), user, args).iterator(),
        Node::identifier);

    Collection<NodeId> referrerIds = node.getReferrers().get(attributeId);

    if (!referrerValueMap.keySet().equals(referrerIds)) {
      log.error("Index may be corrupted or outdated, requested: {}, found: {}",
          referrerIds, referrerValueMap.keySet());
    }

    return referrerIds.stream()
        .filter(referrerValueMap::containsKey)
        .map(referrerValueMap::get)
        .collect(Collectors.toList());
  }

}
