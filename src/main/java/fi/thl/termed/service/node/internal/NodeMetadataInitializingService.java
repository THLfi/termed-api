package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.collect.FunctionUtils.memoize;
import static fi.thl.termed.util.query.AndSpecification.and;
import static java.util.Optional.ofNullable;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByCode;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.specification.NodesByUri;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.service.NamedSequenceService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class NodeMetadataInitializingService extends PreSaveNodeInitializingService {

  private NamedSequenceService<TypeId> nodeSequenceService;
  private BiFunction<TypeId, User, Type> types;
  private BiFunction<GraphId, User, Graph> graphs;
  private String defaultNs;

  public NodeMetadataInitializingService(
      Service<NodeId, Node> delegate,
      NamedSequenceService<TypeId> nodeSequenceService,
      BiFunction<TypeId, User, Optional<Type>> typeSource,
      BiFunction<GraphId, User, Optional<Graph>> graphSource,
      String defaultNs) {
    super(delegate);
    this.nodeSequenceService = nodeSequenceService;
    this.types = (typeId, user) -> typeSource.apply(typeId, user)
        .orElseThrow(IllegalStateException::new);
    this.graphs = (graphId, user) -> graphSource.apply(graphId, user)
        .orElseThrow(IllegalStateException::new);
    this.defaultNs = defaultNs;
  }

  @Override
  public Stream<Node> insert(Stream<Node> newNodes, WriteOptions opts, User user) {
    LocalDateTime now = LocalDateTime.now();

    BiFunction<TypeId, Long, String> codeGenerator = codeGenerator(user);
    BiFunction<GraphId, String, String> uriGenerator = uriGenerator(opts.getUriNamespace(), user);

    return newNodes.map(node -> {
      Long number = nodeSequenceService.getAndAdvance(node.getType(), user);

      // generate code once (and only once) if needed
      Supplier<String> codeGen = memoize(() -> codeGenerator.apply(node.getType(), number));
      String code = node.getCode().orElseGet(() -> opts.isGenerateCodes() ? codeGen.get() : null);
      String uri = node.getUri().orElseGet(() -> opts.isGenerateUris() ?
          uriGenerator.apply(node.getTypeGraph(), ofNullable(code).orElseGet(codeGen)) : null);

      return Node.builderFromCopyOf(node)
          .number(number)
          .code(code)
          .uri(uri)
          .createdBy(user.getUsername())
          .createdDate(now)
          .lastModifiedBy(user.getUsername())
          .lastModifiedDate(now)
          .build();
    });
  }

  @Override
  public Stream<Node> update(Stream<Tuple2<Node, Node>> oldAndNewNodes, WriteOptions opts,
      User user) {
    LocalDateTime now = LocalDateTime.now();

    return oldAndNewNodes.map(oldAndNewNode -> {
      Node oldNode = oldAndNewNode._1;
      Node newNode = oldAndNewNode._2;

      return Node.builderFromCopyOf(newNode)
          .number(oldNode.getNumber())
          .createdBy(oldNode.getCreatedBy())
          .createdDate(oldNode.getCreatedDate())
          .lastModifiedBy(user.getUsername())
          .lastModifiedDate(now)
          .build();
    });
  }

  @Override
  public Stream<Node> upsert(Stream<Tuple2<Optional<Node>, Node>> oldAndNewNodes,
      WriteOptions opts, User user) {

    LocalDateTime now = LocalDateTime.now();

    BiFunction<TypeId, Long, String> codeGenerator = codeGenerator(user);
    BiFunction<GraphId, String, String> uriGenerator = uriGenerator(opts.getUriNamespace(), user);

    return oldAndNewNodes.map(oldAndNewNode -> {
      Optional<Node> optionalOldNode = oldAndNewNode._1;
      Node node = oldAndNewNode._2;

      if (optionalOldNode.isPresent()) {
        Node oldNode = optionalOldNode.get();

        return Node.builderFromCopyOf(node)
            .number(oldNode.getNumber())
            .createdBy(oldNode.getCreatedBy())
            .createdDate(oldNode.getCreatedDate())
            .lastModifiedBy(user.getUsername())
            .lastModifiedDate(now)
            .build();
      } else {
        Long number = nodeSequenceService.getAndAdvance(node.getType(), user);

        Supplier<String> codeGen = memoize(() -> codeGenerator.apply(node.getType(), number));
        String code = node.getCode().orElseGet(() -> opts.isGenerateCodes() ? codeGen.get() : null);
        String uri = node.getUri().orElseGet(() -> opts.isGenerateUris() ?
            uriGenerator.apply(node.getTypeGraph(), ofNullable(code).orElseGet(codeGen)) : null);

        return Node.builderFromCopyOf(node)
            .number(number)
            .code(code)
            .uri(uri)
            .createdBy(user.getUsername())
            .createdDate(now)
            .lastModifiedBy(user.getUsername())
            .lastModifiedDate(now)
            .build();
      }
    });
  }

  private BiFunction<TypeId, Long, String> codeGenerator(User user) {
    Function<TypeId, String> nodeCodePrefixes = memoize(
        typeId ->
            types.apply(typeId, user).getNodeCodePrefixOrDefault());

    return (typeId, number) -> {
      String code = nodeCodePrefixes.apply(typeId) + number;

      if (!existsNodeWithCode(typeId, code, user)) {
        return code;
      }

      return null;
    };
  }

  private BiFunction<GraphId, String, String> uriGenerator(Optional<String> requestNamespace,
      User user) {
    Function<GraphId, String> uriNamespaces = memoize(
        graphId -> requestNamespace.orElseGet(() ->
            graphs.apply(graphId, user).getUri().orElse(defaultNs)));

    return (graphId, code) -> {
      if (code == null) {
        return null;
      }

      String uri = uriNamespaces.apply(graphId) + code;

      if (!existsNodeWithUri(graphId, uri, user)) {
        return uri;
      }

      return null;
    };
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
