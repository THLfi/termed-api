package fi.thl.termed.service.node;

import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;
import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMultimap;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.service.node.specification.NodeRevisionsByGraphId;
import fi.thl.termed.service.node.specification.NodeRevisionsById;
import fi.thl.termed.service.node.specification.NodeRevisionsByTypeId;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.Service;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests that node service generates revisions correctly. Tests also node revision service for
 * reading revisions.
 */
class NodeRevisionSavingServiceIntegrationTest extends BaseNodeServiceIntegrationTest {

  @Autowired
  private Service<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionService;

  @Test
  void shouldSaveNewRevisionForEachNodeSave() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node node = Node.builder().id(nodeId)
        .addProperty("name", "John")
        .addProperty("email", "john@example.org")
        .build();

    assertFalse(nodeService.exists(nodeId, user));

    int numberOfRevisions = 100;

    for (int i = 0; i < numberOfRevisions; i++) {
      nodeService.save(node, UPSERT, defaultOpts(), user);
    }

    assertEquals(numberOfRevisions,
        nodeRevisionService.count(and(
            NodeRevisionsById.of(nodeId.getId()),
            NodeRevisionsByTypeId.of(nodeId.getTypeId()),
            NodeRevisionsByGraphId.of(nodeId.getTypeGraphId())), user));
  }

  @Test
  void shouldSaveRevisionsForNodesWithProperties() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node node = Node.builder().id(nodeId)
        .addProperty("name", "John")
        .addProperty("email", "john@example.org")
        .build();

    assertFalse(nodeService.exists(nodeId, user));

    nodeService.save(node, INSERT, defaultOpts(), user);
    assertTrue(nodeService.exists(nodeId, user));

    Node personUpdated = Node.builderFromCopyOf(node)
        .properties(ImmutableMultimap.of(
            "name", new StrictLangValue("John Doe"),
            "email", new StrictLangValue("john@example.org")))
        .build();
    nodeService.save(personUpdated, UPDATE, defaultOpts(), user);

    nodeService.delete(nodeId, defaultOpts(), user);

    List<Tuple2<RevisionType, Node>> revisions = toListAndClose(
        nodeRevisionService.values(new Query<>(
            and(
                NodeRevisionsById.of(nodeId.getId()),
                NodeRevisionsByTypeId.of(nodeId.getTypeId()),
                NodeRevisionsByGraphId.of(nodeId.getTypeGraphId()))), user));
    assertEquals(3, revisions.size());

    assertEquals(nodeId, revisions.get(0)._2.identifier());
    assertEquals(RevisionType.DELETE, revisions.get(0)._1);

    assertEquals(nodeId, revisions.get(1)._2.identifier());
    assertEquals(RevisionType.UPDATE, revisions.get(1)._1);
    assertEquals("John Doe", revisions.get(1)._2.getFirstPropertyValue("name")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));
    assertEquals("john@example.org", revisions.get(1)._2.getFirstPropertyValue("email")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));

    assertEquals(nodeId, revisions.get(2)._2.identifier());
    assertEquals(RevisionType.INSERT, revisions.get(2)._1);
    assertEquals("John", revisions.get(2)._2.getFirstPropertyValue("name")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));
    assertEquals("john@example.org", revisions.get(2)._2.getFirstPropertyValue("email")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));
  }

  @Test
  void shouldSaveRevisionsForNodesWithReferences() {
    NodeId johnId = NodeId.random("Person", graphId);
    NodeId jackId = NodeId.random("Person", graphId);
    NodeId maryId = NodeId.random("Person", graphId);

    Node john = Node.builder().id(johnId)
        .addProperty("name", "John")
        .build();

    Node jack = Node.builder().id(jackId)
        .addProperty("name", "Jack")
        .addReference("knows", maryId)
        .build();

    Node mary = Node.builder().id(maryId)
        .addProperty("name", "Mary")
        .build();

    nodeService.save(Stream.of(john, jack, mary), INSERT, defaultOpts(), user);

    Node jackUpdated = Node.builderFromCopyOf(jack)
        .references(ImmutableMultimap.of())
        .build();
    nodeService.save(jackUpdated, UPDATE, defaultOpts(), user);

    Node jackUpdatedAgain = Node.builderFromCopyOf(jack)
        .references(ImmutableMultimap.of("knows", johnId))
        .build();
    nodeService.save(jackUpdatedAgain, UPDATE, defaultOpts(), user);

    List<Tuple2<RevisionType, Node>> jackRevisions = toListAndClose(
        nodeRevisionService.values(new Query<>(
            and(
                NodeRevisionsById.of(jackId.getId()),
                NodeRevisionsByTypeId.of(jackId.getTypeId()),
                NodeRevisionsByGraphId.of(jackId.getTypeGraphId()))), user));
    assertEquals(3, jackRevisions.size());

    assertEquals(jackId, jackRevisions.get(0)._2.identifier());
    assertEquals(RevisionType.UPDATE, jackRevisions.get(0)._1);
    assertEquals(johnId, jackRevisions.get(0)._2.getReferences().get("knows")
        .iterator().next());

    assertEquals(jackId, jackRevisions.get(1)._2.identifier());
    assertEquals(RevisionType.UPDATE, jackRevisions.get(1)._1);
    assertTrue(jackRevisions.get(1)._2.getReferences().get("knows").isEmpty());

    assertEquals(jackId, jackRevisions.get(2)._2.identifier());
    assertEquals(RevisionType.INSERT, jackRevisions.get(2)._1);
    assertEquals(maryId, jackRevisions.get(2)._2.getReferences().get("knows")
        .iterator().next());
  }

}