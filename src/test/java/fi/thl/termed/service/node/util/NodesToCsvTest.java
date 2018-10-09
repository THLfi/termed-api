package fi.thl.termed.service.node.util;

import static fi.thl.termed.util.UUIDs.nilUuid;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.csv.CsvOptions;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class NodesToCsvTest {

  private GraphId graphId = GraphId.random();
  private TypeId typeId = TypeId.of("Type", graphId);
  private TypeId unknownTypeId = TypeId.of("", nilUuid());

  private CsvOptions defaultOpts = CsvOptions.builder().build();
  private Set<Select> selectAll = ImmutableSet.of(new SelectAll());

  private void assertNodesAreEqualAfterConvertingCsvAndBack(List<Node> nodes) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    NodesToCsv.writeAsCsv(nodes.stream(), selectAll, defaultOpts, out);

    List<Node> results = NodesToCsv
        .readAsCsv(defaultOpts, new ByteArrayInputStream(out.toByteArray()))
        .collect(Collectors.toList());

    assertEquals(nodes, results);
  }

  @Test
  public void shouldConvertSimpleNodesToCsvAndBack() {
    List<Node> nodes = ImmutableList.of(
        Node.builder()
            .random(typeId)
            .number(1L)
            .addProperty("name", "John")
            .addProperty("email", "john@example.org")
            .build(),
        Node.builder()
            .random(typeId)
            .number(2L)
            .addProperty("name", "Jack")
            .addProperty("email", "jack@example.org")
            .build());

    assertNodesAreEqualAfterConvertingCsvAndBack(nodes);
  }

  @Test
  public void shouldConvertComplexNodesToCsvAndBack() {
    List<Node> nodes = ImmutableList.of(
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