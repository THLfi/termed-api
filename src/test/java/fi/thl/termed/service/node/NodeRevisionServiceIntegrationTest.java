package fi.thl.termed.service.node;

import static fi.thl.termed.util.UUIDs.randomUUIDString;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMultimap;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodeRevisionsByNodeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.service.Service;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NodeRevisionServiceIntegrationTest {

  @Autowired
  private Service<NodeId, Node> nodeService;
  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<String, User> userService;
  @Autowired
  private Service<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionReadService;
  @Autowired
  private PasswordEncoder passwordEncoder;

  private User testUser;
  private UUID testGraphId;

  @Before
  public void setUp() {
    testGraphId = UUIDs.nameUUIDFromString("testGraph");
    testUser = new User("testUser", passwordEncoder.encode(randomUUIDString()), AppRole.ADMIN);

    userService.save(testUser, UPSERT, defaultOpts(),
        new User("testInitializer", "", AppRole.SUPERUSER));
    graphService.save(Graph.builder().id(testGraphId).build(), UPSERT, defaultOpts(), testUser);

    GraphId graphId = new GraphId(testGraphId);
    TypeId personId = new TypeId("Person", graphId);

    Type person = Type.builder().id(personId)
        .textAttributes(
            TextAttribute.builder().id("firstName", personId).regexAll().build(),
            TextAttribute.builder().id("lastName", personId).regexAll().build())
        .referenceAttributes(
            ReferenceAttribute.builder().id("knows", personId).range(personId).build())
        .build();

    typeService.save(person, UPSERT, defaultOpts(), testUser);
  }

  @Test
  public void shouldSaveRevisionsForNodesWithProperties() {
    NodeId nodeId = new NodeId(UUID.randomUUID(), "Person", testGraphId);

    Node examplePerson = new Node(nodeId);
    examplePerson.setProperties(ImmutableMultimap.of(
        "firstName", new StrictLangValue("John"),
        "lastName", new StrictLangValue("Doe")));

    assertFalse(nodeService.get(nodeId, testUser).isPresent());

    nodeService.save(examplePerson, INSERT, defaultOpts(), testUser);
    assertTrue(nodeService.get(nodeId, testUser).isPresent());

    examplePerson.setProperties(ImmutableMultimap.of(
        "firstName", new StrictLangValue("Jane"),
        "lastName", new StrictLangValue("Doe")));
    nodeService.save(examplePerson, UPDATE, defaultOpts(), testUser);

    nodeService.delete(nodeId, defaultOpts(), testUser);

    List<Tuple2<RevisionType, Node>> revisions =
        nodeRevisionReadService.getValues(new NodeRevisionsByNodeId(nodeId), testUser);
    assertEquals(3, revisions.size());

    assertEquals(nodeId, revisions.get(0)._2.identifier());
    assertEquals(RevisionType.DELETE, revisions.get(0)._1);

    assertEquals(nodeId, revisions.get(1)._2.identifier());
    assertEquals(RevisionType.UPDATE, revisions.get(1)._1);
    assertEquals("Jane", revisions.get(1)._2.getProperties().get("firstName")
        .iterator().next().getValue());
    assertEquals("Doe", revisions.get(1)._2.getProperties().get("lastName")
        .iterator().next().getValue());

    assertEquals(nodeId, revisions.get(2)._2.identifier());
    assertEquals(RevisionType.INSERT, revisions.get(2)._1);
    assertEquals("John", revisions.get(2)._2.getProperties().get("firstName")
        .iterator().next().getValue());
    assertEquals("Doe", revisions.get(2)._2.getProperties().get("lastName")
        .iterator().next().getValue());
  }

  @Test
  public void shouldSaveRevisionsForNodesWithReferences() {
    NodeId johnId = new NodeId(UUID.randomUUID(), "Person", testGraphId);
    Node john = new Node(johnId);
    john.setProperties(ImmutableMultimap.of("firstName", new StrictLangValue("John")));

    NodeId jackId = new NodeId(UUID.randomUUID(), "Person", testGraphId);
    Node jack = new Node(jackId);
    jack.setProperties(ImmutableMultimap.of("firstName", new StrictLangValue("Jack")));

    NodeId maryId = new NodeId(UUID.randomUUID(), "Person", testGraphId);
    Node mary = new Node(maryId);
    mary.setProperties(ImmutableMultimap.of("firstName", new StrictLangValue("Mary")));

    jack.setReferences(ImmutableMultimap.of("knows", maryId));
    nodeService.save(asList(john, jack, mary), INSERT, defaultOpts(), testUser);

    jack.setReferences(ImmutableMultimap.of());
    nodeService.save(jack, UPDATE, defaultOpts(), testUser);

    jack.setReferences(ImmutableMultimap.of("knows", johnId));
    nodeService.save(jack, UPDATE, defaultOpts(), testUser);

    List<Tuple2<RevisionType, Node>> jackRevisions =
        nodeRevisionReadService.getValues(new NodeRevisionsByNodeId(jackId), testUser);
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