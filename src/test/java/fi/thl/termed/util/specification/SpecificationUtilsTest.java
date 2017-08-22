package fi.thl.termed.util.specification;

import static java.util.stream.Collectors.toList;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByProperty;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

public class SpecificationUtilsTest {

  @Test
  public void shouldConvertToDnf() {
    Assert.assertEquals("((A ∧ X) ∨ (A ∧ Y) ∨ (B ∧ X) ∨ (B ∧ Y))",
        prettyPrint(SpecificationUtils.toDnf(
            new AndSpecification<>(
                new OrSpecification<>(new SimpleSpec("A"), new SimpleSpec("B")),
                new OrSpecification<>(new SimpleSpec("X"), new SimpleSpec("Y"))))));

    Assert.assertEquals("((A ∧ X ∧ I) ∨ (A ∧ X ∧ J) ∨ (A ∧ X ∧ K) ∨ "
            + "(A ∧ Y ∧ I) ∨ (A ∧ Y ∧ J) ∨ (A ∧ Y ∧ K) ∨ "
            + "(A ∧ Z ∧ I) ∨ (A ∧ Z ∧ J) ∨ (A ∧ Z ∧ K) ∨ "
            + "(B ∧ X ∧ I) ∨ (B ∧ X ∧ J) ∨ (B ∧ X ∧ K) ∨ "
            + "(B ∧ Y ∧ I) ∨ (B ∧ Y ∧ J) ∨ (B ∧ Y ∧ K) ∨ "
            + "(B ∧ Z ∧ I) ∨ (B ∧ Z ∧ J) ∨ (B ∧ Z ∧ K) ∨ "
            + "(C ∧ X ∧ I) ∨ (C ∧ X ∧ J) ∨ (C ∧ X ∧ K) ∨ "
            + "(C ∧ Y ∧ I) ∨ (C ∧ Y ∧ J) ∨ (C ∧ Y ∧ K) ∨ "
            + "(C ∧ Z ∧ I) ∨ (C ∧ Z ∧ J) ∨ (C ∧ Z ∧ K))",
        prettyPrint(SpecificationUtils.toDnf(new AndSpecification<>(
            new OrSpecification<>(new SimpleSpec("A"), new SimpleSpec("B"), new SimpleSpec("C")),
            new OrSpecification<>(new SimpleSpec("X"), new SimpleSpec("Y"), new SimpleSpec("Z")),
            new OrSpecification<>(new SimpleSpec("I"), new SimpleSpec("J"),
                new SimpleSpec("K"))))));
  }

  @Test
  public void shouldConvertNodeSpecToDnf() {
    UUID graphId = UUID.randomUUID();
    String typeId = "Concept";
    String query = "cat";

    Specification<NodeId, Node> expected = new OrSpecification<>(
        new AndSpecification<>(
            new NodesByGraphId(graphId),
            new NodesByTypeId(typeId),
            new NodesByProperty("prefLabel", query)),
        new AndSpecification<>(
            new NodesByGraphId(graphId),
            new NodesByTypeId(typeId),
            new NodesByProperty("altLabel", query)),
        new AndSpecification<>(
            new NodesByGraphId(graphId),
            new NodesByTypeId(typeId),
            new NodesByProperty("description", query)));

    Assert.assertEquals(expected, SpecificationUtils.toDnf(
        new AndSpecification<>(
            new NodesByGraphId(graphId),
            new NodesByTypeId(typeId),
            new OrSpecification<>(
                new NodesByProperty("prefLabel", query),
                new NodesByProperty("altLabel", query),
                new NodesByProperty("description", query)))));
  }

  @Test
  public void shouldConvertToCnf() {
    Assert.assertEquals("((A ∨ X) ∧ (A ∨ Y) ∧ (B ∨ X) ∧ (B ∨ Y))",
        prettyPrint(SpecificationUtils.toCnf(
            new OrSpecification<>(
                new AndSpecification<>(new SimpleSpec("A"), new SimpleSpec("B")),
                new AndSpecification<>(new SimpleSpec("X"), new SimpleSpec("Y"))))));
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

    public SimpleSpec(String symbol) {
      this.symbol = symbol;
    }

    @Override
    public boolean test(String symbol, Void v) {
      return Objects.equals(this.symbol, symbol);
    }

    @Override
    public String toString() {
      return symbol;
    }
  }

}