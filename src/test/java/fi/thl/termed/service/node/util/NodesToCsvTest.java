package fi.thl.termed.service.node.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.service.node.select.SelectProperty;
import fi.thl.termed.util.csv.CsvOptions;
import fi.thl.termed.util.query.Selects;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class NodesToCsvTest {

  @Test
  void shouldWriteNodesToCsv() throws UnsupportedEncodingException {
    UUID graphId = UUID.randomUUID();
    TypeId typeId = TypeId.of("Person", graphId);

    Node node1 = Node.builder().random(typeId)
        .code("example-node-1")
        .number(1L)
        .addProperty("firstName", "John")
        .addProperty("email", "john@example.com")
        .addProperty("email", "john@example.org")
        .build();

    NodesToCsv nodesToCsv = new NodesToCsv();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    nodesToCsv.writeAsCsv(Stream.of(node1),
        ImmutableList.of(
            Selects.field("number"),
            Selects.field("code"),
            new SelectProperty("firstName"),
            new SelectProperty("email")),
        CsvOptions.builder().build(),
        byteArrayOutputStream);

    String csv = byteArrayOutputStream.toString("UTF-8");

    String expectedCsv =
        "code,number,properties.firstName,properties.email\n"
            + "example-node-1,1,John,john@example.com|john@example.org\n";

    assertEquals(expectedCsv, csv);
  }

}