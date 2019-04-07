package fi.thl.termed.service.node;

import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.event.NodeDeletedEvent;
import fi.thl.termed.domain.event.NodeSavedEvent;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.util.query.Queries;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests that node service fires save and delete events.
 */
class NodeWriteEventEmittingServiceIntegrationTest extends BaseNodeServiceIntegrationTest {

  @Autowired
  private EventBus eventBus;

  @Test
  void shouldProduceEventsForNodeWrites() {
    Node node = Node.builder()
        .id(NodeId.random("Person", graphId))
        .build();

    SimpleNodeWriteEventListener eventListener = new SimpleNodeWriteEventListener();
    eventBus.register(eventListener);

    assertFalse(nodeService.exists(node.identifier(), user));
    assertTrue(eventListener.savedEvents.isEmpty());
    assertTrue(eventListener.deletedEvents.isEmpty());

    nodeService.save(node, INSERT, opts(true), user);
    assertEquals(1, eventListener.savedEvents.size());
    assertEquals(0, eventListener.deletedEvents.size());

    nodeService.delete(node.identifier(), opts(true), user);
    assertEquals(1, eventListener.deletedEvents.size());
    assertEquals(1, eventListener.deletedEvents.size());
  }

  @Test
  void shouldProduceEventsForNodeStreamWrites() {
    int nodeCount = 2200;

    Stream<Node> nodeStream = Stream
        .generate(() -> Node.builder().id(NodeId.random("Person", graphId)).build())
        .limit(nodeCount);

    SimpleNodeWriteEventListener eventListener = new SimpleNodeWriteEventListener();
    eventBus.register(eventListener);

    assertEquals(0, nodeService.count(NodesByGraphId.of(graphId), user));
    assertEquals(0, eventListener.countNodesSaved());
    assertEquals(0, eventListener.countNodesDeleted());

    nodeService.save(nodeStream, INSERT, opts(true), user);
    assertEquals(nodeCount, eventListener.countNodesSaved());
    assertEquals(0, eventListener.countNodesDeleted());

    try (Stream<NodeId> keys = nodeService.keys(Queries.query(NodesByGraphId.of(graphId)), user)) {
      nodeService.delete(keys, opts(true), user);
    }

    assertEquals(nodeCount, eventListener.countNodesSaved());
    assertEquals(nodeCount, eventListener.countNodesDeleted());
  }

  @Test
  void shouldProduceEventsForNodeSaveAndDelete() {
    int nodeCount = 1100;

    Supplier<Stream<Node>> nodeStreamSupplier = () -> Stream
        .generate(() -> Node.builder().id(NodeId.random("Person", graphId)).build())
        .limit(nodeCount);

    Stream<Node> initialNodeList = nodeStreamSupplier.get()
        .limit(nodeCount);

    SimpleNodeWriteEventListener eventListener = new SimpleNodeWriteEventListener();
    eventBus.register(eventListener);

    assertEquals(0, nodeService.count(NodesByGraphId.of(graphId), user));
    assertEquals(0, eventListener.countNodesSaved());
    assertEquals(0, eventListener.countNodesDeleted());

    nodeService.save(initialNodeList, INSERT, opts(true), user);
    assertEquals(nodeCount, eventListener.countNodesSaved());
    assertEquals(0, eventListener.countNodesDeleted());

    try (Stream<NodeId> keys = nodeService.keys(Queries.query(NodesByGraphId.of(graphId)), user)) {
      nodeService.saveAndDelete(
          nodeStreamSupplier.get().limit(nodeCount), keys,
          INSERT, opts(true), user);
    }

    assertEquals(nodeCount * 2, eventListener.countNodesSaved());
    assertEquals(nodeCount, eventListener.countNodesDeleted());
  }

  private class SimpleNodeWriteEventListener {

    private List<NodeSavedEvent> savedEvents = new ArrayList<>();
    private List<NodeDeletedEvent> deletedEvents = new ArrayList<>();

    @Subscribe
    public void onNodeSavedEvent(NodeSavedEvent e) {
      savedEvents.add(e);
    }

    @Subscribe
    public void onNodeDeletedEvent(NodeDeletedEvent e) {
      deletedEvents.add(e);
    }

    public long countNodesSaved() {
      return savedEvents.stream()
          .map(e -> e.getNodes().size())
          .mapToInt(Integer::intValue)
          .sum();
    }

    public long countNodesDeleted() {
      return deletedEvents.stream()
          .map(e -> e.getNodes().size())
          .mapToInt(Integer::intValue)
          .sum();
    }

  }

}
