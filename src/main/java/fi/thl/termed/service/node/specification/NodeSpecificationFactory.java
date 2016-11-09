package fi.thl.termed.service.node.specification;


import java.util.List;

import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Specification;

public final class NodeSpecificationFactory {

  private NodeSpecificationFactory() {
  }

  public static Specification<NodeId, Node> byTypeAndCode(TypeId typeId, String nodeCode) {
    return new AndSpecification<>(
        new NodesByGraphId(typeId.getGraphId()),
        new NodesByTypeId(typeId.getId()),
        new NodesByCode(nodeCode));
  }

  public static Specification<NodeId, Node> byGraphAndUri(GraphId graphId, String nodeUri) {
    return new AndSpecification<>(
        new NodesByGraphId(graphId.getId()),
        new NodesByUri(nodeUri));
  }

  public static Specification<NodeId, Node> byAnyType(List<TypeId> typeIds) {
    OrSpecification<NodeId, Node> specs = new OrSpecification<>();
    typeIds.forEach(typeId -> specs.or(byType(typeId)));
    return specs;
  }

  public static Specification<NodeId, Node> byType(TypeId typeId) {
    return new AndSpecification<>(
        new NodesByGraphId(typeId.getGraphId()),
        new NodesByTypeId(typeId.getId()));
  }

  public static Specification<NodeId, Node> byAnyTextAttributeValuePrefix(
      List<TextAttributeId> attributeIds, List<String> prefixes) {
    OrSpecification<NodeId, Node> specs = new OrSpecification<>();
    attributeIds.forEach(attrId -> prefixes.forEach(prefix -> specs.or(
        byTextAttributeValuePrefix(attrId, prefix))));
    return specs;
  }

  public static Specification<NodeId, Node> byTextAttributeValuePrefix(
      TextAttributeId attributeId, String prefix) {
    return new AndSpecification<>(
        byType(attributeId.getDomainId()),
        new NodesByPropertyPrefix(attributeId.getId(), prefix));
  }

}
