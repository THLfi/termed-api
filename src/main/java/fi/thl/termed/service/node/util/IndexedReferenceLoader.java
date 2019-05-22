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
import fi.thl.termed.service.node.internal.NodeReferences;
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
 * Load node references from index.
 */
public class IndexedReferenceLoader implements BiFunction<Node, String, ImmutableList<Node>> {

  private static final Logger log = LoggerFactory.getLogger(IndexedReferenceLoader.class);

  private final Service<NodeId, Node> nodeService;
  private final User user;
  private final List<Select> selects;

  private final Cache<NodeId, Node> referenceCache;

  public IndexedReferenceLoader(Service<NodeId, Node> nodeService, User user) {
    this(nodeService, user, ImmutableList.of(new SelectAll()));
  }

  public IndexedReferenceLoader(Service<NodeId, Node> nodeService, User user,
      List<Select> selects) {
    this(nodeService, user, selects, CacheBuilder.newBuilder().softValues().build());
  }

  public IndexedReferenceLoader(Service<NodeId, Node> nodeService, User user, List<Select> selects,
      Cache<NodeId, Node> referenceCache) {
    this.nodeService = nodeService;
    this.user = user;
    this.selects = selects;
    this.referenceCache = referenceCache;
  }

  @Override
  public ImmutableList<Node> apply(Node node, String attributeId) {
    Collection<NodeId> referenceIds = node.getReferences().get(attributeId);

    List<NodeId> missingReferenceIds = new ArrayList<>();
    Map<NodeId, Node> references = new LinkedHashMap<>();

    referenceIds.forEach(refId -> {
      Node cachedRef = referenceCache.getIfPresent(refId);
      if (cachedRef != null) {
        references.put(refId, cachedRef);
      } else {
        missingReferenceIds.add(refId);
        references.put(refId, null);
      }
    });

    // all ref values found from cache, return results
    if (missingReferenceIds.isEmpty()) {
      return ImmutableList.copyOf(references.values());
    }

    boolean loadAllRefs = missingReferenceIds.size() == referenceIds.size();

    // if all or many refs are missing, find all refs, otherwise query just the missing ones
    Specification<NodeId, Node> missingReferencesSpec;
    if (loadAllRefs || missingReferenceIds.size() > 20) {
      missingReferencesSpec = new NodeReferences(node.identifier(), attributeId);
    } else {
      missingReferencesSpec = OrSpecification.or(missingReferenceIds.stream()
          .map(refId -> AndSpecification.and(
              NodesByGraphId.of(refId.getTypeGraphId()),
              NodesByTypeId.of(refId.getTypeId()),
              NodesById.of(refId.getId())))
          .collect(toList()));
    }

    try (Stream<Node> results = nodeService.values(
        query(selects, missingReferencesSpec, emptyList(), -1), user)) {

      Map<NodeId, Node> referenceValuesMap = results.collect(toMap(Node::identifier, n -> n));

      missingReferenceIds.forEach(refId -> {
        Node reference = referenceValuesMap.remove(refId);

        if (reference != null) {
          references.put(refId, reference);
          referenceCache.put(refId, reference);
        } else {
          logMissingReferenceValue(node.identifier(), attributeId, refId);
        }
      });

      if (loadAllRefs && !referenceValuesMap.isEmpty()) {
        referenceValuesMap.keySet()
            .forEach(k -> logUnexpectedReferenceValue(node.identifier(), attributeId, k));
      }
    }

    return ImmutableList.copyOf(references.values());
  }

  private void logMissingReferenceValue(NodeId nodeId, String attributeId, NodeId referrerId) {
    log.warn(
        "Index may be corrupted or outdated. Node {} is missing references.{}.{} from the index.",
        nodeId, attributeId, referrerId);
  }

  private void logUnexpectedReferenceValue(NodeId nodeId, String attributeId, NodeId referenceId) {
    log.warn(
        "Index may be corrupted or outdated. Node {} has unexpected references.{}.{} in the index.",
        nodeId, attributeId, referenceId);
  }

}
