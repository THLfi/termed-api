package fi.thl.termed.service.node.internal;

import static com.google.common.collect.Multimaps.filterValues;
import static com.google.common.collect.Multimaps.transformEntries;
import static org.assertj.core.util.Strings.isNullOrEmpty;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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

/**
 * Sets regular expressions to text attribute values and ranges to reference attribute values.
 */
public class AttributeValueInitializingNodeService extends ForwardingService<NodeId, Node> {

  private BiFunction<TypeId, User, Type> typeSource;

  public AttributeValueInitializingNodeService(Service<NodeId, Node> delegate,
      BiFunction<TypeId, User, Optional<Type>> typeSource) {
    super(delegate);
    this.typeSource = (t, u) -> typeSource.apply(t, u)
        .orElseThrow(() -> new BadRequestException("Type '" + t.getId() + "' not found"));
  }

  @Override
  public void save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    LoadingCache<TextAttributeId, TextAttribute> textAttrCache = initTextAttrCache(user);
    LoadingCache<ReferenceAttributeId, ReferenceAttribute> refAttrCache = initRefAttrCache(user);
    super.save(
        nodes.map(node -> resolveAttributes(node, textAttrCache, refAttrCache)),
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
    Function<TextAttributeId, TextAttribute> textAttrLoader =
        attr -> typeSource.apply(attr.getDomainId(), user)
            .getTextAttributes().stream()
            .filter(typeAttr -> Objects.equals(typeAttr.getId(), attr.getId()))
            .findAny()
            .orElseThrow(() -> new BadRequestException(
                "Unknown text attribute '" + attr.getId() + "' for " + attr.getDomainId().getId()));

    return CacheBuilder.newBuilder().build(CacheLoader.from(textAttrLoader::apply));
  }

  private LoadingCache<ReferenceAttributeId, ReferenceAttribute> initRefAttrCache(User user) {
    Function<ReferenceAttributeId, ReferenceAttribute> refAttrLoader =
        attr -> typeSource.apply(attr.getDomainId(), user)
            .getReferenceAttributes().stream()
            .filter(typeAttr -> Objects.equals(typeAttr.getId(), attr.getId()))
            .findAny()
            .orElseThrow(() -> new BadRequestException(
                "Unknown ref attribute '" + attr.getId() + "' for " + attr.getDomainId().getId()));

    return CacheBuilder.newBuilder().build(CacheLoader.from(refAttrLoader::apply));
  }

  private Node resolveAttributes(Node node,
      LoadingCache<TextAttributeId, TextAttribute> textAttributeCache,
      LoadingCache<ReferenceAttributeId, ReferenceAttribute> refAttributeCache) {

    return Node.builderFromCopyOf(node)
        .properties(
            transformEntries(
                filterValues(node.getProperties(), v -> !isNullOrEmpty(v.getValue())),
                (attributeId, value) -> {
                  TextAttribute attribute = textAttributeCache.getUnchecked(
                      new TextAttributeId(node.getType(), attributeId));
                  return new StrictLangValue(value.getLang(), value.getValue(),
                      attribute.getRegex());
                }))
        .references(
            transformEntries(node.getReferences(), (attributeId, value) -> {
              ReferenceAttribute attribute = refAttributeCache.getUnchecked(
                  new ReferenceAttributeId(node.getType(), attributeId));
              return new NodeId(value.getId(), attribute.getRange());
            }))
        .build();
  }

}
