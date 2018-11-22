package fi.thl.termed.service.node.internal;

import static com.google.common.base.Suppliers.memoize;
import static fi.thl.termed.util.collect.OptionalUtils.lazyFindFirst;
import static fi.thl.termed.util.query.AndSpecification.and;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodeById;
import fi.thl.termed.service.node.specification.NodesByCode;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.specification.NodesByUri;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.NamedSequenceService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ExtIdsInitializingNodeService extends ForwardingService<NodeId, Node> {

  private NamedSequenceService<TypeId> nodeSequenceService;
  private BiFunction<TypeId, User, Type> typeSource;
  private BiFunction<GraphId, User, Graph> graphSource;

  public ExtIdsInitializingNodeService(Service<NodeId, Node> delegate,
      NamedSequenceService<TypeId> nodeSequenceService,
      BiFunction<TypeId, User, Optional<Type>> typeSource,
      BiFunction<GraphId, User, Optional<Graph>> graphSource) {
    super(delegate);
    this.nodeSequenceService = nodeSequenceService;
    this.typeSource = (typeId, user) -> typeSource.apply(typeId, user)
        .orElseThrow(IllegalStateException::new);
    this.graphSource = (graphId, user) -> graphSource.apply(graphId, user)
        .orElseThrow(IllegalStateException::new);
  }

  @Override
  public Stream<NodeId> save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    Multimap<TypeId, String> generatedCodes = HashMultimap.create();
    Multimap<GraphId, String> generatedUris = HashMultimap.create();
    return super.save(
        nodes.map(n -> addExternalIdentifiers(n, mode, opts, user, generatedCodes, generatedUris)),
        mode, opts, user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    return super.save(
        addExternalIdentifiers(node, mode, opts, user,
            HashMultimap.create(), HashMultimap.create()),
        mode, opts, user);
  }

  private Node addExternalIdentifiers(Node node,
      SaveMode mode, WriteOptions opts, User user,
      Multimap<TypeId, String> generatedCodes,
      Multimap<GraphId, String> generatedUris) {

    switch (mode) {
      case INSERT:
        return insertExtIds(node, opts, user, generatedCodes, generatedUris);
      case UPDATE:
        return updateExtIds(node,
            findCachedNode(node.identifier(), user).orElseThrow(NotFoundException::new));
      case UPSERT:
        Optional<Node> cachedNode = findCachedNode(node.identifier(), user);
        if (cachedNode.isPresent()) {
          return updateExtIds(node, cachedNode.get());
        } else {
          return insertExtIds(node, opts, user, generatedCodes, generatedUris);
        }
      default:
        throw new IllegalStateException("Unknown save mode: " + mode);
    }
  }

  private Node updateExtIds(Node node, Node cachedNode) {
    if (Objects.equals(cachedNode.getNumber(), node.getNumber())) {
      // number is correct, simply return the node
      return node;
    } else {
      return Node.builderFromCopyOf(node)
          .number(cachedNode.getNumber())
          .build();
    }
  }

  private Node insertExtIds(Node node,
      WriteOptions opts, User user,
      Multimap<TypeId, String> usedCodes,
      Multimap<GraphId, String> usedUris) {

    Node.Builder nodeBuilder = Node.builderFromCopyOf(node);

    Long number = nodeSequenceService.getAndAdvance(node.getType(), user);
    nodeBuilder.number(number);

    if (opts.isGenerateCodes() || opts.isGenerateUris()) {
      Supplier<Optional<String>> getOrGenerateCode = memoize(() -> lazyFindFirst(
          node::getCode, () -> buildDefaultCode(node.getType(), number, user)));
      Supplier<Optional<String>> getOrGenerateUri = memoize(() -> lazyFindFirst(
          node::getUri, () -> buildDefaultUri(node.getTypeGraph(), getOrGenerateCode.get(), user)));

      if (opts.isGenerateCodes()) {
        getOrGenerateCode.get().ifPresent(code -> {
          if (!usedCodes.containsEntry(node.getType(), code)) {
            nodeBuilder.code(code);
            usedCodes.put(node.getType(), code);
          } else {
            nodeBuilder.code(null);
          }
        });
      }
      if (opts.isGenerateUris()) {
        getOrGenerateUri.get().ifPresent(uri -> {
          if (!usedUris.containsEntry(node.getTypeGraph(), uri)) {
            nodeBuilder.uri(uri);
            usedUris.put(node.getTypeGraph(), uri);
          } else {
            nodeBuilder.uri(null);
          }
        });
      }
    }

    return nodeBuilder.build();
  }

  private Optional<String> buildDefaultCode(TypeId typeId, Long number, User user) {
    Type type = typeSource.apply(typeId, user);
    String defaultCode = type.getNodeCodePrefixOrDefault() + number;
    return !existsNodeWithCode(typeId, defaultCode, user) ?
        Optional.of(defaultCode) : Optional.empty();
  }

  private Optional<String> buildDefaultUri(GraphId graphId, Optional<String> code, User user) {
    Graph graph = graphSource.apply(graphId, user);

    if (graph.getUri().isPresent() && code.isPresent()) {
      String defaultUri = graph.getUri().get() + code.get();
      return !existsNodeWithUri(graphId, defaultUri, user) ?
          Optional.of(defaultUri) : Optional.empty();
    }

    return Optional.empty();
  }

  private Optional<Node> findCachedNode(NodeId nodeId, User user) {
    try (Stream<Node> nodeStream = values(new Query<>(and(
        new NodesByGraphId(nodeId.getTypeGraphId()),
        new NodesByTypeId(nodeId.getTypeId()),
        new NodeById(nodeId.getId()))), user)) {
      return nodeStream.findAny();
    }
  }

  private boolean existsNodeWithCode(TypeId typeId, String code, User user) {
    return count(and(
        new NodesByGraphId(typeId.getGraphId()),
        new NodesByTypeId(typeId.getId()),
        new NodesByCode(code)), user) > 0;
  }

  private boolean existsNodeWithUri(GraphId graphId, String uri, User user) {
    return count(and(
        new NodesByGraphId(graphId.getId()),
        new NodesByUri(uri)), user) > 0;
  }

}
