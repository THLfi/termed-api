package fi.thl.termed.service.node;

import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.util.query.Query;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Tests that node service generates node numbers correctly.
 */
class NodeNumberSavingServiceIntegrationTest extends BaseNodeServiceIntegrationTest {

  @Test
  void shouldGenerateNodeNumbers() {
    NodeId node0Id = NodeId.random("Person", graphId);
    Node node0 = Node.builder().id(node0Id).build();

    NodeId node1Id = NodeId.random("Person", graphId);
    Node node1 = Node.builder().id(node1Id).build();

    nodeService.save(node0, INSERT, defaultOpts(), user);
    nodeService.save(node1, INSERT, defaultOpts(), user);

    assertEquals(0, (long) nodeService.get(node0Id, user)
        .map(Node::getNumber)
        .orElseThrow(AssertionError::new));

    assertEquals(1, (long) nodeService.get(node1Id, user)
        .map(Node::getNumber)
        .orElseThrow(AssertionError::new));
  }

  @Test
  void shouldOnlyUseGeneratedNumbers() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node node = Node.builder()
        .id(nodeId)
        .number(23L)
        .build();

    nodeService.save(node, INSERT, defaultOpts(), user);

    assertEquals(0, (long) nodeService.get(nodeId, user)
        .map(Node::getNumber)
        .orElseThrow(AssertionError::new));

    // try again with update
    nodeService.save(node, UPDATE, defaultOpts(), user);

    assertEquals(0, (long) nodeService.get(nodeId, user)
        .map(Node::getNumber)
        .orElseThrow(AssertionError::new));
  }

  @Test
  void shouldInsertManyNodesWithCorrectNumbers() {
    int testNodeCount = 2500;

    assertEquals(0, nodeService.count(new NodesByGraphId(graphId), user));

    nodeService.save(
        Stream.generate(() -> Node.builder().id(NodeId.random("Person", graphId)).build())
            .limit(testNodeCount),
        INSERT, defaultOpts(), user);

    assertEquals(testNodeCount, nodeService.count(new NodesByGraphId(graphId), user));

    try (Stream<Node> nodes = nodeService.values(
        new Query<>(new NodesByGraphId(graphId)), user)) {
      assertEquals(
          LongStream.range(0, testNodeCount).boxed().collect(toSet()),
          nodes.map(Node::getNumber).collect(toSet()));
    }
  }

}