package fi.thl.termed.service.node.internal;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimaps;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.StrictLangValue;
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
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class AttributeValueInitializingNodeService extends ForwardingService<NodeId, Node> {

  private BiFunction<TypeId, User, Optional<Type>> typeSource;

  public AttributeValueInitializingNodeService(Service<NodeId, Node> delegate,
      BiFunction<TypeId, User, Optional<Type>> typeSource) {
    super(delegate);
    this.typeSource = typeSource;
  }

  @Override
  public Stream<NodeId> save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    LoadingCache<TextAttributeId, TextAttribute> textAttributeCache = initTextAttrCache(user);
    LoadingCache<ReferenceAttributeId, ReferenceAttribute> refAttributeCache = initRefAttrCache(
        user);
    return super.save(
        nodes.map(node -> resolveAttributes(node, textAttributeCache, refAttributeCache)),
        mode, opts, user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    LoadingCache<TextAttributeId, TextAttribute> textAttributeCache = initTextAttrCache(user);
    LoadingCache<ReferenceAttributeId, ReferenceAttribute> refAttributeCache = initRefAttrCache(
        user);
    return super.save(
        resolveAttributes(node, textAttributeCache, refAttributeCache), mode, opts, user);
  }

  private LoadingCache<TextAttributeId, TextAttribute> initTextAttrCache(User user) {
    Function<TextAttributeId, TextAttribute> loader = attr -> {
      Type domain = typeSource.apply(attr.getDomainId(), user)
          .orElseThrow(() -> new BadRequestException(
              "Type '" + attr.getDomainId().getId() + "' not found"));
      return domain.getTextAttributes().stream()
          .filter(typeAttr -> Objects.equals(typeAttr.getId(), attr.getId()))
          .findAny()
          .orElseThrow(() -> new BadRequestException(
              "Unknown text attribute '" + attr.getId() + "' for " + attr.getDomainId().getId()));
    };

    return CacheBuilder.newBuilder().build(CacheLoader.from(loader::apply));
  }

  private LoadingCache<ReferenceAttributeId, ReferenceAttribute> initRefAttrCache(User user) {
    Function<ReferenceAttributeId, ReferenceAttribute> loader = attr -> {
      Type domain = typeSource.apply(attr.getDomainId(), user)
          .orElseThrow(() -> new BadRequestException(
              "Type '" + attr.getDomainId().getId() + "' not found"));
      return domain.getReferenceAttributes().stream()
          .filter(typeAttr -> Objects.equals(typeAttr.getId(), attr.getId()))
          .findAny()
          .orElseThrow(() -> new BadRequestException(
              "Unknown reference attribute '" + attr.getId() + "' for " + attr.getDomainId()
                  .getId()));
    };

    return CacheBuilder.newBuilder().build(CacheLoader.from(loader::apply));
  }

  private Node resolveAttributes(Node node,
      LoadingCache<TextAttributeId, TextAttribute> textAttributeCache,
      LoadingCache<ReferenceAttributeId, ReferenceAttribute> refAttributeCache) {

    node.setProperties(
        Multimaps.transformEntries(node.getProperties(), (attributeId, value) -> {
          TextAttribute attribute = textAttributeCache.getUnchecked(
              new TextAttributeId(node.getType(), attributeId));
          return new StrictLangValue(value.getLang(), value.getValue(), attribute.getRegex());
        }));

    node.setReferences(
        Multimaps.transformEntries(node.getReferences(), (attributeId, value) -> {
          ReferenceAttribute attribute = refAttributeCache.getUnchecked(
              new ReferenceAttributeId(node.getType(), attributeId));
          return new NodeId(value.getId(), attribute.getRange());
        }));

    return node;
  }

}
