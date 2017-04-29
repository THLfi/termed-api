package fi.thl.termed.service.node.specification;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.NotSpecification;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.specification.SpecificationTreeFilter;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Filters specification so that only attributes present in given readableType are accessed. Useful
 * for filtering user submitted and parsed queries.
 */
public class TypeBasedNodeSpecificationFilter implements
    Function<Specification<NodeId, Node>, Specification<NodeId, Node>> {

  private Type readableType;

  public TypeBasedNodeSpecificationFilter(Type readableType) {
    this.readableType = readableType;
  }

  @Override
  public Specification<NodeId, Node> apply(Specification<NodeId, Node> nodeSpecification) {
    AndSpecification<NodeId, Node> filtered = new AndSpecification<>();

    filtered.and(new NodesByTypeId(readableType.getId()));
    filtered.and(new NodesByGraphId(readableType.getGraphId()));

    Set<String> textAttrIds = readableType.getTextAttributes().stream()
        .map(TextAttribute::getId).collect(Collectors.toSet());
    Set<String> refAttrIds = readableType.getReferenceAttributes().stream()
        .map(ReferenceAttribute::getId).collect(Collectors.toSet());

    Predicate<Specification<NodeId, Node>> acceptedSpec =
        s -> s instanceof NotSpecification ||
            s instanceof AndSpecification ||
            s instanceof OrSpecification ||
            s instanceof NodesByCode ||
            s instanceof NodesByUri ||
            s instanceof NodesByCreatedDate ||
            s instanceof NodesByLastModifiedDate;

    Predicate<Specification<NodeId, Node>> graphSpec =
        s -> s instanceof NodesByGraphId &&
            Objects.equals(readableType.getGraphId(), ((NodesByGraphId) s).getGraphId());
    Predicate<Specification<NodeId, Node>> typeSpec =
        s -> s instanceof NodesByTypeId &&
            Objects.equals(readableType.getId(), ((NodesByTypeId) s).getTypeId());
    Predicate<Specification<NodeId, Node>> propSpec =
        s -> s instanceof NodesByProperty &&
            textAttrIds.contains(((NodesByProperty) s).getAttributeId());
    Predicate<Specification<NodeId, Node>> propPrefixSpec =
        s -> s instanceof NodesByPropertyPrefix &&
            textAttrIds.contains(((NodesByPropertyPrefix) s).getAttributeId());
    Predicate<Specification<NodeId, Node>> refSpec =
        s -> s instanceof NodesByReference &&
            refAttrIds.contains(((NodesByReference) s).getAttributeId());
    Predicate<Specification<NodeId, Node>> nullRefSpec =
        s -> s instanceof NodesWithoutReferences &&
            refAttrIds.contains(((NodesWithoutReferences) s).getAttributeId());

    Predicate<Specification<NodeId, Node>> specificationPredicate =
        acceptedSpec
            .or(graphSpec).or(typeSpec)
            .or(propSpec).or(propPrefixSpec)
            .or(refSpec).or(nullRefSpec);

    filtered.and(new SpecificationTreeFilter<>(specificationPredicate).apply(nodeSpecification));

    return filtered;
  }

}
