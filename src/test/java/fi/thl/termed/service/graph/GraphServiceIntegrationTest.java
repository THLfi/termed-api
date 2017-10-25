package fi.thl.termed.service.graph;

import static fi.thl.termed.domain.AppRole.ADMIN;
import static fi.thl.termed.domain.AppRole.SUPERUSER;
import static fi.thl.termed.util.UUIDs.randomUUIDString;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GraphServiceIntegrationTest {

  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<String, User> userService;
  @Autowired
  private Service<String, Property> propertyService;
  @Autowired
  private PasswordEncoder passwordEncoder;

  private User testDataLoader = new User("TestDataLoader", "", SUPERUSER);
  private boolean labelPropertyInsertedByTest = false;

  private GraphId graphId;
  private User user;

  @Before
  public void setUp() {
    graphId = GraphId.of(randomUUID());

    user = new User("TestUser-" + randomUUID(), passwordEncoder.encode(randomUUIDString()), ADMIN);
    userService.save(user, INSERT, defaultOpts(), testDataLoader);

    if (!propertyService.exists("label", testDataLoader)) {
      propertyService.save(Property.builder().id("label").build(),
          INSERT, defaultOpts(), testDataLoader);
      labelPropertyInsertedByTest = true;
    }
  }

  @After
  public void tearDown() {
    userService.delete(user.identifier(), defaultOpts(), testDataLoader);

    if (labelPropertyInsertedByTest) {
      propertyService.delete("label", defaultOpts(), testDataLoader);
    }
  }

  @Test
  public void shouldInsertAndDeleteNewGraph() {
    Graph graph = Graph.builder().id(graphId).build();

    assertFalse(graphService.exists(graphId, user));

    graphService.save(graph, INSERT, defaultOpts(), user);
    assertTrue(graphService.get(graphId, user).isPresent());

    graphService.delete(graphId, defaultOpts(), user);
    assertFalse(graphService.get(graphId, user).isPresent());
  }

  @Test
  public void shouldInsertAndDeleteMultipleNewGraphs() {
    GraphId graphId0 = GraphId.of(randomUUID());
    Graph graph0 = Graph.builder().id(graphId0).build();

    GraphId graphId1 = GraphId.of(randomUUID());
    Graph graph1 = Graph.builder().id(graphId1).build();

    assertFalse(graphService.exists(graphId0, user));
    assertFalse(graphService.exists(graphId1, user));

    graphService.save(asList(graph0, graph1), INSERT, defaultOpts(), user);
    assertTrue(graphService.get(graphId0, user).isPresent());
    assertTrue(graphService.get(graphId1, user).isPresent());

    graphService.delete(asList(graphId0, graphId1), defaultOpts(), user);
    assertFalse(graphService.get(graphId0, user).isPresent());
    assertFalse(graphService.get(graphId1, user).isPresent());
  }

  @Test
  public void shouldAllowRepeatedUpsertOfGraph() {
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
  public void shouldNotUpdateNonExistentGraph() {
    Graph graph = Graph.builder().id(graphId).build();

    assertFalse(graphService.exists(graphId, user));

    graphService.save(graph, UPDATE, defaultOpts(), user);

    assertFalse(graphService.exists(graphId, user));
  }

  @Test
  public void shouldNotInsertExistingGraph() {
    Graph graph = Graph.builder().id(graphId).build();

    assertFalse(graphService.exists(graphId, user));

    graphService.save(graph, INSERT, defaultOpts(), user);
    assertTrue(graphService.get(graphId, user).isPresent());

    try {
      graphService.save(graph, INSERT, defaultOpts(), user);
      fail("Expected DuplicateKeyException");
    } catch (DuplicateKeyException e) {
      assertTrue(graphService.exists(graphId, user));
    } catch (Throwable t) {
      fail("Unexpected error: " + t);
    }

    graphService.delete(graphId, defaultOpts(), user);
    assertFalse(graphService.get(graphId, user).isPresent());
  }

  @Test
  public void shouldSaveGraphWithProperties() {
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
  public void shouldUpdateGraphWithProperties() {
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