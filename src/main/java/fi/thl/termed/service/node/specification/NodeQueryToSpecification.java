package fi.thl.termed.service.node.specification;


import com.google.common.collect.Multimap;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Attribute;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.NodeQuery;
import fi.thl.termed.domain.Type;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.MatchNone;
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

    OrSpecification<NodeId, Node> propertySpec =
        propertySpecification(textAttributeIds, where.properties);
    if (!propertySpec.getSpecifications().isEmpty()) {
      spec.and(propertySpec);
    }

    AndSpecification<NodeId, Node> referenceSpec =
        referenceSpecification(referenceAttributeIds, where.references);
    if (!referenceSpec.getSpecifications().isEmpty()) {
      spec.and(referenceSpec);
    }

    return spec;
  }

  private static OrSpecification<NodeId, Node> propertySpecification(
      Set<String> textAttributeIds, Multimap<String, String> whereProperties) {

    OrSpecification<NodeId, Node> propertySpec = new OrSpecification<>();

    whereProperties.keySet().stream().filter(textAttributeIds::contains)
        .forEach(key -> whereProperties.get(key)
            .forEach(value -> propertySpec.or(new NodesByPropertyPrefix(key, value))));

    return propertySpec;
  }

  private static AndSpecification<NodeId, Node> referenceSpecification(
      Set<String> referenceAttributeIds, Multimap<String, UUID> whereReferences) {

    AndSpecification<NodeId, Node> referenceSpec = new AndSpecification<>();

    for (String key : whereReferences.keySet()) {
      if (referenceAttributeIds.contains(key)) {
        // multiple values for same key are considered disjunctive, i.e. user searches
        // for "related" is "cat", "dog", we accept values that or "cat" OR "dog" but it
        // must be one of them as this query is inside of conjunctive query
        OrSpecification<NodeId, Node> referenceValuesSpec = new OrSpecification<>();
        for (UUID value : whereReferences.get(key)) {
          referenceValuesSpec.or(new NodesByReference(key, value));
        }
        referenceSpec.and(referenceValuesSpec);
      } else {
        // if queried reference key is not present, we can't match anything,
        // e.g. when we search for "related:cat" and this type does not even have
        // a attribute "related", simply exclude all values of this type from results
        referenceSpec.and(new MatchNone<>());
        break;
      }
    }

    return referenceSpec;
  }

}
