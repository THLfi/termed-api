package fi.thl.termed.service.node.internal;

import static java.util.Collections.singletonList;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public class AttributeValueInitializingNodeService
    extends ForwardingService<NodeId, Node> {

  private BiFunction<TypeId, User, Optional<Type>> typeSource;

  public AttributeValueInitializingNodeService(Service<NodeId, Node> delegate,
      BiFunction<TypeId, User, Optional<Type>> typeSource) {
    super(delegate);
    this.typeSource = typeSource;
  }

  @Override
  public List<NodeId> save(List<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    resolveAttributes(nodes, user);
    return super.save(nodes, mode, opts, user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    resolveAttributes(singletonList(node), user);
    return super.save(node, mode, opts, user);
  }

  @Override
  public List<NodeId> saveAndDelete(List<Node> saves, List<NodeId> deletes, SaveMode mode,
      WriteOptions opts, User user) {
    resolveAttributes(saves, user);
    return super.saveAndDelete(saves, deletes, mode, opts, user);
  }

  private void resolveAttributes(List<Node> nodes, User user) {
    Map<TextAttributeId, TextAttribute> textAttributeCache = new HashMap<>();
    Map<ReferenceAttributeId, ReferenceAttribute> refAttributeCache = new HashMap<>();
    nodes.forEach(node -> resolveAttributes(node, user, textAttributeCache, refAttributeCache));
  }

  private void resolveAttributes(Node node, User user,
      Map<TextAttributeId, TextAttribute> textAttributeCache,
      Map<ReferenceAttributeId, ReferenceAttribute> refAttributeCache) {

    Type type = typeSource.apply(node.getType(), user).orElseThrow(BadRequestException::new);

    node.getProperties().forEach((attributeId, value) -> {
      TextAttribute textAttribute = textAttributeCache.computeIfAbsent(
          new TextAttributeId(node.getType(), attributeId),
          textAttributeId -> type.getTextAttributes().stream()
              .filter(typeAttr -> Objects.equals(typeAttr.getId(), textAttributeId.getId()))
              .findAny().orElseThrow(BadRequestException::new));

      value.setRegex(textAttribute.getRegex());
    });

    node.getReferences().forEach((attributeId, value) -> {
      ReferenceAttribute refAttribute = refAttributeCache.computeIfAbsent(
          new ReferenceAttributeId(node.getType(), attributeId),
          refAttributeId -> type.getReferenceAttributes().stream()
              .filter(typeAttr -> Objects.equals(typeAttr.getId(), refAttributeId.getId()))
              .findAny().orElseThrow(BadRequestException::new));

      value.setType(refAttribute.getRange());
    });
  }

}
