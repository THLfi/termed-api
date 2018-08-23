package fi.thl.termed.service.node;

import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static java.util.stream.Collectors.toCollection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMultimap;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.util.query.ForwardingLuceneSpecification;
import fi.thl.termed.util.query.ForwardingSqlSpecification;
import fi.thl.termed.util.query.Query;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

public class NodeServiceIntegrationTest extends BaseNodeServiceIntegrationTest {

  @Before
  public void verifyGraphNodeDbAndIndexAreEmpty() {
    assertEquals(0,
        nodeService.count(new ForwardingSqlSpecification<>(new NodesByGraphId(graphId)), user));
    assertEquals(0,
        nodeService.count(new ForwardingLuceneSpecification<>(new NodesByGraphId(graphId)), user));
  }

  @After
  public void verifyGraphNodeIndexIntegrity() {
    try (
        Stream<Node> dbNodeStream = nodeService.values(new Query<>(
            new ForwardingSqlSpecification<>(new NodesByGraphId(graphId))), user);
        Stream<Node> indexNodeStream = nodeService.values(new Query<>(
            new ForwardingLuceneSpecification<>(new NodesByGraphId(graphId))), user)) {

      Supplier<TreeSet<Node>> nodeTreeSetSupplier =
          () -> new TreeSet<>(Comparator.comparing(Node::getId));

      Set<Node> dbNodes = dbNodeStream.collect(toCollection(nodeTreeSetSupplier));
      Set<Node> indexNodes = indexNodeStream.collect(toCollection(nodeTreeSetSupplier));

      assertEquals(dbNodes, indexNodes);
    }
  }

  @Override
  protected List<Type> buildTestTypes() {
    TypeId personId = TypeId.of("Person", graphId);
    TypeId groupId = TypeId.of("Group", graphId);

    Type person = Type.builder().id(personId)
        .textAttributes(
            TextAttribute.builder().id("name", personId).regexAll().build(),
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

    return Arrays.asList(person, group);
  }

  @Test
  public void shouldInsertNode() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node examplePerson = Node.builder().id(nodeId).build();

    assertFalse(nodeService.exists(nodeId, user));

    nodeService.save(examplePerson, INSERT, defaultOpts(), user);

    assertTrue(nodeService.exists(nodeId, user));
  }

  @Test(expected = DuplicateKeyException.class)
  public void shouldNotInsertNodeTwice() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node examplePerson = Node.builder().id(nodeId).build();

    assertFalse(nodeService.exists(nodeId, user));

    nodeService.save(examplePerson, INSERT, defaultOpts(), user);

    assertTrue(nodeService.exists(nodeId, user));

