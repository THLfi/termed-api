package fi.thl.termed.service.node.util;

import static com.google.common.collect.ImmutableList.of;
import static fi.thl.termed.util.UUIDs.nilUuid;
import static fi.thl.termed.util.query.AndSpecification.and;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.Charsets;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByPropertyString;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.csv.CsvOptions;
import fi.thl.termed.util.query.SelectAll;
import fi.thl.termed.util.query.Specification;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CsvToNodesTest {

  @Test
  void shouldParseNodesWithProperties() {
    UUID graphId = UUID.randomUUID();
    UUID node1Id = UUID.randomUUID();
    UUID node2Id = UUID.randomUUID();
    String typeId = "Person";

    String headers = String
        .join(",", "id", "type.id", "type.graph.id", "number", "code", "p.name");
    String row1 = String
        .join(",", node1Id.toString(), typeId, graphId.toString(), "1", "example-node-1", "John");
    String row2 = String
        .join(",", node2Id.toString(), typeId, graphId.toString(), "2", "example-node-2", "Mary");

    InputStream csv = new ByteArrayInputStream(
        String.join("\n", headers, row1, row2).getBytes(Charsets.UTF_8));

    List<Node> nodes = new CsvToNodes()
        .parseNodesFromCsv(CsvOptions.builder().build(), csv)
        .collect(toList());

    Node node1 = nodes.get(0);
    assertEquals(node1Id, node1.getId());
    assertEquals(typeId, node1.getTypeId());
    assertEquals(graphId, node1.getTypeGraphId());
    assertEquals(new Long(1), node1.getNumber());
    assertEquals("example-node-1", node1.getCode().orElse(null));
    assertEquals("John", node1.getFirstPropertyValue("name")
        .map(StrictLangValue::getValue)
        .orElse(null));

    Node node2 = nodes.get(1);
    assertEquals(node2Id, node2.getId());
    assertEquals(typeId, node2.getTypeId());
    assertEquals(graphId, node2.getTypeGraphId());
    assertEquals(new Long(2), node2.getNumber());
    assertEquals("example-node-2", node2.getCode().orElse(null));
    assertEquals("Mary", node2.getFirstPropertyValue("name")
        .map(StrictLangValue::getValue)
        .orElse(null));
  }

  @Test
  void shouldParseNodesWithReferences() {
    UUID graphId = UUID.randomUUID();
    UUID node1Id = UUID.randomUUID();
    UUID node2Id = UUID.randomUUID();
    String typeId = "Person";

    String headers = String
        .join(",", "id", "type.id", "type.graph.id", "number", "code", "r.knows.id");
    String row1 = String
        .join(",", node1Id.toString(), typeId, graphId.toString(), "1", "example-node-1",
            node2Id.toString());
    String row2 = String
        .join(",", node2Id.toString(), typeId, graphId.toString(), "2", "example-node-2",
            node1Id.toString());

    InputStream csv = new ByteArrayInputStream(
        String.join("\n", headers, row1, row2).getBytes(Charsets.UTF_8));

    List<Node> nodes = new CsvToNodes()
        .parseNodesFromCsv(CsvOptions.builder().build(), csv)
        .collect(toList());

    Node node1 = nodes.get(0);
    assertEquals(node1Id, node1.getId());
    assertEquals(typeId, node1.getTypeId());
    assertEquals(graphId, node1.getTypeGraphId());
    assertEquals(new Long(1), node1.getNumber());
    assertEquals("example-node-1", node1.getCode().orElse(null));
    assertEquals(node2Id, node1.getFirstReferenceValue("knows")
        .map(NodeId::getId)
        .orElse(null));

    Node node2 = nodes.get(1);
    assertEquals(node2Id, node2.getId());
    assertEquals(typeId, node2.getTypeId());
    assertEquals(graphId, node2.getTypeGraphId());
    assertEquals(new Long(2), node2.getNumber());
    assertEquals("example-node-2", node2.getCode().orElse(null));
    assertEquals(node1Id, node2.getFirstReferenceValue("knows")
        .map(NodeId::getId)
        .orElse(null));
  }

  @Test
  void shouldParseAndResolveNodesWithLabeledReferences() {
    UUID graphId = UUID.randomUUID();
    UUID node1Id = UUID.randomUUID();
    UUID node2Id = UUID.randomUUID();
    String typeId = "Person";

    String headers = String
        .join(",", "id", "type.id", "type.graph.id", "number", "code", "p.name", "r.knows.p.name");
    String row1 = String
        .join(",", node1Id.toString(), typeId, graphId.toString(), "1", "example-node-1",
            "John", "Mary");
    String row2 = String
        .join(",", node2Id.toString(), typeId, graphId.toString(), "2", "example-node-2",
            "Mary", "John");

    InputStream csv = new ByteArrayInputStream(
        String.join("\n", headers, row1, row2).getBytes(Charsets.UTF_8));

    TypeId personTypeId = TypeId.of(typeId, graphId);
    Type personType = Type.builder().id(personTypeId)
        .referenceAttributes(
            ReferenceAttribute.builder()
                .id("knows", personTypeId)
                .range(personTypeId)
                .build())
        .build();

    Function<Specification<NodeId, Node>, Optional<NodeId>> nodeIdResolver = (specification) -> {
      if (specification.equals(and(
          new NodesByGraphId(graphId),
          new NodesByTypeId(typeId),
          new NodesByPropertyString("name", "John")
      ))) {
        return Optional.of(NodeId.of(node1Id, personTypeId));
      }
      if (specification.equals(and(
          new NodesByGraphId(graphId),
          new NodesByTypeId(typeId),
          new NodesByPropertyString("name", "Mary")
      ))) {
        return Optional.of(NodeId.of(node2Id, personTypeId));
      }
      throw new AssertionError("Can't resolve: " + specification);
    };

    List<Node> nodes = new CsvToNodes(of(personType), nodeIdResolver)
        .parseNodesFromCsv(CsvOptions.builder().build(), csv)
        .collect(toList());

    Node node1 = nodes.get(0);
    assertEquals(node1Id, node1.getId());
    assertEquals(typeId, node1.getTypeId());
    assertEquals(graphId, node1.getTypeGraphId());
    assertEquals(new Long(1), node1.getNumber());
    assertEquals("example-node-1", node1.getCode().orElse(null));
    assertEquals(node2Id, node1.getFirstReferenceValue("knows")
        .map(NodeId::getId)
        .orElse(null));

    Node node2 = nodes.get(1);
    assertEquals(node2Id, node2.getId());
    assertEquals(typeId, node2.getTypeId());
    assertEquals(graphId, node2.getTypeGraphId());
    assertEquals(new Long(2), node2.getNumber());
    assertEquals("example-node-2", node2.getCode().orElse(null));
    assertEquals(node1Id, node2.getFirstReferenceValue("knows")
        .map(NodeId::getId)
        .orElse(null));
  }

  private void assertNodesAreEqualAfterConvertingCsvAndBack(List<Node> nodes) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new NodesToCsv()
        .writeAsCsv(nodes.stream(), of(new SelectAll()), CsvOptions.builder().build(), out);

    List<Node> results = new CsvToNodes()
        .parseNodesFromCsv(CsvOptions.builder().build(),
            new ByteArrayInputStream(out.toByteArray()))
        .collect(Collectors.toList());

    assertEquals(nodes, results);
  }

  @Test
  void shouldConvertSimpleNodesToCsvAndBack() {
    GraphId graphId = GraphId.random();
    TypeId typeId = TypeId.of("Person", graphId);

    List<Node> nodes = of(
        Node.builder()
            .random(typeId)
            .number(1L)
            .code("john")
            .addProperty("name", "John")
            .addProperty("email", "john@example.org")
            .build(),
        Node.builder()
            .random(typeId)
            .number(2L)
            .code("jack")
            .addProperty("name", "Jack")
            .addProperty("email", "jack@example.org")
            .build());

    assertNodesAreEqualAfterConvertingCsvAndBack(nodes);
  }

  @Test
  void shouldConvertComplexNodesToCsvAndBack() {
    GraphId graphId = GraphId.random();
    TypeId typeId = TypeId.of("Animal", graphId);
    TypeId unknownTypeId = TypeId.of("", nilUuid());

    List<Node> nodes = of(
        Node.builder()
            .random(typeId)
            .code("cat")
            .uri("http://example.org/cat")
            .number(1L)
            .addProperty("prefLabel", "en", "Cat")
            .addProperty("altLabel", "en", "Kitty")
            .addProperty("altLabel", "en", "Kitten")
            .addProperty("extId", "123")
            .build(),
        Node.builder()
            .random(typeId)
            .number(2L)
            .addProperty("prefLabel", "en", "Dog")
            .addProperty("altLabel", "en", "Doggy")
            .addProperty("altLabel", "en", "Hound")
            .addProperty("altLabel", "en", "Pup")
            .addProperty("altLabel", "fi", "Hau|,veli")
            .addProperty("altLabel", "fi", "Hauva,")
            .addReference("related", NodeId.random(unknownTypeId))
            .addReference("related", NodeId.random(unknownTypeId))
            .build());

    assertNodesAreEqualAfterConvertingCsvAndBack(nodes);
  }

}
