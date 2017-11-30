package fi.thl.termed.service.node.util;

import fi.thl.termed.domain.Attribute;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import java.util.Optional;
import java.util.function.Function;

/**
 * Functions to convert IDs to URIs using given data sources
 */
public final class UriResolvers {

  private UriResolvers() {
  }

  public static Function<TypeId, Optional<String>> typeUriResolver(
      Function<TypeId, Optional<Type>> typeProvider) {
    return id -> typeProvider.apply(id).flatMap(Type::getUri);
  }

  public static Function<TextAttributeId, Optional<String>> textAttrUriResolver(
      Function<TypeId, Optional<Type>> typeProvider) {
    return textAttributeId -> typeProvider.apply(textAttributeId.getDomainId())
        .flatMap(type -> type.getTextAttributes().stream()
            .filter(attribute -> attribute.identifier().equals(textAttributeId))
            .findAny()
            .flatMap(Attribute::getUri));
  }

  public static Function<ReferenceAttributeId, Optional<String>> refAttrUriResolver(
      Function<TypeId, Optional<Type>> typeProvider) {
    return referenceAttributeId -> typeProvider.apply(referenceAttributeId.getDomainId())
        .flatMap(type -> type.getReferenceAttributes().stream()
            .filter(attribute -> attribute.identifier().equals(referenceAttributeId))
            .findAny()
            .flatMap(Attribute::getUri));
  }

  public static Function<NodeId, Optional<String>> nodeUriResolver(
      Function<NodeId, Optional<Node>> nodeProvider) {
    return id -> nodeProvider.apply(id).map(Node::getUri);
  }

}
