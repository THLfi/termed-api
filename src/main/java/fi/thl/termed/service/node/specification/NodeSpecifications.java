package fi.thl.termed.service.node.specification;

import static fi.thl.termed.util.collect.StreamUtils.zip;
import static fi.thl.termed.util.query.BoostSpecification.boost;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.util.query.AndSpecification;
import fi.thl.termed.util.query.OrSpecification;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.query.SpecificationUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Factory methods for node specifications
 */
public final class NodeSpecifications {

  private static NodeSpecificationParser queryParser = new NodeSpecificationParser();

  private NodeSpecifications() {
  }

  public static Specification<NodeId, Node> specifyByQuery(
      List<Graph> graphs, List<Type> types, List<Type> anyDomain, List<String> allQueries) {
    return specifyByQuery(graphs, types, anyDomain, join(" AND ", allQueries));
  }

  public static Specification<NodeId, Node> specifyByQuery(
      List<Graph> graphs, List<Type> types, List<Type> anyDomain, String query) {
    return SpecificationUtils.simplify(OrSpecification.or(
        anyDomain.stream().map(d -> specifyByQuery(graphs, types, d, query)).collect(toList())));
  }

  public static Specification<NodeId, Node> specifyByQuery(
      List<Graph> graphs, List<Type> types, Type domain, List<String> allQueries) {
    return specifyByQuery(graphs, types, domain, join(" AND ", allQueries));
  }

  public static Specification<NodeId, Node> specifyByQuery(
      List<Graph> graphs, List<Type> types, Type domain, String query) {

    List<Specification<NodeId, Node>> clauses = new ArrayList<>();

    clauses.add(new NodesByGraphId(domain.getGraphId()));
    clauses.add(new NodesByTypeId(domain.getId()));

    if (!query.isEmpty()) {
      Specification<NodeId, Node> rawSpec = queryParser.apply(query);

      Specification<NodeId, Node> resolvedSpec =
          new NodeGraphAndTypeSpecificationResolver(graphs, types)
              .apply(rawSpec);

      Specification<NodeId, Node> resolvedFilteredSpec =
          new TypeBasedNodeSpecificationFilter(types)
              .apply(domain, resolvedSpec);

      clauses.add(resolvedFilteredSpec);
    }

    return SpecificationUtils.simplify(AndSpecification.and(clauses));
  }

  public static Specification<NodeId, Node> specifyByAnyPropertyPrefix(Type type, String query) {
    List<Specification<NodeId, Node>> clauses = new ArrayList<>();

    clauses.add(new NodesByGraphId(type.getGraphId()));
    clauses.add(new NodesByTypeId(type.getId()));

    if (!query.isEmpty()) {
      List<String> prefixes = Arrays.asList(query.split("\\s"));

      // boosts for first few text attributes in the query
      Stream<Integer> boosts = IntStream.iterate(8, b -> b > 2 ? b / 2 : 1).boxed();

      List<Specification<NodeId, Node>> orClauses = zip(type.getTextAttributes().stream(), boosts)
          .flatMap(attrAndBoost -> prefixes.stream()
              .map(prefix -> boost(new NodesByPropertyPrefix(attrAndBoost.getKey().getId(), prefix),
                  attrAndBoost.getValue())))
          .collect(toList());

      clauses.add(OrSpecification.or(orClauses));
    }

    return SpecificationUtils.simplify(AndSpecification.and(clauses));
  }

}
