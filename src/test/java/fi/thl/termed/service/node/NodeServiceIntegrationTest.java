package fi.thl.termed.service.node;

import static fi.thl.termed.util.UUIDs.randomUUIDString;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
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
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.Service;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NodeServiceIntegrationTest {

  @Autowired
  private Service<NodeId, Node> nodeService;
  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<String, User> userService;
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
    TypeId groupId = new TypeId("Group", graphId);

    Type person = Type.builder().id(personId)
        .textAttributes(
            TextAttribute.builder().id("firstName", personId).regexAll().build(),
            TextAttribute.builder().id("lastName", personId).regexAll().build())
        .referenceAttributes(
            ReferenceAttribute.builder().id("knows", personId).range(personId).build())
        .build();

    Type group = Type.builder().id(groupId)
        .textAttributes(
            TextAttribute.builder().id("name", groupId).regexAll().build())
        .referenceAttributes(
            ReferenceAttribute.builder().id("member", groupId).range(personId).build())
        .build();

    typeService.save(Stream.of(person, group), UPSERT, defaultOpts(), testUser);
  }

  @Test
  public void shouldInsertNode() {
    NodeId nodeId = new NodeId(UUID.randomUUID(), "Person", testGraphId);
    Node examplePerson = new Node(nodeId);

    assertFalse(nodeService.get(nodeId, testUser).isPresent());

    nodeService.save(examplePerson, INSERT, defaultOpts(), testUser);

    assertTrue(nodeService.get(nodeId, testUser).isPresent());
  }

  @Test
  public void shouldInsertNodeWithProperties() {
    NodeId nodeId = new NodeId(UUID.randomUUID(), "Person", testGraphId);
    Node examplePerson = new Node(nodeId);
    examplePerson.setProperties(ImmutableMultimap.of(
        "firstName", new StrictLangValue("John"),
        "lastName", new StrictLangValue("Doe")));

    assertFalse(nodeService.get(nodeId, testUser).isPresent());

    nodeService.save(examplePerson, INSERT, defaultOpts(), testUser);

    Optional<Node> persistedNode = nodeService.get(nodeId, testUser);

    assertTrue(persistedNode.isPresent());
    assertEquals("John", persistedNode.get().getProperties().get("firstName")
        .iterator().next().getValue());
  }

  @Test
  public void shouldUpdateNodeWithProperties() {
    NodeId nodeId = new NodeId(UUID.randomUUID(), "Person", testGraphId);
    Node examplePerson = new Node(nodeId);
    examplePerson.setProperties(ImmutableMultimap.of(
        "firstName", new StrictLangValue("Jack")));
    nodeService.save(examplePerson, INSERT, defaultOpts(), testUser);

    Optional<Node> persistedNodeOptional = nodeService.get(nodeId, testUser);

    assertTrue(persistedNodeOptional.isPresent());
    Node persistedNode = persistedNodeOptional.get();
    assertEquals("Jack", persistedNode.getProperties().get("firstName")
        .iterator().next().getValue());

    persistedNode.setProperties(ImmutableMultimap.of(
        "firstName", new StrictLangValue("John"),
        "lastName", new StrictLangValue("Doe")));
    nodeService.save(persistedNode, UPDATE, defaultOpts(), testUser);

    persistedNodeOptional = nodeService.get(nodeId, testUser);

    assertTrue(persistedNodeOptional.isPresent());
    assertEquals("John", persistedNodeOptional.get().getProperties().get("firstName")
        .iterator().next().getValue());
    assertEquals("Doe", persistedNodeOptional.get().getProperties().get("lastName")
        .iterator().next().getValue());
  }

  @Test
  public void shouldInsertNodeWithReferences() {
    NodeId johnId = new NodeId(UUID.randomUUID(), "Person", testGraphId);
    Node john = new Node(johnId);
    john.setProperties(ImmutableMultimap.of("firstName", new StrictLangValue("John")));

    NodeId jackId = new NodeId(UUID.randomUUID(), "Person", testGraphId);
    Node jack = new Node(jackId);
    jack.setProperties(ImmutableMultimap.of("firstName", new StrictLangValue("Jack")));

    NodeId maryId = new NodeId(UUID.randomUUID(), "Person", testGraphId);
    Node mary = new Node(maryId);
    mary.setProperties(ImmutableMultimap.of("firstName", new StrictLangValue("Mary")));

    john.addReference("knows", jackId);
    jack.addReference("knows", maryId);

    nodeService.save(Stream.of(john, jack, mary), INSERT, defaultOpts(), testUser);

    Optional<Node> persistedNode = nodeService.get(jackId, testUser);

    assertTrue(persistedNode.isPresent());
    assertEquals("Jack", persistedNode.get().getProperties().get("firstName")
        .iterator().next().getValue());
    assertEquals(johnId, persistedNode.get().getReferrers().get("knows")
        .iterator().next());
    assertEquals(maryId, persistedNode.get().getReferences().get("knows")
        .iterator().next());
  }


  @Test
  public void shouldUpdateNodeWithReferences() {
    NodeId johnId = new NodeId(UUID.randomUUID(), "Person", testGraphId);
    Node john = new Node(johnId);
    john.setProperties(ImmutableMultimap.of("firstName", new StrictLangValue("John")));

    NodeId jackId = new NodeId(UUID.randomUUID(), "Person", testGraphId);
    Node jack = new Node(jackId);
    jack.setProperties(ImmutableMultimap.of("firstName", new StrictLangValue("Jack")));

    NodeId maryId = new NodeId(UUID.randomUUID(), "Person", testGraphId);
    Node mary = new Node(maryId);
    mary.setProperties(ImmutableMultimap.of("firstName", new StrictLangValue("Mary")));

    john.addReference("knows", jackId);
    jack.addReference("knows", maryId);

    nodeService.save(Stream.of(john, jack, mary), INSERT, defaultOpts(), testUser);

    Optional<Node> persistedOptionalNode = nodeService.get(jackId, testUser);

    assertTrue(persistedOptionalNode.isPresent());
    Node persistedNode = persistedOptionalNode.get();
    assertEquals("Jack", persistedNode.getProperties().get("firstName")
        .iterator().next().getValue());
    assertEquals(johnId, persistedNode.getReferrers().get("knows").iterator().next());
    assertEquals(maryId, persistedNode.getReferences().get("knows").iterator().next());

    persistedNode.setReferences(ImmutableMultimap.of());
    nodeService.save(persistedNode, UPDATE, defaultOpts(), testUser);

    persistedOptionalNode = nodeService.get(jackId, testUser);
    assertTrue(persistedOptionalNode.isPresent());
    persistedNode = persistedOptionalNode.get();
    assertTrue(persistedNode.getReferences().get("knows").isEmpty());
  }

}