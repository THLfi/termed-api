package fi.thl.termed.service.node;

import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMultimap;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.service.node.specification.NodeRevisionsByNodeId;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NodeRevisionServiceIntegrationTest extends BaseNodeServiceIntegrationTest {

  @Autowired
  private Service<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionService;

  @Override
  protected List<Type> buildTestTypes() {
    TypeId personId = new TypeId("Person", graphId);
    return singletonList(
        Type.builder().id(personId)
            .textAttributes(
                TextAttribute.builder().id("firstName", personId).regexAll().build(),
                TextAttribute.builder().id("lastName", personId).regexAll().build())
            .referenceAttributes(
                ReferenceAttribute.builder().id("knows", personId).range(personId).build())
            .build());
  }

  @Test
  public void shouldSaveRevisionsForNodesWithProperties() {
    NodeId nodeId = new NodeId(UUID.randomUUID(), "Person", graphId);

    Node person = Node.builder().id(nodeId)
        .addProperties("firstName", "John")
        .addProperties("lastName", "Doe")
        .build();

    assertFalse(nodeService.exists(nodeId, user));

    nodeService.save(person, INSERT, defaultOpts(), user);
    assertTrue(nodeService.exists(nodeId, user));

    Node personUpdated = Node.builderFromCopyOf(person)
        .properties(ImmutableMultimap.of(
            "firstName", new StrictLangValue("Jane"),
            "lastName", new StrictLangValue("Doe")))
        .build();
    nodeService.save(personUpdated, UPDATE, defaultOpts(), user);

    nodeService.delete(nodeId, defaultOpts(), user);

    List<Tuple2<RevisionType, Node>> revisions = toListAndClose(
        nodeRevisionService.values(new Query<>(new NodeRevisionsByNodeId(nodeId)), user));
    assertEquals(3, revisions.size());

    assertEquals(nodeId, revisions.get(0)._2.identifier());
    assertEquals(RevisionType.DELETE, revisions.get(0)._1);

    assertEquals(nodeId, revisions.get(1)._2.identifier());
    assertEquals(RevisionType.UPDATE, revisions.get(1)._1);
    assertEquals("Jane", revisions.get(1)._2.getFirstPropertyValue("firstName")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));
    assertEquals("Doe", revisions.get(1)._2.getFirstPropertyValue("lastName")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));

    assertEquals(nodeId, revisions.get(2)._2.identifier());
    assertEquals(RevisionType.INSERT, revisions.get(2)._1);
    assertEquals("John", revisions.get(2)._2.getFirstPropertyValue("firstName")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));
    assertEquals("Doe", revisions.get(2)._2.getFirstPropertyValue("lastName")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));
  }

  @Test
  public void shouldSaveRevisionsForNodesWithReferences() {
    NodeId johnId = NodeId.random("Person", graphId);
    NodeId jackId = NodeId.random("Person", graphId);
    NodeId maryId = NodeId.random("Person", graphId);

    Node john = Node.builder().id(johnId)
        .addProperties("firstName", "John")
        .build();

    Node jack = Node.builder().id(jackId)
        .addProperties("firstName", "Jack")
        .addReferences("knows", maryId)
        .build();

    Node mary = Node.builder().id(maryId)
        .addProperties("firstName", "Mary")
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
        nodeRevisionService.values(new Query<>(new NodeRevisionsByNodeId(jackId)), user));
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