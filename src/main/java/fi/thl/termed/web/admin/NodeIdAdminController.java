package fi.thl.termed.web.admin;

import static fi.thl.termed.util.collect.StreamUtils.findFirstAndClose;
import static fi.thl.termed.util.collect.StreamUtils.toImmutableListAndClose;
import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.query.Queries.query;
import static fi.thl.termed.util.query.Sorts.sort;
import static fi.thl.termed.util.query.Sorts.sortDesc;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Multimaps;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByNumberRange;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeIdAdminController {

  @Autowired
  private Service<NodeId, Node> nodeService;

  private TimeBasedGenerator timeBasedGenerator = Generators.timeBasedGenerator();

  @DeleteMapping(value = "/graphs/{graphId}/node-ids")
  @ResponseStatus(NO_CONTENT)
  public void regenerateGraphNodeIds(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    if (user.getAppRole() == AppRole.SUPERUSER) {
      valuesInPages(NodesByGraphId.of(graphId), 10_000, user)
          .forEachRemaining(nodes -> regenerateNodeIds(nodes, sync, user));
    } else {
      throw new AccessDeniedException("");
    }
  }

  @DeleteMapping("/graphs/{graphId}/types/{typeId}/node-ids")
  @ResponseStatus(NO_CONTENT)
  public void regenerateTypeNodeIds(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    if (user.getAppRole() == AppRole.SUPERUSER) {
      valuesInPages(and(NodesByGraphId.of(graphId), NodesByTypeId.of(typeId)), 10_000, user)
          .forEachRemaining(nodes -> regenerateNodeIds(nodes, sync, user));
    } else {
      throw new AccessDeniedException("");
    }
  }

  private Iterator<List<Node>> valuesInPages(
      Specification<NodeId, Node> specification, int pageSize, User user) {

    Optional<Long> upperOptional = findFirstAndClose(
        nodeService.values(query(specification, sortDesc("number"), 1), user))
        .map(Node::getNumber);

    if (!upperOptional.isPresent()) {
      return Collections.emptyIterator();
    }

    long upperNumber = upperOptional.get();

    return new AbstractIterator<List<Node>>() {
      long lowerNumber = 0;

      @Override
      protected List<Node> computeNext() {
        List<Node> nodes = toImmutableListAndClose(nodeService.values(query(
            and(specification, NodesByNumberRange.of(lowerNumber, upperNumber)),
            sort("number"),
            pageSize), user));

        OptionalLong lowestNumberOptional = nodes.stream()
            .map(Node::getNumber).mapToLong(Long::longValue).max();

        if (lowestNumberOptional.isPresent()) {
          lowerNumber = lowestNumberOptional.getAsLong() + 1;
          return nodes;
        } else {
          return endOfData();
        }
      }
    };
  }

  private void regenerateNodeIds(List<Node> nodes, boolean sync, User user) {
    LoadingCache<NodeId, Node> nodeCache = CacheBuilder.newBuilder().build(
        CacheLoader.from(key -> nodeService.get(key, user).orElse(null)));

    // pre-fill cache with ready available nodes
    nodes.forEach(n -> nodeCache.put(n.identifier(), n));

    Map<NodeId, NodeId> oldToNewIdMap = nodes.stream()
        .collect(toMap(
            Node::identifier,
            node -> NodeId.of(timeBasedGenerator.generate(), node.getType())));

    Stream<Node> remappedNodes = Stream.concat(
        nodes.stream().map(Node::identifier),
        nodes.stream().flatMap(node -> node.getReferrers().values().stream()))
        .distinct()
        .map(nodeCache::getUnchecked)
        .map(node ->
            Node.builder()
                .id(oldToNewIdMap.getOrDefault(
                    node.identifier(),
                    node.identifier()))
                .copyOptionalsFrom(node)
                .references(
                    Multimaps.transformValues(
                        node.getReferences(),
                        id -> oldToNewIdMap.getOrDefault(id, id)))
                .build());

    nodeService.saveAndDelete(
        remappedNodes,
        nodes.stream().map(Node::identifier),
        SaveMode.UPSERT,
        opts(sync),
        user);
  }

}
