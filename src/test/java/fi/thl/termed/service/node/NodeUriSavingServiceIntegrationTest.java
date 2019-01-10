package fi.thl.termed.service.node;

import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import org.junit.jupiter.api.Test;

/**
 * Tests that node service generates node URIs correctly.
 */
class NodeUriSavingServiceIntegrationTest extends BaseNodeServiceIntegrationTest {

  @Test
  void shouldGenerateUriUsingGivenNamespace() {
    NodeId nodeId0 = NodeId.random("Person", graphId);
    Node node0 = Node.builder()
        .id(nodeId0)
        .code("example-code-0")
        .build();

    assertFalse(nodeService.exists(nodeId0, user));

    nodeService.save(node0, INSERT,
        opts(false, "http://example.org/", true, true), user);

    assertEquals("http://example.org/example-code-0",
        nodeService.get(nodeId0, user)
            .orElseThrow(AssertionError::new)
            .getUri()
            .orElseThrow(AssertionError::new));
  }

}
