package fi.thl.termed.service.node.specification;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Specification;
import java.util.Arrays;
import java.util.List;

/**
 * Factory methods for node specifications
 */
public final class NodeSpecifications {

  private static NodeSpecificationParser queryParser = new NodeSpecificationParser();

  private NodeSpecifications() {
  }

  public static Specification<NodeId, Node> specifyByQuery(Type type, String query) {
    AndSpecification<NodeId, Node> spec = new AndSpecification<>();

    spec.and(new NodesByGraphId(type.getGraphId()));
    spec.and(new NodesByTypeId(type.getId()));

    if (!query.isEmpty()) {
      spec.and(new TypeBasedNodeSpecificationFilter(type).apply(queryParser.apply(query)));
    }

    return spec;
  }

  public static Specification<NodeId, Node> specifyByAnyPropertyPrefix(Type type, String query) {
    AndSpecification<NodeId, Node> spec = new AndSpecification<>();

    spec.and(new NodesByGraphId(type.getGraphId()));
    spec.and(new NodesByTypeId(type.getId()));

    List<String> prefixes = Arrays.asList(query.split("\\s"));

    if (!prefixes.isEmpty()) {
      Specification<NodeId, Node> prefixSpec = type.getTextAttributes().stream()
          .flatMap(textAttribute -> prefixes.stream()
              .map(prefix -> new NodesByPropertyPrefix(textAttribute.getId(), prefix)))
          .collect(OrSpecification::new, OrSpecification::or, OrSpecification::or);

      spec.and(prefixSpec);
    }

    return spec;
  }

}
