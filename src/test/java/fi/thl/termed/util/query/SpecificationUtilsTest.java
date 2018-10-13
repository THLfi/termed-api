package fi.thl.termed.util.query;

import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.query.NotSpecification.not;
import static fi.thl.termed.util.query.OrSpecification.or;
import static fi.thl.termed.util.query.SpecificationUtils.simplify;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByProperty;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SpecificationUtilsTest {

  @Test
  void shouldRemoveDuplicates() {
    assertEquals("(A ∧ B)",
        prettyPrint(simplify(
            and(SimpleSpec.of("A"), SimpleSpec.of("A"), SimpleSpec.of("B")))));

    assertEquals("(A ∨ B)",
        prettyPrint(simplify(
            or(SimpleSpec.of("A"), SimpleSpec.of("A"), SimpleSpec.of("B")))));
  }

  @Test
  void shouldFlattenSingletonCompositeSpecs() {
    assertEquals("A",
        prettyPrint(simplify(
            and(and(and(SimpleSpec.of("A")))))));

    assertEquals("A",
        prettyPrint(simplify(
            or(or(or(SimpleSpec.of("A")))))));

    assertEquals("A",
        prettyPrint(simplify(
            and(or(and(SimpleSpec.of("A")))))));
  }

  @Test
  void shouldSimplifyConjunctiveSpecContainingMatchNodeToMatchNone() {
    assertEquals(new MatchNone<>(),
        simplify(
            and(SimpleSpec.of("A"), new MatchNone<>(), SimpleSpec.of("B"))));

    assertEquals(new MatchNone<>(),
        simplify(
            and(new MatchNone<>(), new MatchNone<>())));

    assertEquals(new MatchNone<>(),
        simplify(
            and(new MatchNone<>(), SimpleSpec.of("A"), new MatchNone<>(), SimpleSpec.of("B"))));

    assertEquals(new MatchNone<>(),
        simplify(
            and(SimpleSpec.of("A"),
                and(new MatchNone<>(), SimpleSpec.of("X")),
                SimpleSpec.of("B"))));
  }

  @Test
  void shouldSimplifyDoubleNegation() {
    assertEquals(SimpleSpec.of("A"),
        simplify(not(not(SimpleSpec.of("A")))));

    assertEquals(not(SimpleSpec.of("A")),
        simplify(not(not(not(SimpleSpec.of("A"))))));

    assertEquals(not(SimpleSpec.of("A")),
        simplify(not(SimpleSpec.of("A"))));
  }

  @Test
  void shouldSimplifyNestedNotSpecifications() {
    assertEquals(
        or(not(SimpleSpec.of("B")), SimpleSpec.of("A")),
        simplify(not(and(SimpleSpec.of("B"), not(SimpleSpec.of("A"))))));

    assertEquals(
        and(not(SimpleSpec.of("B")), SimpleSpec.of("A")),
        simplify(not(or(SimpleSpec.of("B"), not(SimpleSpec.of("A"))))));
  }

  @Test
  void shouldSimplifyNestedSpecifications() {
    assertEquals(
        or(SimpleSpec.of("A"), SimpleSpec.of("B"), SimpleSpec.of("C")),
        simplify(or(SimpleSpec.of("A"), or(SimpleSpec.of("B"), SimpleSpec.of("C")))));

    assertEquals(
        or(SimpleSpec.of("A"), SimpleSpec.of("B"), SimpleSpec.of("C"), SimpleSpec.of("D")),
        simplify(or(
            or(SimpleSpec.of("A"), SimpleSpec.of("B")),
            or(SimpleSpec.of("C"), SimpleSpec.of("D")))));

    assertEquals(
        or(SimpleSpec.of("A"), SimpleSpec.of("B"), and(SimpleSpec.of("C"), SimpleSpec.of("D"))),
        simplify(or(
            or(SimpleSpec.of("A"), SimpleSpec.of("B")),
            and(SimpleSpec.of("C"), SimpleSpec.of("D")))));
  }

  @Test
  void shouldSimplifyNestedSpecificationsWithNot() {
    assertEquals(or(
        SimpleSpec.of("A"),
        SimpleSpec.of("B"),
        SimpleSpec.of("C"),
        SimpleSpec.of("D")),
        simplify(or(
            SimpleSpec.of("A"),
            SimpleSpec.of("B"),
            not(and(
                not(SimpleSpec.of("C")),
                not(SimpleSpec.of("D")),
                not(SimpleSpec.of("D")))))));
  }

  @Test
  void shouldNotSimplifyDisjunctiveSpecContainingMatchNoneToMatchNone() {
    assertNotEquals(new MatchNone<>(), simplify(
        or(SimpleSpec.of("A"), new MatchNone<>(), SimpleSpec.of("B"))));
  }

  @Test
  void shouldRemoveRedundantMatchNoneFromDisjunction() {
    assertEquals(or(SimpleSpec.of("A"), SimpleSpec.of("B")),
        simplify(or(SimpleSpec.of("A"), new MatchNone<>(), SimpleSpec.of("B"))));
  }

  @Test
  void shouldConvertToDnf() {
    assertEquals("((A ∧ X) ∨ (A ∧ Y) ∨ (B ∧ X) ∨ (B ∧ Y))",
        prettyPrint(SpecificationUtils.toDnf(
            and(or(SimpleSpec.of("A"), SimpleSpec.of("B")),
                or(SimpleSpec.of("X"), SimpleSpec.of("Y"))))));

    assertEquals("((A ∧ X ∧ I) ∨ (A ∧ X ∧ J) ∨ (A ∧ X ∧ K) ∨ "
            + "(A ∧ Y ∧ I) ∨ (A ∧ Y ∧ J) ∨ (A ∧ Y ∧ K) ∨ "
            + "(A ∧ Z ∧ I) ∨ (A ∧ Z ∧ J) ∨ (A ∧ Z ∧ K) ∨ "
            + "(B ∧ X ∧ I) ∨ (B ∧ X ∧ J) ∨ (B ∧ X ∧ K) ∨ "
            + "(B ∧ Y ∧ I) ∨ (B ∧ Y ∧ J) ∨ (B ∧ Y ∧ K) ∨ "
            + "(B ∧ Z ∧ I) ∨ (B ∧ Z ∧ J) ∨ (B ∧ Z ∧ K) ∨ "
            + "(C ∧ X ∧ I) ∨ (C ∧ X ∧ J) ∨ (C ∧ X ∧ K) ∨ "
            + "(C ∧ Y ∧ I) ∨ (C ∧ Y ∧ J) ∨ (C ∧ Y ∧ K) ∨ "
            + "(C ∧ Z ∧ I) ∨ (C ∧ Z ∧ J) ∨ (C ∧ Z ∧ K))",
        prettyPrint(SpecificationUtils.toDnf(and(
            or(SimpleSpec.of("A"), SimpleSpec.of("B"), SimpleSpec.of("C")),
            or(SimpleSpec.of("X"), SimpleSpec.of("Y"), SimpleSpec.of("Z")),
            or(SimpleSpec.of("I"), SimpleSpec.of("J"), SimpleSpec.of("K"))))));
  }

  @Test
  void shouldConvertToCnf() {
    assertEquals("((A ∨ X) ∧ (A ∨ Y) ∧ (B ∨ X) ∧ (B ∨ Y))",
        prettyPrint(SpecificationUtils.toCnf(
            or(and(SimpleSpec.of("A"), SimpleSpec.of("B")),
                and(SimpleSpec.of("X"), SimpleSpec.of("Y"))))));
  }

  @Test
  void shouldConvertNodeSpecToDnf() {
    UUID graphId = UUID.randomUUID();
    String typeId = "Concept";
    String query = "cat";

    Specification<NodeId, Node> expected = or(
        and(new NodesByGraphId(graphId),
            new NodesByTypeId(typeId),
            new NodesByProperty("prefLabel", query)),
        and(new NodesByGraphId(graphId),
            new NodesByTypeId(typeId),
            new NodesByProperty("altLabel", query)),
        and(new NodesByGraphId(graphId),
            new NodesByTypeId(typeId),
            new NodesByProperty("description", query)));

    assertEquals(expected, SpecificationUtils.toDnf(
        and(new NodesByGraphId(graphId),
            new NodesByTypeId(typeId),
            or(new NodesByProperty("prefLabel", query),
                new NodesByProperty("altLabel", query),
                new NodesByProperty("description", query)))));
  }

  private <K extends Serializable, V> String prettyPrint(Specification<K, V> spec) {
    if (spec instanceof AndSpecification) {
      List<String> clauses = ((AndSpecification<K, V>) spec).getSpecifications().stream()
          .map(this::prettyPrint).collect(toList());
      return "(" + String.join(" ∧ ", clauses) + ")";
    }
    if (spec instanceof OrSpecification) {
      List<String> clauses = ((OrSpecification<K, V>) spec).getSpecifications().stream()
          .map(this::prettyPrint).collect(toList());
      return "(" + String.join(" ∨ ", clauses) + ")";
    }
    if (spec instanceof NotSpecification) {
      return "¬" + prettyPrint(((NotSpecification<K, V>) spec).getSpecification());
    }
    return spec.toString();
  }

  private static class SimpleSpec implements Specification<String, Void> {

    private String symbol;

    SimpleSpec(String symbol) {
      this.symbol = symbol;
    }

    static SimpleSpec of(String symbol) {
      return new SimpleSpec(symbol);
    }

    @Override
    public boolean test(String symbol, Void v) {
      return Objects.equals(this.symbol, symbol);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SimpleSpec that = (SimpleSpec) o;
      return Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
      return Objects.hash(symbol);
    }

    @Override
    public String toString() {
      return symbol;
    }

  }

}