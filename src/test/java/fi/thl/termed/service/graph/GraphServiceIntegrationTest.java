package fi.thl.termed.service.graph;

import static fi.thl.termed.util.UUIDs.randomUUIDString;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
class GraphServiceIntegrationTest {

  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<String, User> userService;
  @Autowired
  private Service<String, Property> propertyService;
  @Autowired
  private PasswordEncoder passwordEncoder;

  private User testLoader = User.newSuperuser("TestLoader");
  private boolean labelPropertyInsertedByTest = false;

  private GraphId graphId;
  private User user;

  @BeforeAll
  void setUp() {
    graphId = GraphId.of(randomUUID());

    user = User.newAdmin("TestUser-" + randomUUID(), passwordEncoder.encode(randomUUIDString()));
    userService.save(user, INSERT, defaultOpts(), testLoader);

    if (!propertyService.exists("label", testLoader)) {
      propertyService.save(Property.builder().id("label").build(),
          INSERT, defaultOpts(), testLoader);
      labelPropertyInsertedByTest = true;
    }
  }

  @AfterAll
  void tearDown() {
    graphService.delete(graphId, defaultOpts(), user);
    userService.delete(user.identifier(), defaultOpts(), testLoader);

    if (labelPropertyInsertedByTest) {
      propertyService.delete("label", defaultOpts(), testLoader);
    }
  }

  @Test
  void shouldInsertAndDeleteNewGraph() {
    Graph graph = Graph.builder().id(graphId).build();

    assertFalse(graphService.exists(graphId, user));

    graphService.save(graph, INSERT, defaultOpts(), user);
    assertTrue(graphService.get(graphId, user).isPresent());

    graphService.delete(graphId, defaultOpts(), user);
    assertFalse(graphService.get(graphId, user).isPresent());
  }

  @Test
  void shouldInsertAndDeleteMultipleNewGraphs() {
    GraphId graphId0 = GraphId.of(randomUUID());
    Graph graph0 = Graph.builder().id(graphId0).build();

    GraphId graphId1 = GraphId.of(randomUUID());
    Graph graph1 = Graph.builder().id(graphId1).build();

    assertFalse(graphService.exists(graphId0, user));
    assertFalse(graphService.exists(graphId1, user));

    graphService.save(Stream.of(graph0, graph1), INSERT, defaultOpts(), user);
    assertTrue(graphService.get(graphId0, user).isPresent());
    assertTrue(graphService.get(graphId1, user).isPresent());

    graphService.delete(Stream.of(graphId0, graphId1), defaultOpts(), user);
    assertFalse(graphService.get(graphId0, user).isPresent());
    assertFalse(graphService.get(graphId1, user).isPresent());
  }

  @Test
  void shouldAllowRepeatedUpsertOfGraph() {
    Graph graph = Graph.builder().id(graphId).build();

    assertFalse(graphService.exists(graphId, user));

    graphService.save(graph, UPSERT, defaultOpts(), user);
    assertTrue(graphService.get(graphId, user).isPresent());

    graphService.save(graph, UPSERT, defaultOpts(), user);
    assertTrue(graphService.get(graphId, user).isPresent());

    graphService.delete(graphId, defaultOpts(), user);
    assertFalse(graphService.get(graphId, user).isPresent());
  }

  @Test
  void shouldNotUpdateNonExistentGraph() {
    Graph graph = Graph.builder().id(graphId).build();

    assertFalse(graphService.exists(graphId, user));

    graphService.save(graph, UPDATE, defaultOpts(), user);

    assertFalse(graphService.exists(graphId, user));
  }

  @Test
  void shouldNotInsertExistingGraph() {
    Graph graph = Graph.builder().id(graphId).build();

    assertFalse(graphService.exists(graphId, user));

    graphService.save(graph, INSERT, defaultOpts(), user);
    assertTrue(graphService.get(graphId, user).isPresent());

    assertThrows(DuplicateKeyException.class,
        () -> graphService.save(graph, INSERT, defaultOpts(), user));

    assertTrue(graphService.exists(graphId, user));

    graphService.delete(graphId, defaultOpts(), user);
    assertFalse(graphService.get(graphId, user).isPresent());
  }

  @Test
  void shouldSaveGraphWithProperties() {
    Graph graph = Graph.builder().id(graphId)
        .properties("label", LangValue.of("TestGraph label")).build();

    assertFalse(graphService.exists(graphId, user));

    graphService.save(graph, INSERT, defaultOpts(), user);

    Graph savedGraph = graphService.get(graphId, user).orElseThrow(AssertionError::new);
    assertEquals(
        singletonList(LangValue.of("TestGraph label")),
        savedGraph.getProperties().get("label"));

    graphService.delete(graphId, defaultOpts(), user);
  }

  @Test
  void shouldNotSaveGraphWitIllegalProperties() {
    Graph graph = Graph.builder().id(graphId)
        .properties("label!", LangValue.of("TestGraph label")).build();

    assertFalse(graphService.exists(graphId, user));

    assertThrows(DataIntegrityViolationException.class,
        () -> graphService.save(graph, INSERT, defaultOpts(), user));

    assertFalse(graphService.exists(graphId, user));
  }

  @Test
  void shouldUpdateGraphWithProperties() {
    Graph graph = Graph.builder().id(graphId)
        .properties("label", LangValue.of("TestGraph label")).build();

    assertFalse(graphService.exists(graphId, user));

    graphService.save(graph, INSERT, defaultOpts(), user);

    Graph savedGraph = graphService.get(graphId, user).orElseThrow(AssertionError::new);
    assertEquals(
        singletonList(LangValue.of("TestGraph label")),
        savedGraph.getProperties().get("label"));

    Graph updatedGraph = Graph.builderFromCopyOf(savedGraph)
        .properties("label", LangValue.of("Updated TestGraph label")).build();

    graphService.save(updatedGraph, UPDATE, defaultOpts(), user);

    Graph savedUpdatedGraph = graphService.get(graphId, user).orElseThrow(AssertionError::new);
    assertEquals(
        singletonList(LangValue.of("Updated TestGraph label")),
        savedUpdatedGraph.getProperties().get("label"));

    graphService.delete(graphId, defaultOpts(), user);
  }

}