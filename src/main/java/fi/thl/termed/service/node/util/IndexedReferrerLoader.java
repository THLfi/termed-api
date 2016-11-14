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
import fi.thl.termed.service.node.internal.NodeReferrers;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Query;

import static fi.thl.termed.util.specification.Query.Engine.LUCENE;

/**
 * Load node referrers from index.
 */
public class IndexedReferrerLoader implements BiFunction<Node, String, List<Node>> {

  private Service<NodeId, Node> nodeService;
  private User user;

  public IndexedReferrerLoader(Service<NodeId, Node> nodeService, User user) {
    this.nodeService = nodeService;
    this.user = user;
  }

  @Override
  public List<Node> apply(Node node, String attributeId) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));

    List<Node> populatedReferrers = nodeService.get(new Query<>(
        new NodeReferrers(new NodeId(node), attributeId), LUCENE), user).getValues();

    Map<NodeId, Node> populatedReferrersIndex = new HashMap<>();
    populatedReferrers.forEach(r -> populatedReferrersIndex.put(new NodeId(r), r));

    return node.getReferrers().get(attributeId).stream()
        .map(populatedReferrersIndex::get).collect(Collectors.toList());
  }

}