    nodeService.save(examplePerson, INSERT, defaultOpts(), user);
  }

  @Test
  public void shouldUpsertNodeTwice() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node examplePerson = Node.builder().id(nodeId).build();

    assertFalse(nodeService.exists(nodeId, user));

    nodeService.save(examplePerson, UPSERT, defaultOpts(), user);

    assertTrue(nodeService.exists(nodeId, user));

    nodeService.save(examplePerson, UPSERT, defaultOpts(), user);

    assertTrue(nodeService.exists(nodeId, user));
  }

  @Test
  public void shouldGenerateNodeNumbers() {
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
  public void shouldOnlyUseGeneratedNumbers() {
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
  public void shouldNotAllowDuplicateCodes() {
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

    try {
      nodeService.save(Stream.of(node0, node1), INSERT, defaultOpts(), user);
      fail("Expected DataIntegrityViolationException");
    } catch (DataIntegrityViolationException e) {
      assertFalse(nodeService.exists(nodeId0, user));
      assertFalse(nodeService.exists(nodeId1, user));
    } catch (Throwable t) {
      fail("Unexpected error: " + t);
    }
  }

  @Test
  public void shouldInsertNodeWithProperties() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node examplePerson = Node.builder()
        .id(nodeId)
        .addProperties("name", "John")
        .addProperties("email", "john@example.org")
        .build();

    assertFalse(nodeService.exists(nodeId, user));

    nodeService.save(examplePerson, INSERT, defaultOpts(), user);

    Node saved = nodeService.get(nodeId, user)
        .orElseThrow(AssertionError::new);

    assertEquals("John", saved.getFirstPropertyValue("name")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));
  }

  @Test
  public void shouldNotInsertNodeWithIllegalProperties() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node examplePerson = Node.builder().id(nodeId)
        .addProperties("name", "John")
        .addProperties("email", "at-symbol-is-required-but-missing")
        .build();

    assertFalse(nodeService.exists(nodeId, user));

    try {
      nodeService.save(examplePerson, INSERT, defaultOpts(), user);
      fail("Expected DataIntegrityViolationException");
    } catch (DataIntegrityViolationException e) {
      assertFalse(nodeService.exists(nodeId, user));
    } catch (Throwable t) {
      fail("Unexpected error: " + t);
    }
  }

  @Test
  public void shouldUpdateNodeWithProperties() {
    NodeId jackId = NodeId.random("Person", graphId);

    Node jack = Node.builder().id(jackId)
        .addProperties("name", "Jack")
        .build();

    nodeService.save(jack, INSERT, defaultOpts(), user);

    Node saved = nodeService.get(jackId, user)
        .orElseThrow(AssertionError::new);

    assertEquals("Jack", saved.getFirstPropertyValue("name")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));

    Node nameUpdatedAndEmailAdded = Node.builderFromCopyOf(saved)
        .properties(ImmutableMultimap.of(
            "name", new StrictLangValue("John"),
            "email", new StrictLangValue("john@example.org")))
        .build();
    nodeService.save(nameUpdatedAndEmailAdded, UPDATE, defaultOpts(), user);

    Node reSaved = nodeService.get(jackId, user)
        .orElseThrow(AssertionError::new);

    assertEquals("John", reSaved.getFirstPropertyValue("name")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));
    assertEquals("john@example.org", reSaved.getFirstPropertyValue("email")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));
  }

  @Test
  public void shouldInsertNodeWithReferences() {
    NodeId johnId = NodeId.random("Person", graphId);
    NodeId jackId = NodeId.random("Person", graphId);
    NodeId maryId = NodeId.random("Person", graphId);

    Node john = Node.builder().id(johnId)
        .addProperties("name", "John")
        .addReferences("knows", jackId)
        .build();
    Node jack = Node.builder().id(jackId)
        .addProperties("name", "Jack")
        .addReferences("knows", maryId)
        .build();
    Node mary = Node.builder().id(maryId)
        .addProperties("name", "Mary")
        .build();

    nodeService.save(Stream.of(john, jack, mary), INSERT, defaultOpts(), user);

    Node saved = nodeService.get(jackId, user)
        .orElseThrow(AssertionError::new);

    assertEquals("Jack", saved.getFirstPropertyValue("name")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));
    assertEquals(johnId, saved.getFirstReferrerValue("knows").orElseThrow(AssertionError::new));
    assertEquals(maryId, saved.getFirstReferenceValue("knows").orElseThrow(AssertionError::new));
  }

  @Test
  public void shouldUpdateNodeWithReferences() {
    NodeId johnId = NodeId.random("Person", graphId);
    NodeId jackId = NodeId.random("Person", graphId);
    NodeId maryId = NodeId.random("Person", graphId);

    Node john = Node.builder().id(johnId)
        .addProperties("name", "John")
        .addReferences("knows", jackId)
        .build();
    Node jack = Node.builder().id(jackId)
        .addProperties("name", "Jack")
        .addReferences("knows", maryId)
        .build();
    Node mary = Node.builder().id(maryId)
        .addProperties("name", "Mary")
        .build();

    nodeService.save(Stream.of(john, jack, mary), INSERT, defaultOpts(), user);

    Node saved = nodeService.get(jackId, user)
        .orElseThrow(AssertionError::new);

    assertEquals("Jack", saved.getFirstPropertyValue("name")
        .map(StrictLangValue::getValue)
        .orElseThrow(AssertionError::new));
    assertEquals(johnId, saved.getFirstReferrerValue("knows").orElseThrow(AssertionError::new));
    assertEquals(maryId, saved.getFirstReferenceValue("knows").orElseThrow(AssertionError::new));

    Node savedWithoutReferences = Node.builderFromCopyOf(saved)
        .references(ImmutableMultimap.of()).build();
    nodeService.save(savedWithoutReferences, UPDATE, defaultOpts(), user);

    Node reSaved = nodeService.get(jackId, user)
        .orElseThrow(AssertionError::new);
    assertFalse(reSaved.getFirstReferenceValue("knows").isPresent());
  }

  @Test
  public void shouldDeleteNodesWithCircularReferences() {
    NodeId johnId = NodeId.random("Person", graphId);
    NodeId jackId = NodeId.random("Person", graphId);

    Node john = Node.builder().id(johnId)
        .addProperties("name", "John")
        .addReferences("knows", jackId)
        .build();
    Node jack = Node.builder().id(jackId)
        .addProperties("name", "Jack")
        .addReferences("knows", johnId)
        .build();

    nodeService.save(Stream.of(john, jack), INSERT, defaultOpts(), user);

    assertTrue(nodeService.exists(johnId, user));
    assertTrue(nodeService.exists(jackId, user));

    nodeService.delete(Stream.of(johnId, jackId), defaultOpts(), user);

    assertFalse(nodeService.exists(johnId, user));
    assertFalse(nodeService.exists(jackId, user));
  }

  @Test
  public void shouldFailToDeleteReferencedNode() {
    NodeId johnId = NodeId.random("Person", graphId);
    NodeId jackId = NodeId.random("Person", graphId);

    Node john = Node.builder().id(johnId)
        .addProperties("name", "John")
        .build();
    Node jack = Node.builder().id(jackId)
        .addProperties("name", "Jack")
        .addReferences("knows", johnId)
        .build();

    nodeService.save(Stream.of(john, jack), INSERT, defaultOpts(), user);

    assertTrue(nodeService.exists(johnId, user));
    assertTrue(nodeService.exists(jackId, user));

    try {
      nodeService.delete(johnId, defaultOpts(), user);
      fail("Expected DataIntegrityViolationException");
    } catch (DataIntegrityViolationException e) {
      assertTrue(nodeService.exists(jackId, user));
    } catch (Throwable t) {
      fail("Unexpected error: " + t);
    }
  }

}