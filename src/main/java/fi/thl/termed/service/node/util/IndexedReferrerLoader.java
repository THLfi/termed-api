package fi.thl.termed.service.node.util;

import static fi.thl.termed.util.query.Queries.query;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.internal.NodeReferrers;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesById;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.query.AndSpecification;
import fi.thl.termed.util.query.OrSpecification;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
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

  private static final Logger log = LoggerFactory.getLogger(IndexedReferrerLoader.class);

  private final Service<NodeId, Node> nodeService;
  private final User user;
  private final List<Select> selects;

  private final Cache<NodeId, Node> referrerCache;

  public IndexedReferrerLoader(Service<NodeId, Node> nodeService, User user) {
    this(nodeService, user, ImmutableList.of(new SelectAll()));
  }

  public IndexedReferrerLoader(Service<NodeId, Node> nodeService, User user, List<Select> selects) {
    this(nodeService, user, selects, CacheBuilder.newBuilder().softValues().build());
  }

  public IndexedReferrerLoader(Service<NodeId, Node> nodeService, User user, List<Select> selects,
      Cache<NodeId, Node> referrerCache) {
    this.nodeService = nodeService;
    this.user = user;
    this.selects = selects;
    this.referrerCache = referrerCache;
  }

  @Override
  public ImmutableList<Node> apply(Node node, String attributeId) {
    Collection<NodeId> referrerIds = node.getReferrers().get(attributeId);

    List<NodeId> missingReferrerIds = new ArrayList<>();
    Map<NodeId, Node> referrers = new LinkedHashMap<>();

    referrerIds.forEach(refId -> {
      Node cachedRef = referrerCache.getIfPresent(refId);
      if (cachedRef != null) {
        referrers.put(refId, cachedRef);
      } else {
        missingReferrerIds.add(refId);
        referrers.put(refId, null);
      }
    });

    // all ref values found from cache, return results
    if (missingReferrerIds.isEmpty()) {
      return ImmutableList.copyOf(referrers.values());
    }

    boolean loadAllRefs = missingReferrerIds.size() == referrerIds.size();

    Specification<NodeId, Node> missingReferrersSpec;
    if (loadAllRefs || missingReferrerIds.size() > 20) {
      missingReferrersSpec = new NodeReferrers(node.identifier(), attributeId);
    } else {
      missingReferrersSpec = OrSpecification.or(missingReferrerIds.stream()
          .map(refId -> AndSpecification.and(
              NodesByGraphId.of(refId.getTypeGraphId()),
              NodesByTypeId.of(refId.getTypeId()),
              NodesById.of(refId.getId())))
          .collect(toList()));
    }

    try (Stream<Node> results = nodeService.values(
        query(selects, missingReferrersSpec, emptyList(), -1), user)) {

      Map<NodeId, Node> referrerValuesMap = results.collect(toMap(Node::identifier, n -> n));

      missingReferrerIds.forEach(refId -> {
        Node referrer = referrerValuesMap.remove(refId);

        if (referrer != null) {
          referrers.put(refId, referrer);
          referrerCache.put(refId, referrer);
        } else {
          logMissingReferrerValue(node.identifier(), attributeId, refId);
        }
      });

      if (loadAllRefs && !referrerValuesMap.isEmpty()) {
        referrerValuesMap.keySet()
            .forEach(k -> logUnexpectedReferrerValue(node.identifier(), attributeId, k));
      }
    }

    return ImmutableList.copyOf(referrers.values());
  }

  private void logMissingReferrerValue(NodeId nodeId, String attributeId, NodeId referrerId) {
    log.warn(
        "Index may be corrupted or outdated. Node {} is missing referrers.{}.{} from the index.",
        nodeId, attributeId, referrerId);
  }

  private void logUnexpectedReferrerValue(NodeId nodeId, String attributeId, NodeId referrerId) {
    log.warn(
        "Index may be corrupted or outdated. Node {} has unexpected referrers.{}.{} in the index.",
        nodeId, attributeId, referrerId);
  }

}
