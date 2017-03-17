package fi.thl.termed.service.node.specification;


import com.google.common.collect.Multimap;
import fi.thl.termed.domain.Attribute;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.NodeQuery;
import fi.thl.termed.domain.Type;
import fi.thl.termed.util.StringUtils;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.MatchNone;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Specification;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class NodeQueryToSpecification {

  private NodeQueryToSpecification() {
  }

  public static AndSpecification<NodeId, Node> toSpecification(Type type, NodeQuery.Where where) {
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

    List<Specification<NodeId, Node>> specifications = new ArrayList<>();

    for (String key : whereProperties.keySet()) {
      if (textAttributeIds.contains(key)) {
        List<Specification<NodeId, Node>> propertyValueSpecs = new ArrayList<>();

        for (String value : whereProperties.get(key)) {
          for (String token : StringUtils.tokenize(value)) {
            propertyValueSpecs.add(new NodesByPropertyPrefix(key, token));
          }
        }

        specifications.add(new OrSpecification<>(propertyValueSpecs));
      } else {
        // if queried property key is not present, we can't match anything with it,
        // e.g. when we search for "definition:cat" and this type does not even have
        // a attribute "definition", simply treat is like it doesn't match.
        specifications.add(new MatchNone<>());
      }
    }

    return new OrSpecification<>(specifications);
  }

  private static AndSpecification<NodeId, Node> referenceSpecification(
      Set<String> referenceAttributeIds, Multimap<String, UUID> whereReferences) {

    List<Specification<NodeId, Node>> specifications = new ArrayList<>();

    for (String key : whereReferences.keySet()) {
      if (referenceAttributeIds.contains(key)) {
        // multiple values for same key are considered disjunctive, i.e. user searches
        // for "related" is "cat", "dog", we accept values that or "cat" OR "dog" but it
        // must be one of them as this query is inside of conjunctive query
        List<Specification<NodeId, Node>> referenceValuesSpecs = new ArrayList<>();
        for (UUID value : whereReferences.get(key)) {
          if (value != null) {
            referenceValuesSpecs.add(new NodesByReference(key, value));
          } else {
            referenceValuesSpecs.add(new NodesWithoutReferences(key));
          }
        }
        specifications.add(new OrSpecification<>(referenceValuesSpecs));
      } else {
        // if queried reference key is not present, we can't match anything,
        // e.g. when we search for "related:cat" and this type does not even have
        // a attribute "related", simply exclude all values of this type from results
        specifications.add(new MatchNone<>());
      }
    }

    return new AndSpecification<>(specifications);
  }

}
