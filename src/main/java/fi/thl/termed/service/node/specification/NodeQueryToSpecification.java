package fi.thl.termed.service.node.specification;


import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Attribute;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.NodeQuery;
import fi.thl.termed.domain.Type;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Specification;

public final class NodeQueryToSpecification {

  private NodeQueryToSpecification() {
  }

  public static Specification<NodeId, Node> toSpecification(Type type, NodeQuery.Where where) {
    AndSpecification<NodeId, Node> spec = new AndSpecification<>();

    spec.and(new NodesByGraphId(type.getGraphId()));
    spec.and(new NodesByTypeId(type.getId()));

    Set<String> textAttributeIds = type.getTextAttributes().stream()
        .map(Attribute::getId).collect(Collectors.toSet());
    Set<String> referenceAttributeIds = type.getReferenceAttributes().stream()
        .map(Attribute::getId).collect(Collectors.toSet());

    OrSpecification<NodeId, Node> propertySpec = new OrSpecification<>();
    where.properties.keySet().stream().filter(textAttributeIds::contains)
        .forEach(key -> where.properties.get(key)
            .forEach(value -> propertySpec.or(new NodesByPropertyPrefix(key, value))));
    if (!propertySpec.getSpecifications().isEmpty()) {
      spec.and(propertySpec);
    }

    where.references.keySet().stream().filter(referenceAttributeIds::contains)
        .forEach(key -> spec.and(where.references.get(key).stream().map(
            value -> new NodesByReference(key, value)).collect(orCollector())));

    return spec;
  }

  private static <K extends Serializable, V> Collector<Specification<K, V>, ?, OrSpecification<K, V>> orCollector() {
    return Collector.of(OrSpecification::new, OrSpecification::or, OrSpecification::or);
  }

}
