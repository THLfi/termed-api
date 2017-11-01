package fi.thl.termed.service.node.specification;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.util.query.AndSpecification;
import fi.thl.termed.util.query.NotSpecification;
import fi.thl.termed.util.query.OrSpecification;
import fi.thl.termed.util.query.Specification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Resolve specifications on 1) graph uri or code, 2) type uri
 */
public class NodeGraphAndTypeSpecificationResolver implements
    Function<Specification<NodeId, Node>, Specification<NodeId, Node>> {

  private Map<String, List<Graph>> graphsByUri;
  private Map<String, List<Graph>> graphsByCode;
  private Map<String, List<Type>> typesByUri;

  /**
   * @param types list of readable types
   */
  public NodeGraphAndTypeSpecificationResolver(List<Graph> graphs, List<Type> types) {
    this.graphsByUri = graphs.stream()
        .filter(g -> g.getUri().isPresent())
        .collect(groupingBy(g -> g.getUri().orElse(null)));
    this.graphsByCode = graphs.stream()
        .filter(g -> g.getCode().isPresent())
        .collect(groupingBy(g -> g.getCode().orElse(null)));
    this.typesByUri = types.stream()
        .filter(t -> t.getUri().isPresent())
        .collect(groupingBy(t -> t.getUri().orElse(null)));
  }

  @Override
  public Specification<NodeId, Node> apply(Specification<NodeId, Node> specification) {

    if (specification instanceof NodesByGraphUri) {
      String graphUri = ((NodesByGraphUri) specification).getGraphUri();
      List<Specification<NodeId, Node>> graphsById = new ArrayList<>();
      graphsByUri.getOrDefault(graphUri, emptyList())
          .forEach(g -> graphsById.add(new NodesByGraphId(g.getId())));
      return OrSpecification.or(graphsById);
    }
    if (specification instanceof NodesByGraphCode) {
      String graphCode = ((NodesByGraphCode) specification).getGraphCode();
      List<Specification<NodeId, Node>> graphsById = new ArrayList<>();
      graphsByCode.getOrDefault(graphCode, emptyList())
          .forEach(g -> graphsById.add(new NodesByGraphId(g.getId())));
      return OrSpecification.or(graphsById);
    }
    if (specification instanceof NodesByTypeUri) {
      String typeUri = ((NodesByTypeUri) specification).getTypeUri();
      List<Specification<NodeId, Node>> typesById = new ArrayList<>();
      typesByUri.getOrDefault(typeUri, emptyList())
          .forEach(g -> typesById.add(new NodesByTypeId(g.getId())));
      return OrSpecification.or(typesById);
    }

    if (specification instanceof AndSpecification) {
      return resolveAndSpecification((AndSpecification<NodeId, Node>) specification);
    }
    if (specification instanceof OrSpecification) {
      return resolveOrSpecification((OrSpecification<NodeId, Node>) specification);
    }
    if (specification instanceof NotSpecification) {
      return resolveNotSpecification((NotSpecification<NodeId, Node>) specification);
    }

    return specification;
  }

  private AndSpecification<NodeId, Node> resolveAndSpecification(
      AndSpecification<NodeId, Node> specs) {
    List<Specification<NodeId, Node>> resolved = new ArrayList<>();
    specs.forEach(spec -> resolved.add(apply(spec)));
    return AndSpecification.and(resolved);
  }

  private OrSpecification<NodeId, Node> resolveOrSpecification(
      OrSpecification<NodeId, Node> specs) {
    List<Specification<NodeId, Node>> resolved = new ArrayList<>();
    specs.forEach(spec -> resolved.add(apply(spec)));
    return OrSpecification.or(resolved);
  }

  private Specification<NodeId, Node> resolveNotSpecification(NotSpecification<NodeId, Node> s) {
    return NotSpecification.not(apply(s.getSpecification()));
  }

}
