package fi.thl.termed.util.query;

import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.query.OrSpecification.or;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByProperty;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.Test;

public class SpecificationUtilsTest {

  @Test
  public void shouldRemoveDuplicates() {
    assertEquals("(A ∧ B)",
        prettyPrint(SpecificationUtils.simplify(
            and(SimpleSpec.of("A"), SimpleSpec.of("A"), SimpleSpec.of("B")))));

    assertEquals("(A ∨ B)",
        prettyPrint(SpecificationUtils.simplify(
            or(SimpleSpec.of("A"), SimpleSpec.of("A"), SimpleSpec.of("B")))));
  }

  @Test
  public void shouldFlattenSingletonCompositeSpecs() {
    assertEquals("A",
        prettyPrint(SpecificationUtils.simplify(
            and(and(and(SimpleSpec.of("A")))))));

    assertEquals("A",
        prettyPrint(SpecificationUtils.simplify(
            or(or(or(SimpleSpec.of("A")))))));

    assertEquals("A",
        prettyPrint(SpecificationUtils.simplify(
            and(or(and(SimpleSpec.of("A")))))));
  }

  @Test
  public void shouldSimplifyConjunctiveSpecContainingMatchNodeToMatchNone() {
    assertEquals(new MatchNone<>(),
        SpecificationUtils.simplify(
            and(SimpleSpec.of("A"), new MatchNone<>(), SimpleSpec.of("B"))));

    assertEquals(new MatchNone<>(),
        SpecificationUtils.simplify(
            and(new MatchNone<>(), new MatchNone<>())));

    assertEquals(new MatchNone<>(),
        SpecificationUtils.simplify(
            and(new MatchNone<>(), SimpleSpec.of("A"), new MatchNone<>(), SimpleSpec.of("B"))));

    assertEquals(new MatchNone<>(),
        SpecificationUtils.simplify(
            and(SimpleSpec.of("A"),
                and(new MatchNone<>(), SimpleSpec.of("X")),
                SimpleSpec.of("B"))));
  }

  @Test
  public void shouldNotSimplifyDisjunctiveSpecContainingMatchNoneToMatchNone() {
    assertNotEquals(new MatchNone<>(), SpecificationUtils.simplify(
        or(SimpleSpec.of("A"), new MatchNone<>(), SimpleSpec.of("B"))));
  }

  @Test
  public void shouldRemoveRedundantMatchNoneFromDisjunction() {
    assertEquals(or(SimpleSpec.of("A"), SimpleSpec.of("B")),
        SpecificationUtils.simplify(or(SimpleSpec.of("A"), new MatchNone<>(), SimpleSpec.of("B"))));
  }

  @Test
  public void shouldConvertToDnf() {
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
  public void shouldConvertToCnf() {
    assertEquals("((A ∨ X) ∧ (A ∨ Y) ∧ (B ∨ X) ∧ (B ∨ Y))",
        prettyPrint(SpecificationUtils.toCnf(
            or(and(SimpleSpec.of("A"), SimpleSpec.of("B")),
                and(SimpleSpec.of("X"), SimpleSpec.of("Y"))))));
  }

  @Test
  public void shouldConvertNodeSpecToDnf() {
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