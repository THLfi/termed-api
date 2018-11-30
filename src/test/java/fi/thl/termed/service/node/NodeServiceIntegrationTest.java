package fi.thl.termed.service.node;

import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMultimap;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.Specifications;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

class NodeServiceIntegrationTest extends BaseNodeServiceIntegrationTest {

  @Test
  void shouldInsertNode() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node examplePerson = Node.builder().id(nodeId).build();

    assertFalse(nodeService.exists(nodeId, user));

    nodeService.save(examplePerson, INSERT, defaultOpts(), user);

    assertTrue(nodeService.exists(nodeId, user));
  }

  @Test
  void shouldNotInsertNodeTwice() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node examplePerson = Node.builder().id(nodeId).build();

    assertFalse(nodeService.exists(nodeId, user));

    nodeService.save(examplePerson, INSERT, defaultOpts(), user);

    assertTrue(nodeService.exists(nodeId, user));

    assertThrows(DuplicateKeyException.class,
        () -> nodeService.save(examplePerson, INSERT, defaultOpts(), user));
  }

  @Test
  void shouldUpsertNodeTwice() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node examplePerson = Node.builder().id(nodeId).build();

    assertFalse(nodeService.exists(nodeId, user));

    nodeService.save(examplePerson, UPSERT, defaultOpts(), user);

    assertTrue(nodeService.exists(nodeId, user));

    nodeService.save(examplePerson, UPSERT, defaultOpts(), user);

    assertTrue(nodeService.exists(nodeId, user));
  }

  @Test
  void shouldInsertNodeWithProperties() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node examplePerson = Node.builder()
        .id(nodeId)
        .addProperty("name", "John")
        .addProperty("email", "john@example.org")
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
  void shouldNotInsertNodeWithIllegalProperties() {
    NodeId nodeId = NodeId.random("Person", graphId);
    Node examplePerson = Node.builder().id(nodeId)
        .addProperty("name", "John")
        .addProperty("email", "at-symbol-is-required-but-missing")
        .build();

    assertFalse(nodeService.exists(nodeId, user));

    assertThrows(DataIntegrityViolationException.class,
        () -> nodeService.save(examplePerson, INSERT, defaultOpts(), user));

    assertFalse(nodeService.exists(nodeId, user));
  }

  @Test
  void shouldNotInsertAnyInBatchIfSomeIsWithIllegalProperties() {
    Node p0 = Node.builder().random(TypeId.of("Person", graphId))
        .addProperty("name", "John")
        .addProperty("email", "john@example.og")
        .build();

    Node p1 = Node.builder().random(TypeId.of("Person", graphId))
        .addProperty("name", "Jack")
        .addProperty("email", "at-symbol-is-required-but-missing")
        .build();

    Node p2 = Node.builder().random(TypeId.of("Person", graphId))
        .addProperty("name", "Lisa")
        .addProperty("email", "lisa@example.org")
        .build();

    assertEquals(0, nodeService.count(Specifications.matchAll(), user));

    assertThrows(DataIntegrityViolationException.class,
        () -> nodeService.save(Stream.of(p0, p1, p2), INSERT, defaultOpts(), user));

    assertEquals(0, nodeService.count(Specifications.matchAll(), user));
  }

  @Test
  void shouldUpdateNodeWithProperties() {
    NodeId jackId = NodeId.random("Person", graphId);

    Node jack = Node.builder().id(jackId)
        .addProperty("name", "Jack")
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
  void shouldInsertNodeWithReferences() {
    NodeId johnId = NodeId.random("Person", graphId);
    NodeId jackId = NodeId.random("Person", graphId);
    NodeId maryId = NodeId.random("Person", graphId);

    Node john = Node.builder().id(johnId)
        .addProperty("name", "John")
        .addReference("knows", jackId)
        .build();
    Node jack = Node.builder().id(jackId)
        .addProperty("name", "Jack")
        .addReference("knows", maryId)
        .build();
    Node mary = Node.builder().id(maryId)
        .addProperty("name", "Mary")
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
  void shouldUpdateNodeWithReferences() {
    NodeId johnId = NodeId.random("Person", graphId);
    NodeId jackId = NodeId.random("Person", graphId);
    NodeId maryId = NodeId.random("Person", graphId);

    Node john = Node.builder().id(johnId)
        .addProperty("name", "John")
        .addReference("knows", jackId)
        .build();
    Node jack = Node.builder().id(jackId)
        .addProperty("name", "Jack")
        .addReference("knows", maryId)
        .build();
    Node mary = Node.builder().id(maryId)
        .addProperty("name", "Mary")
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
  void shouldDeleteNodesWithCircularReferences() {
    NodeId johnId = NodeId.random("Person", graphId);
    NodeId jackId = NodeId.random("Person", graphId);

    Node john = Node.builder().id(johnId)
        .addProperty("name", "John")
        .addReference("knows", jackId)
        .build();
    Node jack = Node.builder().id(jackId)
        .addProperty("name", "Jack")
        .addReference("knows", johnId)
        .build();

    nodeService.save(Stream.of(john, jack), INSERT, defaultOpts(), user);

    assertTrue(nodeService.exists(johnId, user));
    assertTrue(nodeService.exists(jackId, user));

    nodeService.delete(Stream.of(johnId, jackId), defaultOpts(), user);

    assertFalse(nodeService.exists(johnId, user));
    assertFalse(nodeService.exists(jackId, user));
  }

  @Test
  void shouldFailToDeleteReferencedNode() {
    NodeId johnId = NodeId.random("Person", graphId);
    NodeId jackId = NodeId.random("Person", graphId);

    Node john = Node.builder().id(johnId)
        .addProperty("name", "John")
        .build();
    Node jack = Node.builder().id(jackId)
        .addProperty("name", "Jack")
        .addReference("knows", johnId)
        .build();

    nodeService.save(Stream.of(john, jack), INSERT, defaultOpts(), user);

    assertTrue(nodeService.exists(johnId, user));
    assertTrue(nodeService.exists(jackId, user));

    assertThrows(DataIntegrityViolationException.class,
        () -> nodeService.delete(johnId, defaultOpts(), user));

    assertTrue(nodeService.exists(jackId, user));
  }

}