package fi.thl.termed.service.node.internal;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import fi.thl.termed.util.spring.exception.BadRequestException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class AttributeValueInitializingNodeService
    extends ForwardingService<NodeId, Node> {

  private BiFunction<TypeId, User, Optional<Type>> typeSource;

  public AttributeValueInitializingNodeService(Service<NodeId, Node> delegate,
      BiFunction<TypeId, User, Optional<Type>> typeSource) {
    super(delegate);
    this.typeSource = typeSource;
  }

  @Override
  public Stream<NodeId> save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    Map<TextAttributeId, TextAttribute> textAttributeCache = new HashMap<>();
    Map<ReferenceAttributeId, ReferenceAttribute> refAttributeCache = new HashMap<>();
    return super.save(
        nodes.map(node -> resolveAttributes(node, user, textAttributeCache, refAttributeCache)),
        mode, opts, user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    Map<TextAttributeId, TextAttribute> textAttributeCache = new HashMap<>();
    Map<ReferenceAttributeId, ReferenceAttribute> refAttributeCache = new HashMap<>();
    return super.save(
        resolveAttributes(node, user, textAttributeCache, refAttributeCache), mode, opts, user);
  }

  private Node resolveAttributes(Node node, User user,
      Map<TextAttributeId, TextAttribute> textAttributeCache,
      Map<ReferenceAttributeId, ReferenceAttribute> refAttributeCache) {

    Type type = typeSource.apply(node.getType(), user).orElseThrow(
        () -> new BadRequestException("Type '" + node.getType().getId() + "' not found"));

    node.getProperties().forEach((attributeId, value) -> {
      TextAttribute textAttribute = textAttributeCache.computeIfAbsent(
          new TextAttributeId(node.getType(), attributeId),
          textAttributeId -> type.getTextAttributes().stream()
              .filter(typeAttr -> Objects.equals(typeAttr.getId(), textAttributeId.getId()))
              .findAny().orElseThrow(() -> new BadRequestException(
                  "Unknown text attribute '" + attributeId + "' for " + type.getId())));

      value.setRegex(textAttribute.getRegex());
    });

    node.getReferences().forEach((attributeId, value) -> {
      ReferenceAttribute refAttribute = refAttributeCache.computeIfAbsent(
          new ReferenceAttributeId(node.getType(), attributeId),
          refAttributeId -> type.getReferenceAttributes().stream()
              .filter(typeAttr -> Objects.equals(typeAttr.getId(), refAttributeId.getId()))
              .findAny().orElseThrow(() -> new BadRequestException(
                  "Unknown reference attribute '" + attributeId + "' for " + type.getId())));

      value.setType(refAttribute.getRange());
    });

    return node;
  }

}
