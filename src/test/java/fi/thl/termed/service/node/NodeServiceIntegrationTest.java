package fi.thl.termed.service.node;

import static fi.thl.termed.domain.User.newSuperuser;
import static fi.thl.termed.util.UUIDs.randomUUIDString;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMultimap;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.Node.Builder;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.Service;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
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
    testUser = User.newAdmin("testUser", passwordEncoder.encode(randomUUIDString()));

    userService.save(testUser, UPSERT, defaultOpts(), newSuperuser("testInitializer"));
    graphService.save(Graph.builder().id(testGraphId).build(), UPSERT, defaultOpts(), testUser);

    GraphId graphId = GraphId.of(testGraphId);
    TypeId personId = TypeId.of("Person", graphId);
    TypeId groupId = TypeId.of("Group", graphId);

    Type person = Type.builder().id(personId)
        .textAttributes(
            TextAttribute.builder().id("firstName", personId).regexAll().build(),
            TextAttribute.builder().id("lastName", personId).regexAll().build(),
            TextAttribute.builder().id("email", personId).regex("^.*@.*$").build())
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
    NodeId nodeId = NodeId.random("Person", testGraphId);
    Node examplePerson = Node.builder().id(nodeId).build();

    assertFalse(nodeService.exists(nodeId, testUser));

    nodeService.save(examplePerson, INSERT, defaultOpts(), testUser);

    assertTrue(nodeService.exists(nodeId, testUser));
  }

  @Test(expected = DuplicateKeyException.class)
  public void shouldNotInsertNodeTwice() {
    NodeId nodeId = NodeId.random("Person", testGraphId);
    Node examplePerson = Node.builder().id(nodeId).build();

    assertFalse(nodeService.exists(nodeId, testUser));

    nodeService.save(examplePerson, INSERT, defaultOpts(), testUser);

    assertTrue(nodeService.exists(nodeId, testUser));

    nodeService.save(examplePerson, INSERT, defaultOpts(), testUser);
  }

  @Test
  public void shouldUpsertNodeTwice() {
    NodeId nodeId = NodeId.random("Person", testGraphId);
    Node examplePerson = Node.builder().id(nodeId).build();

    assertFalse(nodeService.exists(nodeId, testUser));

    nodeService.save(examplePerson, UPSERT, defaultOpts(), testUser);

    assertTrue(nodeService.exists(nodeId, testUser));

    nodeService.save(examplePerson, UPSERT, defaultOpts(), testUser);

    assertTrue(nodeService.exists(nodeId, testUser));
  }

  @Test
  public void shouldInsertNodeWithProperties() {
    NodeId nodeId = NodeId.random("Person", testGraphId);
    Node examplePerson = Node.builder()
        .id(nodeId)
        .properties(ImmutableMultimap.of(
            "firstName", new StrictLangValue("John"),
            "lastName", new StrictLangValue("Doe")))
        .build();

    assertFalse(nodeService.exists(nodeId, testUser));

    nodeService.save(examplePerson, INSERT, defaultOpts(), testUser);

    Node saved = nodeService.get(nodeId, testUser)
        .orElseThrow(AssertionError::new);

    assertEquals("John", saved.getProperties().get("firstName").iterator().next().getValue());
  }

  @Test
  public void shouldNotInsertNodeWithIllegalProperties() {
    NodeId nodeId = NodeId.random("Person", testGraphId);
    Node examplePerson = Node.builder()
        .id(nodeId)
        .properties(ImmutableMultimap.of(
            "firstName", new StrictLangValue("John"),
            "lastName", new StrictLangValue("Doe"),
            "email", new StrictLangValue("at-symbol-is-required-but-missing")))
        .build();

    assertFalse(nodeService.exists(nodeId, testUser));

    try {
      nodeService.save(examplePerson, INSERT, defaultOpts(), testUser);
      fail("Expected DataIntegrityViolationException");
    } catch (DataIntegrityViolationException e) {
      assertFalse(nodeService.exists(nodeId, testUser));
    } catch (Throwable t) {
      fail("Unexpected error: " + t);
    }
  }

  @Test
  public void shouldUpdateNodeWithProperties() {
    NodeId nodeId = NodeId.random("Person", testGraphId);
    Node examplePerson = Node.builder()
        .id(nodeId)
        .properties("firstName", new StrictLangValue("Jack"))
        .build();
    nodeService.save(examplePerson, INSERT, defaultOpts(), testUser);

    Node saved = nodeService.get(nodeId, testUser)
        .orElseThrow(AssertionError::new);

    assertEquals("Jack", saved.getProperties().get("firstName")
        .iterator().next().getValue());

    Node lastNameAdded = Node.builderFromCopyOf(saved)
        .properties(ImmutableMultimap.of(
            "firstName", new StrictLangValue("John"),
            "lastName", new StrictLangValue("Doe")))
        .build();
    nodeService.save(lastNameAdded, UPDATE, defaultOpts(), testUser);

    Node reSaved = nodeService.get(nodeId, testUser)
        .orElseThrow(AssertionError::new);

    assertEquals("John", reSaved.getProperties().get("firstName").iterator().next().getValue());
    assertEquals("Doe", reSaved.getProperties().get("lastName").iterator().next().getValue());
  }

  @Test
  public void shouldInsertNodeWithReferences() {
    NodeId johnId = NodeId.random("Person", testGraphId);
    Node.Builder john = Node.builder()
        .id(johnId)
        .properties("firstName", new StrictLangValue("John"));

    NodeId jackId = NodeId.random("Person", testGraphId);
    Node.Builder jack = Node.builder()
        .id(jackId)
        .properties("firstName", new StrictLangValue("Jack"));

    NodeId maryId = NodeId.random("Person", testGraphId);
    Node.Builder mary = Node.builder()
        .id(maryId)
        .properties("firstName", new StrictLangValue("Mary"));

    john.references("knows", jackId);
    jack.references("knows", maryId);

    nodeService
        .save(Stream.of(john, jack, mary).map(Builder::build), INSERT, defaultOpts(), testUser);

    Node saved = nodeService.get(jackId, testUser)
        .orElseThrow(AssertionError::new);

    assertEquals("Jack", saved.getProperties().get("firstName").iterator().next().getValue());
    assertEquals(johnId, saved.getReferrers().get("knows").iterator().next());
    assertEquals(maryId, saved.getReferences().get("knows").iterator().next());
  }

  @Test
  public void shouldUpdateNodeWithReferences() {
    NodeId johnId = NodeId.random("Person", testGraphId);
    Node.Builder john = Node.builder()
        .id(johnId)
        .properties("firstName", new StrictLangValue("John"));

    NodeId jackId = NodeId.random("Person", testGraphId);
    Node.Builder jack = Node.builder()
        .id(jackId)
        .properties("firstName", new StrictLangValue("Jack"));

    NodeId maryId = NodeId.random("Person", testGraphId);
    Node.Builder mary = Node.builder()
        .id(maryId)
        .properties("firstName", new StrictLangValue("Mary"));

    john.references("knows", jackId);
    jack.references("knows", maryId);

    nodeService
        .save(Stream.of(john, jack, mary).map(Builder::build), INSERT, defaultOpts(), testUser);

    Node saved = nodeService.get(jackId, testUser)
        .orElseThrow(AssertionError::new);

    assertEquals("Jack", saved.getProperties().get("firstName").iterator().next().getValue());
    assertEquals(johnId, saved.getReferrers().get("knows").iterator().next());
    assertEquals(maryId, saved.getReferences().get("knows").iterator().next());

    Node savedWithoutReferences = Node.builderFromCopyOf(saved)
        .references(ImmutableMultimap.of()).build();
    nodeService.save(savedWithoutReferences, UPDATE, defaultOpts(), testUser);

    Node reSaved = nodeService.get(jackId, testUser)
        .orElseThrow(AssertionError::new);
    assertTrue(reSaved.getReferences().get("knows").isEmpty());
  }

}