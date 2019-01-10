package fi.thl.termed.service.node;

import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Tests that node service generates node codes correctly.
 */
class NodeCodeSavingServiceIntegrationTest extends BaseNodeServiceIntegrationTest {

  @Test
  void shouldNotAllowDuplicateCodes() {
    NodeId nodeId0 = NodeId.random("Person", graphId);
    Node node0 = Node.builder()
        .id(nodeId0)
        .code("example-code")
        .build();

    NodeId nodeId1 = NodeId.random("Person", graphId);
    Node node1 = Node.builder()
        .id(nodeId1)
        .code("example-code")
        .build();

    assertFalse(nodeService.exists(nodeId0, user));
    assertFalse(nodeService.exists(nodeId1, user));

    assertThrows(DataIntegrityViolationException.class,
        () -> nodeService.save(Stream.of(node0, node1), INSERT, defaultOpts(), user));

    assertFalse(nodeService.exists(nodeId0, user));
    assertFalse(nodeService.exists(nodeId1, user));
  }

  @Test
  void shouldNullifyDuplicateCodeIfOptsGenerateCodesIsTrue() {
    NodeId nodeId0 = NodeId.random("Person", graphId);
    Node node0 = Node.builder()
        .id(nodeId0)
        .code("example-code")
        .build();

    NodeId nodeId1 = NodeId.random("Person", graphId);
    Node node1 = Node.builder()
        .id(nodeId1)
        .code("example-code")
        .build();

    assertFalse(nodeService.exists(nodeId0, user));
    assertFalse(nodeService.exists(nodeId1, user));

    nodeService.save(Stream.of(node0, node1), INSERT, opts(false, true, true), user);

    assertEquals("example-code",
        nodeService.get(nodeId0, user)
            .orElseThrow(AssertionError::new)
            .getCode()
            .orElseThrow(AssertionError::new));

    assertFalse(
        nodeService.get(nodeId1, user)
            .orElseThrow(AssertionError::new)
            .getCode()
            .isPresent());
  }

  @Test
  void shouldNotGenerateDefaultCodeIfIsAlreadyInUse() {
    NodeId nodeId0 = NodeId.random("Person", graphId);
    Node node0 = Node.builder()
        .id(nodeId0)
        .build();

    NodeId nodeId1 = NodeId.random("Person", graphId);
    Node node1 = Node.builder()
        .id(nodeId1)
        // give code that would be default for the next node
        .code("person-2")
        .build();

    NodeId nodeId2 = NodeId.random("Person", graphId);
    Node node2 = Node.builder()
        .id(nodeId2)
        .build();

    nodeService.save(node0, INSERT, opts(false, true, true), user);
    nodeService.save(node1, INSERT, opts(false, true, true), user);
    nodeService.save(node2, INSERT, opts(false, true, true), user);

    assertEquals("person-0", nodeService.get(nodeId0, user)
        .flatMap(Node::getCode).orElse(null));
    assertEquals("person-2", nodeService.get(nodeId1, user)
        .flatMap(Node::getCode).orElse(null));
    assertNull(nodeService.get(nodeId2, user)
        .flatMap(Node::getCode).orElse(null));
  }

}