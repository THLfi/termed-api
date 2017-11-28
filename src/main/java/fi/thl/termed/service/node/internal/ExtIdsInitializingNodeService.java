package fi.thl.termed.service.node.internal;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.Strings.isNullOrEmpty;
import static fi.thl.termed.util.query.AndSpecification.and;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodeById;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.NamedSequenceService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class ExtIdsInitializingNodeService extends ForwardingService<NodeId, Node> {

  private NamedSequenceService<TypeId> nodeSequenceService;
  private BiFunction<TypeId, User, Optional<Type>> typeSource;
  private BiFunction<GraphId, User, Optional<Graph>> graphSource;

  public ExtIdsInitializingNodeService(Service<NodeId, Node> delegate,
      NamedSequenceService<TypeId> nodeSequenceService,
      BiFunction<TypeId, User, Optional<Type>> typeSource,
      BiFunction<GraphId, User, Optional<Graph>> graphSource) {
    super(delegate);
    this.nodeSequenceService = nodeSequenceService;
    this.typeSource = typeSource;
    this.graphSource = graphSource;
  }

  @Override
  public List<NodeId> save(List<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    addSerialNumbersWithDefaultCodesAndUris(nodes, user);
    return super.save(nodes, mode, opts, user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    addSerialNumbersWithDefaultCodesAndUris(singletonList(node), user);
    return super.save(node, mode, opts, user);
  }

  @Override
  public List<NodeId> saveAndDelete(List<Node> saves, List<NodeId> deletes, SaveMode mode,
      WriteOptions opts, User user) {
    addSerialNumbersWithDefaultCodesAndUris(saves, user);
    return super.saveAndDelete(saves, deletes, mode, opts, user);
  }

  private void addSerialNumbersWithDefaultCodesAndUris(List<Node> nodes, User user) {
    nodes.stream().collect(groupingBy(Node::getType)).forEach((type, instances) -> {

      List<Node> newNodes = new ArrayList<>();

      for (Node node : instances) {
        try (Stream<Node> stream = getValueStream(and(
            new NodesByGraphId(node.getTypeGraphId()),
            new NodesByTypeId(node.getTypeId()),
            new NodeById(node.getId())), user)) {
          Optional<Node> old = stream.findAny();
          if (old.isPresent()) {
            node.setNumber(old.get().getNumber());
          } else {
            newNodes.add(node);
          }
        }
      }

      if (!newNodes.isEmpty()) {
        Set<String> usedCodes = nodes.stream()
            .map(Node::getCode).filter(Objects::nonNull).collect(toSet());
        Set<String> usedUris = nodes.stream()
            .map(Node::getUri).filter(Objects::nonNull).collect(toSet());

        long number = nodeSequenceService.getAndAdvance(type, (long) (newNodes.size()), user);

        for (Node node : newNodes) {
          node.setNumber(number++);
          addDefaultCodeIfMissing(node, usedCodes, user);
          addDefaultUriIfMissing(node, usedUris, user);
        }
      }
    });
  }

  private void addDefaultCodeIfMissing(Node node, Set<String> usedCodes, User user) {
    if (isNullOrEmpty(node.getCode())) {
      Type type = typeSource.apply(node.getType(), user)
          .orElseThrow(IllegalStateException::new);

      String code = type.getNodeCodePrefix()
          .map(prefix -> prefix + node.getNumber())
          .orElseGet(() -> UPPER_CAMEL.to(LOWER_HYPHEN, node.getTypeId()) + "-" + node.getNumber());

      if (!usedCodes.contains(code)) {
        node.setCode(code);
      }
    }
  }

  private void addDefaultUriIfMissing(Node node, Set<String> usedUris, User user) {
    if (isNullOrEmpty(node.getUri()) && !isNullOrEmpty(node.getCode())) {
      Graph graph = graphSource.apply(node.getTypeGraph(), user)
          .orElseThrow(IllegalStateException::new);

      graph.getUri().map(ns -> ns + node.getCode()).ifPresent(uri -> {
        if (!usedUris.contains(uri)) {
          node.setUri(uri);
        }
      });
    }
  }

}
