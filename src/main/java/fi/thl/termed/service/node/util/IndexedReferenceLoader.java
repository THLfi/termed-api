package fi.thl.termed.service.node.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.internal.NodeReferences;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.service.Service;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load node references from index.
 */
public class IndexedReferenceLoader implements BiFunction<Node, String, List<Node>> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Service<NodeId, Node> nodeService;
  private User user;

  public IndexedReferenceLoader(Service<NodeId, Node> nodeService, User user) {
    this.nodeService = nodeService;
    this.user = user;
  }

  @Override
  public List<Node> apply(Node node, String attributeId) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));

    Map<NodeId, Node> referenceValueMap = Maps.uniqueIndex(
        nodeService.get(new NodeReferences(new NodeId(node), attributeId), user),
        Node::identifier);

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
