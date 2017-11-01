package fi.thl.termed.service.node.specification;

import static java.util.stream.Collectors.toMap;

import fi.thl.termed.domain.Attribute;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.AndSpecification;
import fi.thl.termed.util.query.MatchNone;
import fi.thl.termed.util.query.NotSpecification;
import fi.thl.termed.util.query.OrSpecification;
import fi.thl.termed.util.query.Specification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Filters specification so that only attributes present in given domain type are searched. Useful
 * for filtering user submitted queries.
 */
public class TypeBasedNodeSpecificationFilter implements
    BiFunction<Type, Specification<NodeId, Node>, Specification<NodeId, Node>> {

  private Map<TypeId, Type> readPermittedTypes;

  /**
   * @param types list of readable types
   */
  public TypeBasedNodeSpecificationFilter(List<Type> types) {
    this.readPermittedTypes = types.stream().collect(toMap(Type::identifier, t -> t));
  }

  @Override
  public Specification<NodeId, Node> apply(Type domain, Specification<NodeId, Node> specification) {
    if (!readPermittedTypes.containsKey(domain.identifier())) {
      return new MatchNone<>();
    }

    Set<String> textAttrIds = domain.getTextAttributes().stream()
        .map(TextAttribute::getId).collect(Collectors.toSet());
    Map<String, ReferenceAttribute> refAttrs = domain.getReferenceAttributes().stream()
        .collect(toMap(Attribute::getId, r -> r));

    if (isAcceptedIdentifierSpecification(specification) ||
        isAcceptedAuditSpecification(specification) ||
        isAcceptedTextAttributeSpecification(specification, textAttrIds) ||
        isAcceptedReferenceAttributeSpecification(specification, refAttrs)) {
      return specification;
    }
    if (specification instanceof NodesByGraphId &&
        Objects.equals(domain.getGraphId(), ((NodesByGraphId) specification).getGraphId())) {
      return specification;
    }
    if (specification instanceof NodesByTypeId &&
        Objects.equals(domain.getId(), ((NodesByTypeId) specification).getTypeId())) {
      return specification;
    }
    if (specification instanceof NodesByReferencePath &&
        refAttrs.containsKey(((NodesByReferencePath) specification).getAttributeId())) {
      NodesByReferencePath nodesByRefSpec = (NodesByReferencePath) specification;
      ReferenceAttribute refAttr = refAttrs.get(nodesByRefSpec.getAttributeId());
      // apply filter recursively for nested "sub" specification
      return new NodesByReferencePath(nodesByRefSpec.getAttributeId(), apply(
          readPermittedTypes.get(refAttr.getRange()), nodesByRefSpec.getValueSpecification()));
    }
    if (specification instanceof AndSpecification) {
      return filterAndSpecification(domain, (AndSpecification<NodeId, Node>) specification);
    }
    if (specification instanceof OrSpecification) {
      return filterOrSpecification(domain, (OrSpecification<NodeId, Node>) specification);
    }
    if (specification instanceof NotSpecification) {
      return filterNotSpecification(domain, (NotSpecification<NodeId, Node>) specification);
    }

    return new MatchNone<>();
  }

  private boolean isAcceptedIdentifierSpecification(Specification<NodeId, Node> s) {
    return s instanceof NodeById || s instanceof NodesByCode || s instanceof NodesByUri
        || s instanceof NodesByNumber;
  }

  private boolean isAcceptedAuditSpecification(Specification<NodeId, Node> s) {
    return s instanceof NodesByCreatedDate || s instanceof NodesByLastModifiedDate;
  }

  private boolean isAcceptedTextAttributeSpecification(Specification<NodeId, Node> s,
      Set<String> acceptedTextAttrs) {
    return (s instanceof NodesByProperty && acceptedTextAttrs
        .contains(((NodesByProperty) s).getAttributeId()))
        || (s instanceof NodesByPropertyPrefix && acceptedTextAttrs
        .contains(((NodesByPropertyPrefix) s).getAttributeId()))
        || (s instanceof NodesByPropertyPhrase && acceptedTextAttrs
        .contains(((NodesByPropertyPhrase) s).getAttributeId()));
  }

  private boolean isAcceptedReferenceAttributeSpecification(Specification<NodeId, Node> s,
      Map<String, ReferenceAttribute> acceptedReferenceAttrs) {
    String attributeId = null;
    if (s instanceof NodesByReference) {
      attributeId = ((NodesByReference) s).getAttributeId();
    }
    if (s instanceof NodesWithoutReferences) {
      attributeId = ((NodesWithoutReferences) s).getAttributeId();
    }
    return attributeId != null &&
        acceptedReferenceAttrs.containsKey(attributeId) &&
        readPermittedTypes.containsKey(acceptedReferenceAttrs.get(attributeId).getRange());
  }

  private AndSpecification<NodeId, Node> filterAndSpecification(Type domain,
      AndSpecification<NodeId, Node> specs) {
    List<Specification<NodeId, Node>> clauses = new ArrayList<>();
    specs.forEach(spec -> clauses.add(apply(domain, spec)));
    return AndSpecification.and(clauses);
  }

  private OrSpecification<NodeId, Node> filterOrSpecification(Type domain,
      OrSpecification<NodeId, Node> specs) {
    List<Specification<NodeId, Node>> clauses = new ArrayList<>();
    specs.forEach(spec -> clauses.add(apply(domain, spec)));
    return OrSpecification.or(clauses);
  }

  private Specification<NodeId, Node> filterNotSpecification(Type domain,
      NotSpecification<NodeId, Node> s) {
    Specification<NodeId, Node> filteredInnerSpec = apply(domain, s.getSpecification());
    return filteredInnerSpec instanceof MatchNone ? new MatchNone<>()
        : NotSpecification.not(filteredInnerSpec);
  }

}
